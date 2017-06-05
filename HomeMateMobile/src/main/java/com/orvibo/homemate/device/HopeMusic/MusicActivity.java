package com.orvibo.homemate.device.HopeMusic;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.NetChangeHelper;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.device.HopeMusic.listener.OnCmdSendListener;
import com.orvibo.homemate.device.HopeMusic.socket.HopeSocket;
import com.orvibo.homemate.device.HopeMusic.util.HopeJsonHandler;
import com.orvibo.homemate.device.manage.edit.DeviceEditActivity;
import com.orvibo.homemate.model.DeleteDevice;
import com.orvibo.homemate.sharedPreferences.GatewayOnlineCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.AppTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.NetworkUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.nbhope.smarthomelib.app.api.MusicAppService;
import cn.nbhope.smarthomelib.app.api.impl.MusicAppServiceImpl;
import cn.nbhope.smarthomelib.app.enity.DevicePlayState;
import cn.nbhope.smarthomelib.app.type.AppCommandType;
import cn.nbhope.smarthomelib.app.type.HopeCommandType;

/**
 * Created by wuliquan on 2016/6/17.
 */
public class MusicActivity extends BaseActivity implements OnCmdSendListener, NetChangeHelper.OnNetChangedListener, ScreenListener.ScreenStateListener {
    private static final String TAG = MusicActivity.class.getSimpleName();
    private String deviceId;
    private String mToken;
    private Device device;
    /**
     * 用来标识是否连接成功
     */
    private boolean isConnected;
    /**
     * 用来防止重新登陆冲突
     */
    private boolean isReLogin;
    private long    reLoginTime;

    /**
     * 限制重新登陆次数
     */
    private int reloginCount;
    private static final int LIMIT_COUNT = 10;

    /**
     * 用来标识是否退出
     */
    private boolean isExit;

    /**
     * 屏幕熄灭打开使用
     */
    //用来标识是否已经由于熄屏断开socket
    private boolean isStopSocketByScreen;
    //屏幕监听者
    private ScreenListener screenListener;
    //屏幕熄屏时间
    private static final int SCREEN_OFF_LIMIT_TIME = 60*1000;

    /**
     * JSON 解析器
     */
    private JsonParser parser;

    private NetChangeHelper mNetChangeHelper;
    private HopeMusicHelper hopeMusicHelper;
    private HopeJsonHandler mHopeHandler;
    private BaseFragment baseFragment;
    private SongListFragment songListFragment;
    private SongPlayFragment songPlayFragment;

    private MusicAppService musicAppService =new MusicAppServiceImpl();
    private List<ServerStatusListener> serverStatusListenerList = new ArrayList<>();

    //用来存储已发送出去还没超时的命令对象
    private List<CmdTimeBean> cmdTimeBeanList =new ArrayList<>();

