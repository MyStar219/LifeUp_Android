package com.orvibo.homemate.device.HopeMusic;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.device.HopeMusic.Bean.DeviceSongBean;
import com.orvibo.homemate.device.HopeMusic.Bean.Song;
import com.orvibo.homemate.device.HopeMusic.Db.SongDao;
import com.orvibo.homemate.device.HopeMusic.listener.OnCmdSendListener;
import com.orvibo.homemate.device.HopeMusic.listener.OnSongChangeListener;
import com.orvibo.homemate.device.HopeMusic.util.HopeJsonHandler;
import com.orvibo.homemate.device.HopeMusic.widget.CircleProgressBar;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.pulltorefresh.ErrorMaskView;
import com.orvibo.homemate.view.custom.pulltorefresh.OnUpDownListener;
import com.orvibo.homemate.view.custom.pulltorefresh.PullListMaskController;
import com.orvibo.homemate.view.custom.pulltorefresh.PullRefreshView;

import java.util.ArrayList;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;
import cn.nbhope.smarthomelib.app.type.HopeCommandType;

/**
 * Created by wuliquan on 2016/5/27.
 */
public class SongListFragment extends BaseFragment implements CircleProgressBar.StateChangeListener, OnSongChangeListener, MusicManager.OnSongListListener,
                                                          SongTimerManager.OnPlayProgress, View.OnClickListener, MusicActivity.ServerStatusListener {
    private final static String TAG = "SongListFragment";
    private PullRefreshView listView;
    private TextView singer_name;
    private TextView song_title;
    private TextView title_tv;
    private TextView sub_title_tv;
    private ImageView song_img;
    private CircleProgressBar progressBar;
    private SongAdapter adapter;
    private SongTimerManager songTimerManager;
    private HopeJsonHandler mHopeHandler;
    private MusicManager musicManager;
    private PullListMaskController mViewController;
    private OnCmdSendListener onCmdSendListener;
    private String mDeviceId;
    private String mToken;
    //用来标识服务器歌曲的总数
    private int remoteTotalSize=0;
    //用来标识本地歌曲的总数
    private int localTotalSize=-1;
    //用来标识加载的页码
    private int pageIndex=1;
    //用来标识每一页加载的数量
    private static int pageSize = 20;

    //用来判断现在是刷新数据还是加载更多
    private int GET_SONG_STATE = -1;
    private static final int FRESH = 0;
    private static final int LOAD  = 1;

    //用来判断点击list的item切换歌曲的动作
    private boolean isClick;
    private long    clickTime;

    //现在平板上的播放状态
    private DevicePlayState devicePlayState;

    private boolean isResumed;

    private boolean isDestory;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(!isAdded() || isDetached()||isDestory){
                LogUtil.w(TAG,"Fragment is destory or isDetached");
                return;
            }

            switch (msg.what) {
                case HopeJsonHandler.CONTROL_FAIL:
                    String result = (String) msg.obj;
                    if(isFragmentVisible()){
                    ToastUtil.showToast(result);}
                    break;
                case HopeJsonHandler.NET_DISCONNECT:
                    adapter.setPause();
                    progressBar.setChecked(false);
                    if(songTimerManager!=null){
                        songTimerManager.pausePlay();}
                    if(isFragmentVisible()) {
                        ToastUtil.showToast(getResources().getString(R.string.NET_DISCONNECT));
                    }
                    break;
                case HopeJsonHandler.DEVICE_DISCONNECT:
                    adapter.setPause();
                    progressBar.setChecked(false);
                    if(songTimerManager!=null){
                    songTimerManager.pausePlay();}
                    if(isFragmentVisible()) {
                        ToastUtil.showToast(getResources().getString(R.string.hope_device_offline));
                    }
                    break;
                case HopeJsonHandler.MSG_HANDLE_INIT_STATE:
                    devicePlayState = (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    break;
                case HopeJsonHandler.MUSIC_PROGRESS:
                    devicePlayState = (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    break;
                case HopeJsonHandler.MSG_HANDLE_MUSIC_PLAYEX:
                    devicePlayState = (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    if (isClick){
                        if (System.currentTimeMillis()-clickTime<3000){
                            isClick=false;
                            getToSongPlayFragment();
                        }
                    }
                    break;
                case HopeJsonHandler.MSG_HANDLE_MUSIC_PLAY:
                    devicePlayState = (DevicePlayState) msg.obj;
                    initDeviceState(devicePlayState);
                    break;
                case HopeJsonHandler.MSG_HANDLE_MUSIC_PAUSE:
                    adapter.setPause();
                       if(songTimerManager!=null) {
                           songTimerManager.pausePlay();
                       }
                    progressBar.setChecked(false);
                    break;


            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        songTimerManager = SongTimerManager.getInstance();
        songTimerManager.initPlay();


        ((MusicActivity)getActivity()).registerStatus(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.activity_music_list, container, false);
        singer_name=(TextView)parentView.findViewById(R.id.singer_name);
        song_title=(TextView)parentView.findViewById(R.id.song_title);
        progressBar=(CircleProgressBar)parentView.findViewById(R.id.progressBar);
        song_img =(ImageView)parentView.findViewById(R.id.song_img);
        progressBar.setStateChangeListener(this);
        listView= (PullRefreshView) parentView.findViewById(R.id.song_list);
        ErrorMaskView maskView = (ErrorMaskView) parentView.findViewById(R.id.maskView);
        mViewController = new PullListMaskController(listView,maskView);
        adapter = new SongAdapter(getActivity(),new ArrayList<Song>());
        adapter.setOnSongChangeListener(this);
        listView.setAdapter(adapter);
        parentView.findViewById(R.id.play_rl).setOnClickListener(this);
        parentView.findViewById(R.id.heard).setBackgroundColor(Color.parseColor("#31c37c"));
        ViewGroup viewGroup= (ViewGroup) parentView.findViewById(R.id.heard);
        viewGroup.findViewById(R.id.head_right_ibtn).setBackgroundResource(R.drawable.set_selector);
        parentView.findViewById(R.id.back).setOnClickListener(this);
        viewGroup.findViewById(R.id.head_right_ibtn).setOnClickListener(this);
        title_tv= (TextView) parentView.findViewById(R.id.title);
        sub_title_tv = (TextView)parentView.findViewById(R.id.sub_title);

        initListener();
        return parentView;
    }

    private void initListener(){
        listView.setOnUpDownListener(new OnUpDownListener() {
            @Override
            public void onScrollUp() {
                if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
                    if(localTotalSize>=remoteTotalSize){
                        GET_SONG_STATE = LOAD;
                        if(musicManager!=null) {
                            musicManager.getSong(pageIndex + "", pageSize + "");
                        }
                    }
                }

            }

            @Override
            public void onScrollDown() {
            }
        });

        mViewController.setOnLoadMoreListener(new PullRefreshView.OnClickFootViewListener() {
            @Override
            public void onClickFootView() {
                if(musicManager!=null) {
                    if(localTotalSize>=remoteTotalSize){

                    }else {
                        GET_SONG_STATE = LOAD;
                        musicManager.getSong(pageIndex + "", pageSize + "");
                    }
                }
            }
        });

        mViewController.setOnRefreshListener(new PullRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(musicManager!=null) {
                    GET_SONG_STATE = FRESH;
                    musicManager.getSong("1", pageSize + "");
                }else{
                    if(isFragmentVisible()){
                    ToastUtil.showToast(getResources().getString(R.string.canot_get_song));}
                    mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
                    mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
                }
            }
        });
    }

    public void setTitle(String title,String subTitle){
        title_tv.setText(title);
        sub_title_tv.setText(subTitle);
    }
    @Override
    public void onResume() {
        super.onResume();
        isResumed =true;
        if(songTimerManager!=null) {
            songTimerManager.setOnPlayProgress(this);
        }
        mHopeHandler = HopeJsonHandler.getInstance();
        mHopeHandler.setHandler(mHandler);

    }

    protected boolean isFragmentVisible() {
        return isResumed && isVisible();
    }
    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){}
        else{
            ((MusicActivity)getActivity()).setMainBackground(Color.parseColor("#31c37c"));
            if(songTimerManager!=null) {
                songTimerManager.setOnPlayProgress(this);
            }
            mHopeHandler = HopeJsonHandler.getInstance();
            mHopeHandler.setHandler(mHandler);
        }
    }

    /**
     * 刷新状态
     * @param devicePlayState
     */
    private void initDeviceState(DevicePlayState devicePlayState){

        if(isDestory||devicePlayState==null){
            return;
        }
        adapter.setPlayIndex(Integer.parseInt(devicePlayState.getIndex()));
        if(songTimerManager!=null) {
            songTimerManager.setcurrentProgress(devicePlayState.getProgress());
            songTimerManager.setTotalProgress(devicePlayState.getDuration());
        }
        progressBar.setMax(devicePlayState.getDuration());
        if(devicePlayState.getArtist().contains("unknown")){
            if(isFragmentVisible()) {
                singer_name.setText(getResources().getString(R.string.unknown));
            }
        }else{
            singer_name.setText(devicePlayState.getArtist());
        }

        song_title.setText(devicePlayState.getTitle());

        if(devicePlayState.getState().equals(MusicConstant.STATE_2)){
            if(songTimerManager!=null) {
                songTimerManager.reStart();
            }
            progressBar.setChecked(true);
            adapter.setPaly();
        }
        else{
            progressBar.setChecked(false);
            if(songTimerManager!=null) {
            songTimerManager.pausePlay();}
            adapter.setPause();

        }
        try {
            String tag = (String) song_img.getTag();
            if(tag!=null){
                if (tag.equals(devicePlayState.getImg())){
                    return;
                }
            }
            new ImageBlurManager(song_img).downloadFile(devicePlayState.getImg());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 点击了页面上的播放、暂停
     * @param isCheck
     */
    @Override
    public void stateChange(boolean isCheck) {

        if(onCmdSendListener!=null){
            if(isCheck){
                onCmdSendListener.sendCmd(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAY,isCheck,adapter.getSongSelect());
            }else{
                onCmdSendListener.sendCmd(HopeCommandType.HOPECOMMAND_TYPE_MUSICPAUSE,isCheck,adapter.getSongSelect());
            }

        }
    }

    /**
     * 点击list 的item回掉方法
     * @param cmd
     * @param isSelect
     * @param song
     */
    @Override
    public void change(String cmd, boolean isSelect, Song song) {
           if(onCmdSendListener!=null){
               long currentTime = System.currentTimeMillis();
               if (currentTime-clickTime>500){
                   isClick=true;
                   clickTime = currentTime;
                   onCmdSendListener.sendCmd(cmd,isSelect,song);
               }
           }
    }

    /**
     * 刷新歌曲回掉
     * @param result
     * @param bean
     * @param data
     */
    @Override
    public void onDeviceStateChange(boolean result,DeviceSongBean bean,String data) {
       if(result&&adapter!=null&&bean!=null){

           if(localTotalSize==-1){
               localTotalSize=0;
           }
           remoteTotalSize=bean.getTotal();
           if(GET_SONG_STATE==FRESH){
               pageIndex=2;
               localTotalSize=0;
               adapter.freshSongList(bean.getSongList());
               mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
           }else if(GET_SONG_STATE==LOAD){
               pageIndex++;
               adapter.loadMoreList(bean.getSongList());
           }
           localTotalSize+=bean.getSongList().size();
           if(isFragmentVisible()) {
               setTitle(getResources().getString(R.string.my_songs), bean.getTotal() + getResources().getString(R.string.songs));
           }
           if(localTotalSize>=remoteTotalSize){
               mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
               mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
           }else{

               mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NORMAL_HAS_MORE);
           }


           try {
               ((MusicActivity)getActivity()).freshPlayState();
           }catch (Exception e){
               e.printStackTrace();
           }
       }else{

           if(GET_SONG_STATE==LOAD){
               if(isFragmentVisible()) {
                   ToastUtil.showToast(getResources().getString(R.string.no_more_songs));
               }
           }
           else{
               ToastUtil.showToast(data);
           }

           mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_REFRESH_COMPLETE);
           //如果本地一次都没有加载成功,则认为加载不成功，加载本地历史记录
           if(localTotalSize==-1){
               mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
               adapter.freshSongList(getLocalSong());
           }
           //如果本地加载的小于
           else if(localTotalSize!=-1&(localTotalSize<remoteTotalSize)){
               mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NORMAL_HAS_MORE);
           }
           //（没有数据的情况下）其他情况都不再加载
           else{
               mViewController.showViewStatus(PullListMaskController.ListViewState.LIST_NO_MORE);
               mViewController.getmListView().setSelection(mViewController.getmListView().getGlobalFirstVisibleItem());
           }
       }
    }

    /**
     * 设置listener
     * 用来发送命令
     * @param listener
     */
    public void setOnCmdSendListener(OnCmdSendListener listener) {
        this.onCmdSendListener = listener;
    }

    /**
     * 每隔1s调用一次，用来刷新歌曲进度条
     * @param progress
     */
    @Override
    public void playProgress(int progress) {
        progressBar.setProgress(progress);
    }

    /**
     * 一首歌曲播放完成回掉
     */
    @Override
    public void playFinsh() {
        if(songTimerManager!=null) {
            songTimerManager.stopPlay();
        }
        progressBar.finsh();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                release();
                break;
            case R.id.play_rl:
                //本地没有数据就不调到播放页面
                if (localTotalSize<=0)
                    return;
                getToSongPlayFragment();
                break;
            case R.id.head_right_ibtn:
                ((MusicActivity)getActivity()).goToSetDeviceActivity();
                break;

        }
    }

    /**
     * 跳转奥音乐播放页面
     */
    private void getToSongPlayFragment(){
        Bundle bundle = new Bundle();
        bundle.putString("deviceId",mDeviceId);
        bundle.putString("token",mToken);
        bundle.putSerializable("devicePlayState",devicePlayState);
        ((MusicActivity)getActivity()).goToPlayFragment(bundle);
    }

    /**
     * 当重新登陆后回掉
     * 重新登陆向往服务器，获取新的token
     * @param deviceId
     * @param token
     */
    @Override
    public void statusChange(String deviceId,String token) {
          mDeviceId  =deviceId;
          mToken = token;
          musicManager = MusicManager.getInstance(mDeviceId,mToken);
        if(musicManager!=null) {
            musicManager.registerListener(this);
            GET_SONG_STATE = FRESH;
            pageIndex=1;
            musicManager.getSong(pageIndex + "", pageSize + "");
        }
        ((MusicActivity)getActivity()).judgeIsHaveDevcie();
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
     * 设备离线
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
        ((MusicActivity)getActivity()).unRegisterStatus(this);
    }

    /**
     * 点击左上角返回
     */
    @Override
    public void onBack(){
        ArrayList<Song> songs=adapter.getSongList();
        if(songs!=null){
            saveLocalSong(songs);
        }
        release();
    }

    /**
     * 释放资源
     */
    private void release(){
        isDestory = true;
        mHandler.removeCallbacksAndMessages(null);
        if(musicManager!=null) {
            musicManager.releaseInstance();
            musicManager = null;
        }
        if(songTimerManager!=null) {
            songTimerManager.release();
        }
        if(isAdded()){
            ((MusicActivity)getActivity()).finish();
        }
    }

    /**
     * 获取本地数据库数据
     * @return
     */
    private ArrayList<Song> getLocalSong(){
        String userId=UserCache.getCurrentUserId(getActivity());
        SongDao songDao = new SongDao(getActivity());
        return songDao.selItemByTyple(userId,mDeviceId);
    }

    /**
     * 这里采取在退出该页面是才保存数据
     * @param list
     */
    private void saveLocalSong(ArrayList<Song> list){
        if(list!=null&list.size()>0){
            String userId=UserCache.getCurrentUserId(getActivity());
            SongDao songDao = new SongDao(getActivity());
            songDao.deleteDataByUserId(userId);
            songDao.insertSong(userId,list);
        }
    }
}
