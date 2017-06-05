package com.orvibo.homemate.device.HopeMusic;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.Bean.Song;
import com.orvibo.homemate.device.HopeMusic.listener.OnCmdSendListener;
import com.orvibo.homemate.device.HopeMusic.util.CommonUtil;
import com.orvibo.homemate.device.HopeMusic.util.HopeJsonHandler;
import com.orvibo.homemate.device.HopeMusic.widget.BasePopupWindow;
import com.orvibo.homemate.device.HopeMusic.widget.CircleImageView;
import com.orvibo.homemate.device.HopeMusic.widget.DragProgressBar;
import com.orvibo.homemate.device.HopeMusic.widget.SelectEffectPopupWindow;
import com.orvibo.homemate.device.HopeMusic.widget.SelectPicPopupWindow;
import com.orvibo.homemate.device.HopeMusic.widget.SelectSourcePopupWindow;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;
import cn.nbhope.smarthomelib.app.type.HopeCommandType;

/**
 * Created by wuliquan on 2016/5/27.
 */
public class SongPlayFragment extends BaseFragment implements View.OnClickListener,SeekBar.OnSeekBarChangeListener
        , SongTimerManager.OnPlayProgress, OnCmdSendListener,MusicActivity.ServerStatusListener, DragProgressBar.OnDragProgressListener {

    private final static String TAG = "SongPlayFragment";
    private CircleImageView cover_img;
    private ImageButton play_pause_btn;
    private DragProgressBar dragProgressBar;
    private ImageButton mode_ibtn;
    private View parentView;
    private TextView title_tv;
    private TextView sub_title_tv;
    private SongTimerManager songTimerManager;
    private HopeJsonHandler mHopeHandler;
    private MusicManager musicManager;
    private DevicePlayState devicePlayState;
    private RotateAnimation animation;
    private BasePopupWindow basePopupWindow;

    //判断动画是否执行
    private boolean isStartAnim=false;
    private OnCmdSendListener onCmdSendListener;

    private boolean isResumed;

    private boolean isDestory;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!isAdded() || isDetached()||isDestory){
                LogUtil.w(TAG,"Fragment is destory or isDetached");
                return;
            }
            switch (msg.what) {
                case HopeJsonHandler.CONTROL_FAIL:
                    String result = (String) msg.obj;
                    ToastUtil.showToast(result);
                    break;
                case HopeJsonHandler.NET_DISCONNECT:
                    stopRoatAnimation();
                    if(songTimerManager!=null){
                        songTimerManager.pausePlay();}
                    if(devicePlayState!=null){
                        devicePlayState.setState(MusicConstant.STATE_1);}
                    if(play_pause_btn!=null)
                        play_pause_btn.setImageResource(R.drawable.play_selector);
                    if (isFragmentVisible()) {
                        ToastUtil.showToast(getResources().getString(R.string.NET_DISCONNECT));
                    }
                    break;
                case HopeJsonHandler.DEVICE_DISCONNECT:
                    stopRoatAnimation();
                    if(songTimerManager!=null){
                    songTimerManager.pausePlay();}
                    if(devicePlayState!=null){
                    devicePlayState.setState(MusicConstant.STATE_1);}
                    if(play_pause_btn!=null)
                    play_pause_btn.setImageResource(R.drawable.play_selector);
                    if(isFragmentVisible()) {
                        ToastUtil.showToast(getResources().getString(R.string.hope_device_offline));
                    }
                    break;
                case HopeJsonHandler.MSG_HANDLE_INIT_STATE:
                    devicePlayState = (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    break;
                case HopeJsonHandler.MUSIC_PROGRESS:
                    devicePlayState= (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    break;
                case HopeJsonHandler.MSG_HANDLE_CHANGE_SOURCE:
                    devicePlayState.setSource((String) msg.obj);
                    if (basePopupWindow!=null)
                    basePopupWindow.initSource((String) msg.obj);
                    break;
                case HopeJsonHandler.MSG_HANDLE_CHANGE_PLAY_MODE:
                    devicePlayState.setMode((String) msg.obj);
                    initMode((String) msg.obj,true);
                    break;
                case HopeJsonHandler.MSG_HANDLE_CHANGE_VOICE:
                    devicePlayState.setCurrentVol((String) msg.obj);
                    if (basePopupWindow!=null)
                    basePopupWindow.initVoice((String) msg.obj);
                    break;
                case HopeJsonHandler.MSG_HANDLE_CHANGE_VOICE_EFFECT:
                    devicePlayState.setEffect((String) msg.obj);
                    if (basePopupWindow!=null)
                    basePopupWindow.initEffect((String) msg.obj);
                    break;
                case HopeJsonHandler.MSG_HANDLE_MUSIC_PLAYEX:
                    devicePlayState = (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    break;
                case HopeJsonHandler.MSG_HANDLE_MUSIC_PLAY:
                    devicePlayState = (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    break;
                case HopeJsonHandler.MSG_HANDLE_MUSIC_PAUSE:
                    stopRoatAnimation();
                    if(songTimerManager!=null) {
                        songTimerManager.pausePlay();
                    }
                    devicePlayState.setState(MusicConstant.STATE_1);
                    play_pause_btn.setImageResource(R.drawable.play_selector);
                    break;


            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAnimation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.activity_music_play, container, false);

        cover_img=(CircleImageView)parentView.findViewById(R.id.cover_img);
        play_pause_btn=(ImageButton)parentView.findViewById(R.id.play_pause_btn);
        mode_ibtn=(ImageButton)parentView.findViewById(R.id.mode_ibtn);
        parentView.findViewById(R.id.menu_btn).setOnClickListener(this);
        parentView.findViewById(R.id.pre_ibtn).setOnClickListener(this);
        parentView.findViewById(R.id.next_ibtn).setOnClickListener(this);
        play_pause_btn.setOnClickListener(this);
        mode_ibtn.setOnClickListener(this);

        parentView.findViewById(R.id.back).setOnClickListener(this);
        if(isFragmentVisible()) {
            parentView.findViewById(R.id.heard).setBackgroundColor(getResources().getColor(R.color.transparent));
        }
        ViewGroup viewGroup= (ViewGroup) parentView.findViewById(R.id.heard);
        viewGroup.findViewById(R.id.head_right_ibtn).setBackgroundResource(0);
        title_tv= (TextView) parentView.findViewById(R.id.title);
        sub_title_tv = (TextView)parentView.findViewById(R.id.sub_title);

        dragProgressBar = (DragProgressBar)parentView.findViewById(R.id.dragBar);
        dragProgressBar.setOnDragProgressListener(this);
        return parentView;
    }
    @Override
    public void onResume() {
        super.onResume();
        isResumed =true;

        LinearLayout linearLayout = ((MusicActivity)getActivity()).getMainBackground();
        String tag = (String) linearLayout.getTag();
        if(tag==null){
            linearLayout.setBackgroundResource(R.drawable.bg_music);
        }
        songTimerManager = SongTimerManager.getInstance();
        songTimerManager.setOnPlayProgress(this);
        mHopeHandler = HopeJsonHandler.getInstance();
        mHopeHandler.setHandler(mHandler);

        initDeviceState(devicePlayState);

    }

    protected boolean isFragmentVisible() {
        return isResumed && isVisible();
    }
    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }
    /** 设置旋转动画 */
    public void initAnimation(){
        animation =new RotateAnimation(0f,360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(18000);//设置动画持续时间
        animation.setRepeatCount(Integer.MAX_VALUE);
        animation.setRepeatMode(Animation.RESTART);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isStartAnim=true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isStartAnim=false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                isStartAnim=true;
            }
        });
    }

    /**
     *   停止动画
     */
    public void stopRoatAnimation(){
        if(animation!=null){
            animation.cancel();
        }
    }
    /**
     *  开始动画
     */
    public void startRoatAnimation(View imageView){
        if (animation!=null){
            if(isStartAnim){
                return;
            }
            imageView.setAnimation(animation);
            animation.startNow();
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            stopRoatAnimation();
            LinearLayout linearLayout = ((MusicActivity)getActivity()).getMainBackground();
            linearLayout.setBackgroundColor(Color.parseColor("#31c37c"));
        }
        else{
            LinearLayout linearLayout = ((MusicActivity)getActivity()).getMainBackground();
            String tag = (String) linearLayout.getTag();
            if(tag==null){
                linearLayout.setBackgroundResource(R.drawable.bg_music);
            }
            if (songTimerManager!=null) {
                songTimerManager.setOnPlayProgress(this);
            }
            mHopeHandler = HopeJsonHandler.getInstance();
            mHopeHandler.setHandler(mHandler);
        }
    }

    /**
     *  传递参数过来
     */
    public void setArg(String deviceId,String token,DevicePlayState devicestate){
        musicManager = MusicManager.getInstance(deviceId,token);
        devicePlayState = devicestate;
    }

    public void setOnCmdSendListener(OnCmdSendListener listener) {
        this.onCmdSendListener = listener;
    }

    /**
     *   过滤unknown
     */
    public void setTitle(String title,String subTitle){
        if(title_tv!=null){
        title_tv.setText(title);}
        if(sub_title_tv!=null) {
            if (subTitle.contains("unknown")) {
                if(isFragmentVisible()) {
                    sub_title_tv.setText(getResources().getString(R.string.unknown));
                }
            } else {
                sub_title_tv.setText(subTitle);
            }
        }

    }
    /**
     *   初始化页面效果
     */
    private void initDeviceState(DevicePlayState devicePlayState){
        if(devicePlayState==null||songTimerManager==null)
            return;
         setTitle(devicePlayState.getTitle(),devicePlayState.getArtist());
        songTimerManager.setcurrentProgress(devicePlayState.getProgress());
        songTimerManager.setTotalProgress(devicePlayState.getDuration());
        dragProgressBar.setMax(devicePlayState.getDuration());
        initMode(devicePlayState.getMode(),false);
        if(devicePlayState.getState().equals(MusicConstant.STATE_2)){
            songTimerManager.reStart();
            startRoatAnimation(cover_img);
            play_pause_btn.setImageResource(R.drawable.pause_selector);
        }
        else{
            songTimerManager.pausePlay();
            stopRoatAnimation();
            play_pause_btn.setImageResource(R.drawable.play_selector);
        }

        dragProgressBar.setRight_title(CommonUtil.timeTran(devicePlayState.getDuration()));

        try {
            //用来判断现在的背景图是否一致
            String tag= (String) cover_img.getTag();
            if(tag!=null){
                if(tag.equals(devicePlayState.getImg())){
                    return;
                }
            }
            cover_img.setTag(devicePlayState.getImg());
            LinearLayout linearLayout = ((MusicActivity)getActivity()).getMainBackground();
            new ImageBlurManager(linearLayout,cover_img).downloadFile(devicePlayState.getImg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *    设置播放模式状态
     */
    private void initMode(String flag,boolean isToast){
        switch (flag){
            case MusicConstant.MODEL_1:
                if (isToast){
                    ToastUtil.showToast(R.string.random);
                }
                mode_ibtn.setImageResource(R.drawable.random_style_selector);
                break;
            case MusicConstant.MODEL_2:
                if (isToast){
                    ToastUtil.showToast(R.string.circle);
                }
                mode_ibtn.setImageResource(R.drawable.cycle_style_selector);
                break;
            case MusicConstant.MODEL_3:
                if (isToast){
                    ToastUtil.showToast(R.string.single);
                }
                mode_ibtn.setImageResource(R.drawable.single_style_selector);
                break;
        }
    }
    @Override
    public void onClick(View view) {

        if(view.getId()==R.id.back){
            ((MusicActivity)getActivity()).geToListFragment();
        }else {
            //如果播放状态为null的话重新请求
            if (devicePlayState == null) {
                getDevicePlayState();
                return;
            }
            switch (view.getId()) {
                case R.id.mode_ibtn:
                    if (devicePlayState == null)
                        return;
                    String mode = devicePlayState.getMode();
                    Song song = new Song();
                    song.setDeviceId(devicePlayState.getDeviceId());
                    if (mode.equals(MusicConstant.MODEL_1)) {
                        song.setMode(MusicConstant.MODEL_2);
                    } else if (mode.equals(MusicConstant.MODEL_2)) {
                        song.setMode(MusicConstant.MODEL_3);
                    } else if (mode.equals(MusicConstant.MODEL_1)) {
                        song.setMode(mode);
                    } else {
                        song.setMode(MusicConstant.MODEL_1);
                    }
                    senCmdMethod(HopeCommandType.HOPECOMMAND_TYPE_MUSICCHANGEMODE, song);
                    break;
                case R.id.play_pause_btn:
                    if (devicePlayState == null)
                        return;
                    String state = devicePlayState.getState();
                    if (state != null) {
                        if (state.equals(MusicConstant.STATE_1)) {
                            senCmdMethod(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAY, devicePlayState);
                        }
                        if (state.equals(MusicConstant.STATE_2)) {
                            senCmdMethod(HopeCommandType.HOPECOMMAND_TYPE_MUSICPAUSE, devicePlayState);
                        }
                    }
                    break;
                case R.id.pre_ibtn:
                    senCmdMethod(HopeCommandType.HOPECOMMAND_TYPE_MUSICPREV, devicePlayState);
                    break;
                case R.id.next_ibtn:
                    senCmdMethod(HopeCommandType.HOPECOMMAND_TYPE_MUSICNEXT, devicePlayState);
                    break;
                case R.id.mute_ibtn:
                    if (devicePlayState != null) {
                        DevicePlayState newBean = new DevicePlayState();
                        newBean.setDeviceId(devicePlayState.getDeviceId());
                        newBean.setCurrentVol("0");
                        senCmdMethod(HopeCommandType.HOPECOMMAND_TYPE_MUSICVOLUMESET, newBean);
                    }
                    break;
                case R.id.menu_btn:
                    if (devicePlayState == null)
                        return;
                    SelectPicPopupWindow popupWindow = new SelectPicPopupWindow(getActivity(), devicePlayState, this, this);
                    //显示窗口
                    basePopupWindow = popupWindow;
                    popupWindow.showAtLocation(parentView.findViewById(R.id.root), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;
                case R.id.set_style_ll:
                    if (devicePlayState == null)
                        return;
                    SelectEffectPopupWindow popupWindow1 = new SelectEffectPopupWindow(getActivity(), devicePlayState, this);
                    basePopupWindow = popupWindow1;
                    popupWindow1.showAtLocation(parentView.findViewById(R.id.root), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;
                case R.id.source_ll:
                    if (devicePlayState == null)
                        return;
                    SelectSourcePopupWindow popupWindow2 = new SelectSourcePopupWindow(getActivity(), devicePlayState, this);
                    basePopupWindow = popupWindow2;
                    popupWindow2.showAtLocation(parentView.findViewById(R.id.root), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    break;
            }
        }
    }

    //重新获取播放器播放的状态
    private void getDevicePlayState(){
        MusicActivity activity = (MusicActivity)getActivity();
        if(activity!=null){
            activity.freshPlayState();
        }

    }

    //发送命令
   private void senCmdMethod(String cmd, DevicePlayState bean){
       if(onCmdSendListener!=null){
           if(bean!=null){
               onCmdSendListener.sendCmd(cmd,true,bean);
           }

       }
   }


    @Override
    public void playProgress(int progress) {
        dragProgressBar.setProgress(progress);
    }

    /**
     * 一首歌曲播放完成
     */
    @Override
    public void playFinsh() {
        if(songTimerManager!=null){
        songTimerManager.stopPlay();}
        dragProgressBar.setProgress(0);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 拖动音乐进度条控制音量
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(devicePlayState!=null) {
            DevicePlayState newBean = new DevicePlayState();
            newBean.setDeviceId(devicePlayState.getDeviceId());
            newBean.setCurrentVol(seekBar.getProgress()/10 + "");
            senCmdMethod(HopeCommandType.HOPECOMMAND_TYPE_MUSICVOLUMESET, newBean);
        }
    }

    /**
     *  点击list的item回掉
     */
    @Override
    public void sendCmd(String cmd, boolean isSelect, DevicePlayState song) {
        senCmdMethod(cmd,song);
    }


    @Override
    public void statusChange(String deviceId, String token) {
        musicManager = MusicManager.getInstance(deviceId,token);
    }

    /**
     *   网络发生错误：断网情况
     */
    @Override
    public void onNetChanged() {
        Message message = new Message();
        message.what = HopeJsonHandler.NET_DISCONNECT;
        mHandler.sendMessage(message);
    }

    /**
     * 设备掉线
     */
    @Override
    public void deviceOffline() {
        Message message = new Message();
        message.what = HopeJsonHandler.DEVICE_DISCONNECT;
        mHandler.sendMessage(message);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        stopRoatAnimation();
        ((MusicActivity)getActivity()).unRegisterStatus(this);
    }

    /**
     * 点击左上角返回按钮
     */
    @Override
    public void onBack() {
        ((MusicActivity)getActivity()).geToListFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestory = true;
        stopRoatAnimation();
        if(mHandler!=null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     *  拖动音乐播放进度条回掉
     */
    @Override
    public void onProgressChange(int position) {
        if(devicePlayState==null)
            return;
        //由于向往未做这个功能，暂时采用直接处理方式
        if(songTimerManager!=null) {
            songTimerManager.setcurrentProgress(position);
        }
        Song song = new Song();
        song.setDeviceId(devicePlayState.getDeviceId());
        song.setProgress(position);
        senCmdMethod(MusicConstant.MUSIC_PLAY_POSITION,song);
    }
}