    private static final int CMD_OUT_TIME = 3*1000;
    private static final int MSG_EXIT_ACTIVITY = 0x110;
    private static final int MSG_JUDGE_CMD_OUT_TIME = 0x111;
    private static final int MSG_SCREEN_OFF_STOP_SOCKET = 0x112;
    private static final int MSG_SCREEN_ON_OPEN_SOCKET = 0x113;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg != null) {
                switch (msg.what) {
                    case MSG_EXIT_ACTIVITY:
                       finish();
                        break;
                    case MSG_JUDGE_CMD_OUT_TIME:
                        judgeCmdOutTime();
                        break;
                    case HopeSocket.SEND_OUT_TIME:
                        dismissDialog();
                        ToastUtil.showToast(getResources().getString(R.string.TIMEOUT_CD));
                        break;
                    case HopeSocket.MSG_HANDLE_SERVER_CALLBACK:
                        disposServerCallBack(msg);
                        break;
                    case MSG_SCREEN_OFF_STOP_SOCKET:
                        isStopSocketByScreen=true;
                        hopeMusicHelper.stopSocket();
                        break;
                    case MSG_SCREEN_ON_OPEN_SOCKET:
                        if(mHandler.hasMessages(MSG_SCREEN_OFF_STOP_SOCKET)){
                            mHandler.removeMessages(MSG_SCREEN_OFF_STOP_SOCKET);
                        }
                        if(isStopSocketByScreen){
                            isStopSocketByScreen=false;
                            //如果activity是在前台
                            if (AppTool.isAppOnForeground(mAppContext)){
                            reLoginHopeServer();}
                        }
                        break;

                    default:
                        break;
                }

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        device = (Device) getIntent().getSerializableExtra(IntentKey.DEVICE);
        deviceId = device.getIrDeviceId();
        hopeMusicHelper =new HopeMusicHelper(MusicActivity.this);
        mHopeHandler = HopeJsonHandler.getInstance();
        mHopeHandler.setmOwnDeviceId(deviceId);

        songListFragment = new SongListFragment();
        songPlayFragment = new SongPlayFragment();

        songListFragment.setOnCmdSendListener(this);
        songPlayFragment.setOnCmdSendListener(this);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.add(R.id.fl_main,songListFragment).commit();
        baseFragment  = songListFragment;

        mNetChangeHelper = NetChangeHelper.getInstance(MusicActivity.this);
        mNetChangeHelper.doCheck(this);

        screenListener = new ScreenListener(this);
        screenListener.begin(this);

        /**
         * 先去登陆向往公司服务器获得token，然后再与向往服务器建立socket长链接
         */
        hopeMusicHelper.setLoginHopeServerListener(new HopeMusicHelper.LoginHopeServerListener() {
            @Override
            public void loginSuccess(String token) {
                dismissDialog();
                mToken = token;
                hopeMusicHelper.startSocket(MusicActivity.this,token);
                hopeMusicHelper.setSocketThreadHandler(mHandler);
                for(ServerStatusListener listener:serverStatusListenerList){
                    listener.statusChange(deviceId,token);
                }
            }

            @Override
            public void loginFail(String msg) {
                isReLogin=false;
                dismissDialog();
                ToastUtil.showToast(getResources().getString(R.string.network_canot_work));
            }
        });

        hopeMusicHelper.setConnectServerListener(new HopeSocket.ConnectServerListener() {
            @Override
            public void onReConnecting() {
                isConnected=false;
            }

            @Override
            public void onConnected() {
                isReLogin=false;
                isConnected = false;
                hopeMusicHelper.initState(deviceId,mToken);
            }

            @Override
            public void onDisconnected() {
                isConnected=false;
            }
        });

        judgeIsHaveDevcie();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (device != null) {
            device = new DeviceDao().selDevice(device.getUid(), device.getDeviceId());
        }
        if(!isConnected){
            hopeMusicHelper.loginHopeServer();
            showDialog();
        }else {
            freshPlayState();
        }
    }

    /**
     * 设置状态离线
     */
    private void setOffline(){
        GatewayOnlineCache.setOffline(MusicActivity.this,device.getUid());
    }

    /**
     *   设置背景色
     */
    public void setMainBackground(int color){
        findViewById(R.id.main_background).setBackgroundColor(color);

    }
    /**
     *   获取背景主布局
     */
    public LinearLayout getMainBackground(){
        return (LinearLayout) findViewById(R.id.main_background);
    }

    public void registerStatus(ServerStatusListener listener){
        if(!serverStatusListenerList.contains(listener)){
            serverStatusListenerList.add(listener);
        }
    }

    public void unRegisterStatus(ServerStatusListener listener){
        if(serverStatusListenerList!=null)
        if(serverStatusListenerList.contains(listener)){
            serverStatusListenerList.remove(listener);
        }
    }

    /**
     * 刷新设备的现在状态
     */
    public void freshPlayState(){
        if (NetUtil.isNetworkEnable(MusicActivity.this)) {
            if (deviceId != null && mToken != null)
                hopeMusicHelper.initState(deviceId, mToken);
        }else{
            ToastUtil.showToast(getResources().getString(R.string.network_canot_work));
        }
    }
    /**
     *   处理服务端反馈的内容
     */
    private void disposServerCallBack(Message msg){
        String data = (String) msg.obj;
        if(parser==null){
            parser = new JsonParser();
        }
        JsonElement root = parser.parse(data);
        JsonObject rootJson = root.getAsJsonObject();
        JsonElement cmdElement = rootJson.get("Cmd");
        String cmd = cmdElement.getAsString();
        JsonElement resultElement = rootJson.get("Result");
        //屏蔽心跳返回（心跳不返回Result字段）
        String result="";
        if(resultElement!=null) {
            result = resultElement.getAsString();
        }

        //如果是心跳
        if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_HEARTTICK)) {
            //如果需要重新连接，则重连
            if (isNeedReLogin(cmd,result)) {
                reLoginHopeServer();
            }
        } else {
            dismissDialog();
            removeCmdFromList(cmd);
            //再过滤一下设备未在线的情况
            if(!filterDeviceOffline(result,rootJson)) {
                mHopeHandler.responseDevice(data);
            }
        }
    }

    /**
     *   从向往服务端请求，判断该设备是否绑定在用户名下
     */
    public void judgeIsHaveDevcie(){
        hopeMusicHelper.judgeIsHaveDevice( deviceId,new HopeMusicHelper.JudgeIsHaveListener(){
            @Override
            public void isHave(boolean isHave) {
                //如果向往服务器没有该设备则需要从我们服务器解绑
               if (!isHave){
                   ToastUtil.showToast(getResources().getString(R.string.device_no_bind));
                   String userName = UserCache.getCurrentUserName(MusicActivity.this);
                   showDialogNow();
                   mDeleteDevice.deleteWifiDeviceOrGateway(device.getUid(),userName);
               }
            }

            @Override
            public void error(String error) {
                //需要重新登陆
              if(error!=null&error.equals(MusicConstant.ERROR_RELOGIN)){
                  reLoginHopeServer();
              }
            }
        });
    }

    DeleteDevice mDeleteDevice = new DeleteDevice(MusicActivity.this) {
        @Override
        public void onDeleteDeviceResult(String uid, int serial, final int result) {
            dismissDialog();
            if (result == ErrorCode.SUCCESS) {
                isExit = true;
                mHandler.sendEmptyMessage(MSG_EXIT_ACTIVITY);
            }else {
                ToastUtil.showToast(R.string.device_delete_failure, Toast.LENGTH_SHORT);
            }
        }
    };
    /**
     * 跳到播放页面后重新获取一下设备播放初始状态
     */
    public void goToPlayFragment(Bundle bundle){
        if(!isExit&&songPlayFragment!=null) {
            songPlayFragment.setArg(bundle.getString("deviceId", ""), bundle.getString("token", ""), (DevicePlayState) bundle.getSerializable("devicePlayState"));
            switchFragment(songListFragment, songPlayFragment);
            freshPlayState();
        }
    }

    /**
     * 跳转到音乐列表页面
     */
    public void geToListFragment(){
        if(!isExit&&songListFragment!=null) {
            switchFragment(songPlayFragment, songListFragment);
            freshPlayState();
        }
    }

    /**
     * 跳转到设备设置页面
     */
    public void goToSetDeviceActivity(){
        if(!isExit) {
            Intent intent = new Intent();
            intent.setClassName(mAppContext, DeviceEditActivity.class.getName());
            intent.putExtra(IntentKey.DEVICE, device);
            intent.putExtra(IntentKey.IS_HOME_CLICK, false);
            startActivity(intent);
        }
    }
    /**
     * 切换页面的重载，优化了fragment的切换
     *
     */
    public void switchFragment(BaseFragment from, BaseFragment to) {
        if (from == null || to == null)
            return;
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        if (!to.isAdded()) {
            // 隐藏当前的fragment，add下一个到Activity中
            transaction.hide(from).add(R.id.fl_main, to).commit();
        } else {
            // 隐藏当前的fragment，显示下一个
            transaction.hide(from).show(to).commit();
        }
        baseFragment  = to;
    }


    /**
     *  判断是否需要重新登陆
     */
    private boolean  isNeedReLogin(String cmd,String result){

        if(cmd.equals(AppCommandType.APPCOMMAND_TYPE_HEARTTICK)){
            if(result.equals(MusicConstant.NEED_RELOGIN)){
              return true;
            }
        }
        return false;
    }

    /**
     *  过滤客户端端发送了命令，服务端反馈设备不在线的情况
     */
    private boolean filterDeviceOffline(String result,JsonObject dataObject){
        if(result.equals("Failed")){
            JsonElement dataElement = dataObject.get("Data");
            if(dataElement!=null) {
                JsonObject messageObject = dataElement.getAsJsonObject();
                JsonElement element = messageObject.get("Message");
                if (element.getAsString().equals(MusicConstant.DEVICE_OFFLINE)) {
                    baseFragment.deviceOffline();
                    setOffline();
                    return true;
                }
            }
        }
        return false;

    }
    @Override
    public void sendCmd(String cmd, boolean isSelect, DevicePlayState song) {
        if(isExit){
            return;
        }
        if(!NetworkUtil.isNetworkAvailable(MusicActivity.this)){
            ToastUtil.showToast(R.string.network_canot_work);
            return;
        }
        if(cmd==null||hopeMusicHelper==null||song==null)
            return;

        showDialog();

        //这里由于客户端发送（上一首）（下一首）切换歌曲时，服务端只会返回MusicPlayEx
        if(cmd!=null&(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICPREV)||cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICNEXT))){
            CmdTimeBean cmdTimeBean = new CmdTimeBean(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAYEX,System.currentTimeMillis());
            cmdTimeBeanList.add(cmdTimeBean);
        }//这里由于拖动进度条后发送命令，但服务端返回的是musicplay
        else if(cmd!=null&cmd.equals(MusicConstant.MUSIC_PLAY_POSITION)){
            CmdTimeBean cmdTimeBean = new CmdTimeBean(HopeCommandType.HOPECOMMAND_TYPE_SEEKPOSTION,System.currentTimeMillis());
            cmdTimeBeanList.add(cmdTimeBean);
        }
        else {
            CmdTimeBean cmdTimeBean = new CmdTimeBean(cmd,System.currentTimeMillis());
            cmdTimeBeanList.add(cmdTimeBean);
        }



        //延时1000ms启动超时检测
        mHandler.sendEmptyMessageDelayed(MSG_JUDGE_CMD_OUT_TIME,1000);

        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAY)){
            hopeMusicHelper.commondSend(musicAppService.musicPlay(song.getDeviceId()+"",mToken));
        }
        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAYEX)){
            hopeMusicHelper.commondSend(musicAppService.musicPlayEx(song.getDeviceId()+"",song.getIndex()+"",mToken));
        }
        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICPAUSE)){
            hopeMusicHelper.commondSend(musicAppService.musicPause(song.getDeviceId()+"",mToken));
        }
        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICPREV)){
            hopeMusicHelper.commondSend(musicAppService.musicPrev(song.getDeviceId()+"",mToken));
        }
        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICNEXT)){
            hopeMusicHelper.commondSend(musicAppService.musicNext(song.getDeviceId()+"",mToken));
        }
        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICVOLUMESET)){
            String voiceset = musicAppService.musicVolumeSet(song.getDeviceId()+"",song.getCurrentVol(),mToken);
            hopeMusicHelper.commondSend(voiceset);
        }
        if (cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICCHANGEDSOURCE)){
            hopeMusicHelper.commondSend(musicAppService.musicChangedSource(song.getDeviceId()+"",Integer.parseInt(song.getSource()),mToken));
        }
        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICCHANGEMODE)){
            hopeMusicHelper.commondSend(musicAppService.musicChangeMode(song.getDeviceId()+"",Integer.parseInt(song.getMode()),mToken));
        }
        if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSIC_SOUND_EFFECT)){
            hopeMusicHelper.commondSend(musicAppService.musicChangedEffect(song.getDeviceId()+"",Integer.parseInt(song.getEffect()),mToken));
        }
        if(cmd.equals(MusicConstant.MUSIC_PLAY_POSITION)){
            hopeMusicHelper.commondSend(musicAppService.seekMusicPostion(song.getDeviceId(),song.getProgress(),mToken));
        }
    }

    /**
     *  用来判断是否有已经超时的命令，如果有的话从list清除掉，并发送超时消息
     */
    private void judgeCmdOutTime(){
        if(cmdTimeBeanList!=null){
            long currentTime = System.currentTimeMillis();
            Iterator<CmdTimeBean> it = cmdTimeBeanList.iterator();
            while (it.hasNext()) {
                CmdTimeBean bean = it.next();
                if(currentTime-bean.createTime>CMD_OUT_TIME){
                    it.remove();
                    mHandler.sendEmptyMessage(HopeSocket.SEND_OUT_TIME);
                }
            }
            //只有命令缓存大于0才去启动重新检查
            if(cmdTimeBeanList.size()>0){
                mHandler.sendEmptyMessage(MSG_JUDGE_CMD_OUT_TIME);
            }
        }
    }

    /**
     * 当有消息返回时清除对应缓存
     * @param cmd
     */
    private void removeCmdFromList(String cmd){

        if(cmdTimeBeanList!=null) {
            Iterator<CmdTimeBean> it = cmdTimeBeanList.iterator();
            while (it.hasNext()) {
                CmdTimeBean bean = it.next();
                if (bean.cmd.equals(cmd)) {
                    it.remove();
                }
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(baseFragment!=null){
            baseFragment.onBack();}
            return false;
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 退出音乐播放Activity
     */
    private void exitMusicActivity(){
        if(mHandler!=null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if(hopeMusicHelper!=null){
            hopeMusicHelper.release();
            hopeMusicHelper=null;
        }

        if(serverStatusListenerList!=null){
            serverStatusListenerList.clear();
            serverStatusListenerList=null;
        }

        if(mHopeHandler!=null){
            mHopeHandler.release();
            mHopeHandler = null;
        }

        baseFragment= null;
        songListFragment = null;
        songPlayFragment = null;

        if(mNetChangeHelper!=null) {
            mNetChangeHelper.cancelCheck();
        }
        if(screenListener!=null){
        screenListener.unregisterListener();}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitMusicActivity();
    }

    /**
     *    检测网络变化，如果客户端页面还没有退出音乐播放就需要重新连接向往服务器
     */
    @Override
    public void onNetChanged(){
        reLoginHopeServer();
    }

    /**
     * 重新获取token，重新登陆向往服务器
     */
    private void reLoginHopeServer() {
        if (NetUtil.isNetworkEnable(MusicActivity.this)) {
            String userName = UserCache.getCurrentUserName(MusicActivity.this);
            if (!StringUtil.isEmpty(userName)) {
                String md5Password = UserCache.getMd5Password(MusicActivity.this, userName);
                if (!StringUtil.isEmpty(md5Password)) {
                    int loginStatus = UserCache.getLoginStatus(MusicActivity.this, userName);
                    if (loginStatus == LoginStatus.SUCCESS ) {
                        if(hopeMusicHelper!=null) {
                            boolean isCanLogin = true;
                            if(!isReLogin){
                                isCanLogin = true;
                            }else{
                                //如果距离上一次登陆超过3000ms，则认为可以重新登陆
                                if(System.currentTimeMillis()-reLoginTime>3000){
                                    //如果重登次数超过了设定值就不重新登陆
                                    if(reloginCount>LIMIT_COUNT){
                                        isCanLogin=false;
                                        reloginCount=0;
                                    }else{
                                        isCanLogin=true;
                                    }

                                }else{
                                    isCanLogin=false;
                                }
                            }
                            //如果可以重新登陆
                            if(isCanLogin) {
                                //重登变量增加
                                reloginCount++;
                                showDialog();
                                isReLogin = true;
                                reLoginTime = System.currentTimeMillis();
                                hopeMusicHelper.loginHopeServer();
                            }
                        }
                    }
                }
            }
        } else {
            if(hopeMusicHelper!=null){
            hopeMusicHelper.stopSocket();}
            if(songListFragment!=null)
            songListFragment.onNetChanged();
            if(songPlayFragment!=null)
            songPlayFragment.onNetChanged();
            LogUtil.w(TAG, "reconnect()-Net disconnect,do not reconnect.");
        }
    }

    @Override
    public void onScreenOn() {
        mHandler.sendEmptyMessage(MSG_SCREEN_ON_OPEN_SOCKET);
    }

    @Override
    public void onScreenOff() {
     isStopSocketByScreen = false;
      mHandler.sendEmptyMessageDelayed(MSG_SCREEN_OFF_STOP_SOCKET,SCREEN_OFF_LIMIT_TIME);
    }

    @Override
    public void onUserPresent() {
        mHandler.sendEmptyMessage(MSG_SCREEN_ON_OPEN_SOCKET);
    }

    public interface ServerStatusListener{
        void statusChange(String deviceId,String token);
    }

    /**
     *  由于向往服务器没有消息机制，暂时新建此类帮助管理命令（主要用来计算超时）
     */
    private class CmdTimeBean{
        //命令名称
        private String cmd;
        //命令发送启始时间（单位：ms）
        private long createTime;

        public CmdTimeBean(String cmd, long createTime) {
            this.cmd = cmd;
            this.createTime = createTime;
        }
    }


}
