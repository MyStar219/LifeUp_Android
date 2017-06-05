package com.orvibo.homemate.device.ys;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.bo.CameraInfo;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.dao.CameraInfoDao;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.manage.edit.DeviceEditActivity;
import com.orvibo.homemate.logcat.LogcatHelper;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.HttpTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.PopupWindowUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.CircleWaveView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;
import com.videogo.camera.CameraInfoEx;
import com.videogo.camera.CameraManager;
import com.videogo.cameramgt.CameraMgtCtrl;
import com.videogo.device.DeviceInfoEx;
import com.videogo.device.DeviceManager;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZConstants.EZRealPlayConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.util.LocalInfo;
import com.videogo.util.SDCardUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;


/**
 * Created by allen on 2015/11/16.
 */
public class YsCameraActivity extends BaseActivity implements SurfaceHolder.Callback, Handler.Callback {
    private static final String TAG = YsCameraActivity.class.getName();
    private NavigationGreenBar titleBar;
    private LinearLayout titleBarLS;
    private Chronometer chronometer;
    private ImageView divideLine, closeTalking;
    private RelativeLayout operate1, operate2, talkingRLLS;
    private ImageView sound, soundLS, talk, talkLS, talkingLS, photo, photoLS, record, recordLS;
    private CircleWaveView circleWaveViewTalking, circleWaveViewTalkingLS;
    private TextView definition, definitionLS, talkText, photoText, recordText;
    private CheckTextButton fullScreenIV, fullScreenIVLS;
    private Device device;
    private EZPlayer mEZPlayer;
    private EZOpenSDK mEZOpenSDK = EZOpenSDK.getInstance();
    private EZCameraInfo mCameraInfo;
    private SurfaceView mRealPlaySv;
    private SurfaceHolder mRealPlaySh;
    private LocalInfo mLocalInfo;
    private StubPlayer mStub = new StubPlayer();
    private PopupWindow mQualityPopupWindow;
    private View popView;
    private Handler mHandler;
    private static final int GET_CAMERA_INFO = 1;
    private static final int TAKE_PHOTO_SAVE_IN_SYSTEM = 2;
    private static final int TAKE_PHOTO_SAVE_IN_ORVIBO = 3;
    private static final int CAPTURE_PICTURE_FAIL = 4;
    private static final int TIME_OUT = 5;
    /**
     * 标识是否正在播放
     */
    private int mStatus = RealPlayStatus.STATUS_INIT;
    // 对讲模式
    private boolean mIsOnTalk = false;
    private boolean mIsOnRecord = false;
    private ScreenOrientationHelper mScreenOrientationHelper;
    /**
     * 屏幕当前方向
     */
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ys_camera);
        Serializable serializable = getIntent().getSerializableExtra(IntentKey.DEVICE);
        if (serializable != null && serializable instanceof Device) {
            device = (Device) serializable;
        }
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mScreenOrientationHelper.postOnStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScreenOrientationHelper.postOnStop();
        saveLastFrame();
        stopRecord();
        stopRealPlay();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        device = new DeviceDao().selDevice(device.getDeviceId());
        titleBar.setText(device.getDeviceName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bitmap lastFrame = getLastFrame();
        if (lastFrame != null) {
            mRealPlaySv.setBackgroundDrawable(new BitmapDrawable(lastFrame));
        }
//        if (mEZPlayer != null) {
//            mEZPlayer.resumePlayback();
//        }
        startRealPlay();
    }

    @Override
    public void onBackPressed() {
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            fullScreenIVLS.performClick();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.sound:
            case R.id.soundLS:
                if (mLocalInfo.isSoundOpen()) {
                    closeSound();
                } else {
                    openSound();
                }
                break;
            case R.id.definition:
                openQualityPopupWindow(definition);
                break;
            case R.id.definitionLS:
                openQualityPopupWindow(definitionLS);
                break;
            case R.id.definitionHigh:
                setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_HD);
                break;
            case R.id.definitionNormal:
                setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED);
                break;
            case R.id.definitionLow:
                setQualityMode(EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET);
                break;
            case R.id.talk:
            case R.id.talkLS:
                if (mIsOnTalk) {
                    stopVoiceTalk();
                } else {
                    startVoiceTalk();
                }
                break;
            case R.id.closeTalking:
            case R.id.talkingLS:
                stopVoiceTalk();
                break;
            case R.id.photo:
            case R.id.photoLS:
                takePhoto();
                break;
            case R.id.record:
            case R.id.recordLS:
                if (mIsOnRecord) {
                    stopRecord();
                } else {
                    startRecord();
                }
                break;
        }
    }

    private void init() {
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        titleBar = (NavigationGreenBar) findViewById(R.id.titleBar);
        titleBar.setText(device.getDeviceName());
        titleBarLS = (LinearLayout) findViewById(R.id.titleBarLS);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
//        chronometer.setFormat("mm:ss");
        divideLine = (ImageView) findViewById(R.id.divideLine);
        closeTalking = (ImageView) findViewById(R.id.closeTalking);
        closeTalking.setOnClickListener(this);
        operate1 = (RelativeLayout) findViewById(R.id.operate1);
        operate2 = (RelativeLayout) findViewById(R.id.operate2);
        talkingRLLS = (RelativeLayout) findViewById(R.id.talkingRLLS);
        sound = (ImageView) findViewById(R.id.sound);
        sound.setOnClickListener(this);
        sound.setEnabled(false);
        soundLS = (ImageView) findViewById(R.id.soundLS);
        soundLS.setOnClickListener(this);
        soundLS.setEnabled(false);
        definition = (TextView) findViewById(R.id.definition);
        definition.setOnClickListener(this);
        definition.setEnabled(false);
        definitionLS = (TextView) findViewById(R.id.definitionLS);
        definitionLS.setOnClickListener(this);
        definitionLS.setEnabled(false);
        fullScreenIV = (CheckTextButton) findViewById(R.id.fullScreenIV);
        fullScreenIVLS = (CheckTextButton) findViewById(R.id.fullScreenIVLS);
        talk = (ImageView) findViewById(R.id.talk);
        talk.setOnClickListener(this);
        talk.setEnabled(false);
        talkLS = (ImageView) findViewById(R.id.talkLS);
        talkLS.setOnClickListener(this);
        talkLS.setEnabled(false);
        talkingLS = (ImageView) findViewById(R.id.talkingLS);
        talkingLS.setOnClickListener(this);
        talkingLS.setEnabled(false);
        photo = (ImageView) findViewById(R.id.photo);
        photo.setOnClickListener(this);
        photo.setEnabled(false);
        photoLS = (ImageView) findViewById(R.id.photoLS);
        photoLS.setOnClickListener(this);
        photoLS.setEnabled(false);
        record = (ImageView) findViewById(R.id.record);
        record.setOnClickListener(this);
        record.setEnabled(false);
        recordLS = (ImageView) findViewById(R.id.recordLS);
        recordLS.setOnClickListener(this);
        recordLS.setEnabled(false);
        talkText = (TextView) findViewById(R.id.talkText);
        talkText.setEnabled(false);
        photoText = (TextView) findViewById(R.id.photoText);
        photoText.setEnabled(false);
        recordText = (TextView) findViewById(R.id.recordText);
        recordText.setEnabled(false);
        circleWaveViewTalking = (CircleWaveView) findViewById(R.id.circleWaveViewTalking);
        circleWaveViewTalking.setWaveColor(getResources().getColor(R.color.green));
        circleWaveViewTalkingLS = (CircleWaveView) findViewById(R.id.circleWaveViewTalkingLS);
        circleWaveViewTalkingLS.setWaveColor(getResources().getColor(R.color.green));
        mRealPlaySv = (SurfaceView) findViewById(R.id.surfaceView);

        popView = View.inflate(this, R.layout.ys_definition_pop, null);
        popView.measure(getResources().getDimensionPixelSize(R.dimen.ys_camera_definition_width), LinearLayout.LayoutParams.WRAP_CONTENT);
        mQualityPopupWindow = new PopupWindow(popView, getResources().getDimensionPixelSize(R.dimen.ys_camera_definition_width), WindowManager.LayoutParams.WRAP_CONTENT);
        PopupWindowUtil.initPopup(mQualityPopupWindow, new BitmapDrawable());
        int height = getResources().getDisplayMetrics().widthPixels * 9 / 16;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.titleBar);
        mRealPlaySv.setLayoutParams(layoutParams);
        mRealPlaySv.getHolder().addCallback(this);
        // 获取配置信息操作对象
        mLocalInfo = LocalInfo.getInstance();
        mHandler = new Handler(this);
        mScreenOrientationHelper = new ScreenOrientationHelper(this, fullScreenIV, fullScreenIVLS);

        showDialogNow();
        getCameraInfo();
        mHandler.sendEmptyMessageDelayed(TIME_OUT, 30 * 1000);
    }

    private void getCameraInfo() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Account account = new AccountDao().selAccountByUserId(UserCache.getCurrentUserId(mContext));
                if (account != null && !TextUtils.isEmpty(account.getPhone())) {
                    String accessToken = HttpTool.getAccessToken(account.getPhone());
                    if (!TextUtils.isEmpty(accessToken)) {
                        EZOpenSDK.getInstance().setAccessToken(accessToken);
                    }
                }
                try {
                    CameraInfo cameraInfo = new CameraInfoDao().selCameraInfoByUid(device.getUid());
                    if (cameraInfo != null) {
                        EZOpenSDK.getInstance().setValidateCode(cameraInfo.getPassword(), cameraInfo.getCameraUid());
                    } else {
                        LogUtil.e(TAG, "getCameraInfo()-cameraInfo is null " + device);
                    }
                    mCameraInfo = EZOpenSDK.getInstance().getCameraInfo(device.getUid());
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(GET_CAMERA_INFO);
            }
        }.start();
    }

    /**
     * 开始播放
     *
     * @since V2.0
     */
    private void startRealPlay() {
        if (mCameraInfo == null) {
            return;
        }
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }
        // 检查网络是否可用
        if (!NetUtil.isNetworkEnable(mAppContext)) {
            // 提示没有连接网络
            ToastUtil.showToast(R.string.network_canot_work, ToastType.NULL, Toast.LENGTH_SHORT);
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;
        LogUtil.d(TAG, "startRealPlay");
        String cameraId = Utils.getCameraId(mCameraInfo.getCameraId());
        mEZPlayer = mEZOpenSDK.createPlayer(this, cameraId);
        mStub.setCameraId(mCameraInfo.getCameraId());////****  mj
        if (mEZPlayer == null) {
            return;
        }
        mEZPlayer.setHandler(mHandler);
        mEZPlayer.setSurfaceHold(mRealPlaySh);

        mEZPlayer.startRealPlay();
    }

    private void handleStartRealPlaySuccess() {
        sound.setEnabled(true);
        soundLS.setEnabled(true);
        definition.setEnabled(true);
        definitionLS.setEnabled(true);
        talk.setEnabled(true);
        talkLS.setEnabled(true);
        talkingLS.setEnabled(true);
        photo.setEnabled(true);
        photoLS.setEnabled(true);
        record.setEnabled(true);
        recordLS.setEnabled(true);
        talkText.setEnabled(true);
        photoText.setEnabled(true);
        recordText.setEnabled(true);
    }

    /**
     * 停止播放
     *
     * @see
     * @since V1.0
     */
    private void stopRealPlay() {
        if (mEZPlayer == null) {
            return;
        }
        LogUtil.d(TAG, "stopRealPlay");
        mStatus = RealPlayStatus.STATUS_STOP;
        mEZPlayer.stopRealPlay();
//        stopUpdateTimer();
//        stopRealPlayRecord();
//        mTotalStreamFlow += mStub.getStreamFlow();
//        mStreamFlow = 0;
    }

    private void openQualityPopupWindow(final View anchor) {
        if (mEZPlayer == null) {
            return;
        }

        TextView definitionHigh = (TextView) popView.findViewById(R.id.definitionHigh);
        definitionHigh.setOnClickListener(this);
        definitionHigh.setTextColor(getResources().getColor(R.color.gray));
        TextView definitionNormal = (TextView) popView.findViewById(R.id.definitionNormal);
        definitionNormal.setOnClickListener(this);
        definitionNormal.setTextColor(getResources().getColor(R.color.gray));
        TextView definitionLow = (TextView) popView.findViewById(R.id.definitionLow);
        definitionLow.setOnClickListener(this);
        definitionLow.setTextColor(getResources().getColor(R.color.gray));

        // 视频质量，2-高清，1-标清，0-流畅
        if (mStub.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            definitionLow.setTextColor(getResources().getColor(R.color.green));
        } else if (mStub.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            definitionNormal.setTextColor(getResources().getColor(R.color.green));
        } else if (mStub.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            definitionHigh.setTextColor(getResources().getColor(R.color.green));
        }

        // 2-2-1 支持低中高，低中为子码流，高为主码流；2-1-0 支持低中，不支持高品质，低为子码流，中为主码流
        String capability = mStub.getCapability();
        String[] quality = capability.split("-");
        try {
            if (Integer.parseInt(quality[0]) == 0) {
                definitionLow.setVisibility(View.GONE);
            } else {
                definitionLow.setVisibility(View.VISIBLE);
            }
            if (Integer.parseInt(quality[1]) == 0) {
                definitionNormal.setVisibility(View.GONE);
            } else {
                definitionNormal.setVisibility(View.VISIBLE);
            }
            if (Integer.parseInt(quality[2]) == 0) {
                definitionHigh.setVisibility(View.GONE);
            } else {
                definitionHigh.setVisibility(View.VISIBLE);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        int yoff;
        if (anchor == definitionLS) {
            yoff = getResources().getDimensionPixelSize(R.dimen.margin_x2);
        } else {
            yoff = -(popView.getMeasuredHeight() + anchor.getMeasuredHeight() + getResources().getDimensionPixelSize(R.dimen.margin_x2));
        }
        mQualityPopupWindow.showAsDropDown(anchor, 0, yoff);

    }

    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null && !isFinishing()) {
            popupWindow.dismiss();
        }
    }

    /**
     * 码流配置 清晰度 2-高清，1-标清，0-流畅
     *
     * @see
     * @since V2.0
     */
    private void setQualityMode(final EZConstants.EZVideoLevel mode) {
        mQualityPopupWindow.dismiss();
        // 检查网络是否可用
        if (!NetUtil.isNetworkEnable(mAppContext)) {
            // 提示没有连接网络
            ToastUtil.showToast(R.string.network_canot_work, ToastType.NULL, Toast.LENGTH_SHORT);
            return;
        }
        stopRecord();
        if (mEZPlayer != null) {
            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    try {
                        mEZPlayer.setVideoLevel(mode);
                        return 0;
                    } catch (BaseException e) {
                        e.printStackTrace();
                        return e.getErrorCode();
                    }
                }

                @Override
                protected void onPostExecute(Integer o) {
                    if (o == 0) {
                        handleSetVideoMode(true, 0);
                    } else {
                        handleSetVideoMode(false, o);
                    }
                }
            }.execute();
        }
    }

    private void handleSetVideoMode(boolean isSuccess, int errorCode) {
        if (isSuccess) {
            setVideoLevelUI();
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                mRealPlaySv.setBackgroundResource(R.drawable.pic_noframe);
                // 停止播放
                stopRealPlay();
                //下面语句防止stopRealPlay线程还没释放surface, startRealPlay线程已经开始使用surface
                //因此需要等待500ms
                SystemClock.sleep(500);
                // 开始播放
                startRealPlay();
            }
        } else {
            Utils.showToast(mAppContext, R.string.realplay_set_vediomode_fail, errorCode);
        }
    }

    private void setVideoLevelUI() {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }
        if (mCameraInfo.getOnlineStatus() == 1 && mStub.getPrivacyStatus() != 1) {
            definition.setEnabled(true);
            definitionLS.setEnabled(true);
        } else {
            definition.setEnabled(false);
            definitionLS.setEnabled(false);
        }
        // 视频质量，2-高清，1-标清，0-流畅
        if (mStub.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            definition.setText(R.string.ys_definition_low);
            definitionLS.setText(R.string.ys_definition_low);
        } else if (mStub.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            definition.setText(R.string.ys_definition_normal);
            definitionLS.setText(R.string.ys_definition_normal);
        } else if (mStub.getVideoLevel() == EZConstants.EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            definition.setText(R.string.ys_definition_high);
            definitionLS.setText(R.string.ys_definition_high);
        }
    }

    private void openSound() {
        if (mEZPlayer == null) {
            return;
        }
        LogUtil.d(TAG, "openSound");
        if (mEZPlayer.openSound()) {
            mLocalInfo.setSoundOpen(true);
            sound.setImageResource(R.drawable.icon_voice);
            soundLS.setImageResource(R.drawable.icon_voice);
        } else {
            mLocalInfo.setSoundOpen(false);
            sound.setImageResource(R.drawable.icon_mute);
            soundLS.setImageResource(R.drawable.icon_mute);
        }
    }

    private void closeSound() {
        if (mEZPlayer == null) {
            return;
        }
        LogUtil.d(TAG, "closeSound");
        if (mEZPlayer.closeSound()) {
            mLocalInfo.setSoundOpen(false);
            sound.setImageResource(R.drawable.icon_mute);
            soundLS.setImageResource(R.drawable.icon_mute);
        } else {
            mLocalInfo.setSoundOpen(true);
            sound.setImageResource(R.drawable.icon_voice);
            soundLS.setImageResource(R.drawable.icon_voice);
        }
    }

    /**
     * 设备对讲
     *
     * @see
     * @since V2.0
     */
    private void startVoiceTalk() {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }
        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            Utils.showToast(YsCameraActivity.this, R.string.remoteplayback_SDCard_disable_use);
            return;
        }
//        mHandler.sendEmptyMessageDelayed(EZRealPlayConstants.MSG_REALPLAY_VOICETALK_SUCCESS, 3000);LogUtil.d(TAG, "startVoiceTalk");
        definition.setEnabled(false);
        definitionLS.setEnabled(false);
        sound.setEnabled(false);
        soundLS.setEnabled(false);
        photo.setVisibility(View.INVISIBLE);
        photoText.setVisibility(View.INVISIBLE);
        record.setVisibility(View.INVISIBLE);
        recordText.setVisibility(View.INVISIBLE);
        talk.setEnabled(false);
        talk.setImageResource(R.drawable.icon_talk);
        talkLS.setEnabled(false);
        talkLS.setImageResource(R.drawable.icon_talk_cross);
        talkText.setText(R.string.start_voice_talk);
        talkingLS.setEnabled(false);
        talkingLS.setImageResource(R.drawable.icon_talk);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            titleBarLS.setVisibility(View.GONE);
            talkingRLLS.setVisibility(View.VISIBLE);
            ToastUtil.showToast(R.string.start_voice_talk);
        }
        closeSound();
        if (!mEZPlayer.startVoiceTalk()) {
            handleVoiceTalkStopped();
        }
    }

    private void handleVoiceTalkSucceed() {
        LogUtil.d(TAG, "handleVoiceTalkSucceed");
        mIsOnTalk = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                circleWaveViewTalking.init();
                circleWaveViewTalkingLS.init();
            }
        }, 20);
        circleWaveViewTalking.setVisibility(View.VISIBLE);
        circleWaveViewTalking.start();
        circleWaveViewTalkingLS.setVisibility(View.VISIBLE);
        circleWaveViewTalkingLS.start();
        talk.setEnabled(true);
        talk.setImageResource(R.drawable.icon_talk_select);
        talkLS.setEnabled(true);
        talkLS.setImageResource(R.drawable.icon_talk_cross_select);
        talkingLS.setEnabled(true);
        talkingLS.setImageResource(R.drawable.icon_talk_select);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            titleBarLS.setVisibility(View.GONE);
            talkingRLLS.setVisibility(View.VISIBLE);
        }
        closeTalking.setVisibility(View.VISIBLE);
        talkText.setText(R.string.talking);
    }

    /**
     * 停止对讲
     *
     * @see
     * @since V2.0
     */
    private void stopVoiceTalk() {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }
//        mHandler.sendEmptyMessageDelayed(EZRealPlayConstants.MSG_REALPLAY_VOICETALK_STOP, 3000);
        LogUtil.d(TAG, "stopVoiceTalk");
        mIsOnTalk = false;
        talk.setEnabled(false);
        talk.setImageResource(R.drawable.icon_talk);
        talkLS.setEnabled(false);
        talkLS.setImageResource(R.drawable.icon_talk_cross);
        talkText.setText(R.string.stop_voice_talk);
        talkingLS.setEnabled(false);
        talkingLS.setImageResource(R.drawable.icon_talk);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ToastUtil.showToast(R.string.stop_voice_talk);
        }
        circleWaveViewTalking.setVisibility(View.GONE);
        circleWaveViewTalking.stop();
        circleWaveViewTalkingLS.setVisibility(View.GONE);
        circleWaveViewTalkingLS.stop();
        closeTalking.setVisibility(View.GONE);
        if (!mEZPlayer.stopVoiceTalk()) {
            handleVoiceTalkStopped();
        }
    }

    private void handleVoiceTalkStopped() {
        LogUtil.d(TAG, "handleVoiceTalkStopped");
        mIsOnTalk = false;
        sound.setEnabled(true);
        soundLS.setEnabled(true);
        definition.setEnabled(true);
        definitionLS.setEnabled(true);
        photo.setVisibility(View.VISIBLE);
        photoText.setVisibility(View.VISIBLE);
        record.setVisibility(View.VISIBLE);
        recordText.setVisibility(View.VISIBLE);
        talk.setEnabled(true);
        talk.setImageResource(R.drawable.icon_talk);
        talkLS.setEnabled(true);
        talkLS.setImageResource(R.drawable.icon_talk_cross_default);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            titleBarLS.setVisibility(View.VISIBLE);
            talkingRLLS.setVisibility(View.GONE);
        }
        talkText.setText(R.string.camera_talk);
        circleWaveViewTalking.setVisibility(View.GONE);
        circleWaveViewTalking.stop();
        circleWaveViewTalkingLS.setVisibility(View.GONE);
        circleWaveViewTalkingLS.stop();
        closeTalking.setVisibility(View.GONE);
        if (mLocalInfo.isSoundOpen()) {
            openSound();
        }
    }

    private Bitmap getLastFrame() {
        String filePath = LogcatHelper.getInstance(mAppContext).getDir(mAppContext) + File.separator + "photo" + File.separator + device.getUid() + "lastFrame.jpg";
        return BitmapFactory.decodeFile(filePath);
    }

    private void saveLastFrame() {
        if (mEZPlayer == null) {
            return;
        }
        Bitmap bmp = mEZPlayer.capturePicture();
        if (bmp == null) {
            return;
        }
        String dirPath = LogcatHelper.getInstance(mAppContext).getDir(mAppContext) + File.separator + "photo";
        File dir = new File(dirPath);
        dir.mkdirs();
        String filePath = dir + File.separator + device.getUid() + "lastFrame.jpg";
        File file = new File(filePath);
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(filePath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBitmap() {

    }

    private void takePhoto() {
        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            Utils.showToast(YsCameraActivity.this, R.string.remoteplayback_SDCard_disable_use);
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (mEZPlayer == null) {
                    return;
                }
                Bitmap bmp = mEZPlayer.capturePicture();
                if (bmp != null) {
                    String url = MediaStore.Images.Media.insertImage(mAppContext.getContentResolver(), bmp, "", "");
                    if (!TextUtils.isEmpty(url)) {
                        Uri uri = Uri.parse(url);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                        mHandler.sendEmptyMessage(TAKE_PHOTO_SAVE_IN_SYSTEM);
                    } else {
                        String dirPath = LogcatHelper.getInstance(mAppContext).getDir(mAppContext) + File.separator + "photo";
                        File dir = new File(dirPath);
                        dir.mkdirs();
                        String filePath = dirPath + File.separator + DateUtil.getNowStr2() + ".jpg";
                        File file = new File(filePath);
                        try {
                            file.createNewFile();
                            FileOutputStream fos = new FileOutputStream(filePath);
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();
                            fos.close();
                            mHandler.sendEmptyMessage(TAKE_PHOTO_SAVE_IN_ORVIBO);
                        } catch (IOException e) {
                            e.printStackTrace();
                            mHandler.sendEmptyMessage(CAPTURE_PICTURE_FAIL);
                        }
                    }
                }
            }
        }.start();
    }

//    FileOutputStream mOs;
//    private EZOpenSDKListener.EZStandardFlowCallback mLocalRecordCb = new EZOpenSDKListener.EZStandardFlowCallback() {
//        @Override
//        public void onStandardFlowCallback(int i, byte[] bytes, int i1) {
//            try {
//                if (mOs == null) {
//                    String dirPath = LogcatHelper.getInstance(mAppContext).getDir(mAppContext) + File.separator + "record";
//                    File dir = new File(dirPath);
//                    dir.mkdirs();
//                    String filePath = dirPath + File.separator + DateUtil.getNowStr2() + ".mp4";
//                    File file = new File(filePath);
//                    mOs = new FileOutputStream(file);
//                }
//                mOs.write(bytes, 0, i1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    };

    private void startRecord() {
        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            Utils.showToast(YsCameraActivity.this, R.string.remoteplayback_SDCard_disable_use);
            return;
        }
        if (mEZPlayer == null) {
            return;
        }
//        mOs = null;
        String dirPath = LogcatHelper.getInstance(mAppContext).getDir(mAppContext) + File.separator + "record";
        File dir = new File(dirPath);
        dir.mkdirs();
        String filePath = dirPath + File.separator + DateUtil.getNowStr2() + ".mp4";
//        File file = new File(filePath);
        mEZPlayer.startLocalRecordWithFile(filePath);
        handleRecordSuccess();
        LogUtil.d(TAG, "startRecord");
//        record.setEnabled(false);
//        recordLS.setEnabled(false);
    }

    private void stopRecord() {
        if (mEZPlayer == null) {
            return;
        }
        mEZPlayer.stopLocalRecord();
        LogUtil.d(TAG, "stopRecord");
        if (mIsOnRecord) {
            Utils.showToast(mAppContext, R.string.ys_record_save);
        }
        chronometer.stop();
        chronometer.setVisibility(View.GONE);
        record.setImageResource(R.drawable.icon_record);
        recordLS.setImageResource(R.drawable.icon_record_ls);
        recordText.setText(R.string.camera_video);
        mIsOnRecord = false;
    }

    /**
     * 开始录像成功
     *
     * @see
     * @since V2.0
     */
    private void handleRecordSuccess() {
        LogUtil.d(TAG, "handleRecordSuccess");
        mIsOnRecord = true;
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        chronometer.setVisibility(View.VISIBLE);
        record.setEnabled(true);
        record.setImageResource(R.drawable.icon_recording);
        recordLS.setEnabled(true);
        recordLS.setImageResource(R.drawable.icon_recording_ls);
        recordText.setText(R.string.ys_recording);

    }

    private void handleRecordFail(int errorCode) {
        LogUtil.d(TAG, "handleRecordFail");
        mIsOnRecord = false;
        Utils.showToast(mAppContext, R.string.remoteplayback_record_fail, errorCode);
    }

    @Override
    public void rightTitleClick(View v) {
        super.rightTitleClick(v);

        Intent intent = new Intent(this, DeviceEditActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        startActivity(intent);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtil.d(TAG, "onConfigurationChanged:" + newConfig.orientation);
        mOrientation = newConfig.orientation;
        dismissPopWindow(mQualityPopupWindow);
        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }


    private void onOrientationChanged() {
        LogUtil.d(TAG, "onOrientationChanged:" + mOrientation);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            fullScreen(true);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.margin_x2);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            chronometer.setLayoutParams(params);
            titleBar.setVisibility(View.GONE);
            divideLine.setVisibility(View.GONE);
            operate1.setVisibility(View.GONE);
            operate2.setVisibility(View.GONE);
            if (mIsOnTalk) {
                talkingRLLS.setVisibility(View.VISIBLE);
            } else {
                titleBarLS.setVisibility(View.VISIBLE);
            }
            mRealPlaySv.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        } else {
            fullScreen(false);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_x2);
            params.addRule(RelativeLayout.BELOW, R.id.titleBar);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            chronometer.setLayoutParams(params);
            titleBar.setVisibility(View.VISIBLE);
            divideLine.setVisibility(View.VISIBLE);
            operate1.setVisibility(View.VISIBLE);
            operate2.setVisibility(View.VISIBLE);
            talkingRLLS.setVisibility(View.GONE);
            titleBarLS.setVisibility(View.GONE);
            int height = getResources().getDisplayMetrics().widthPixels * 9 / 16;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.titleBar);
            mRealPlaySv.setLayoutParams(layoutParams);
        }
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

//    private void setOrientation(int sensor) {
//        LogUtil.d(TAG, "setOrientation mForceOrientation:" + mForceOrientation);
//        if (mForceOrientation != 0) {
//            return;
//        }
//
//        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
//            mScreenOrientationHelper.enableSensorOrientation();
//        else
//            mScreenOrientationHelper.disableSensorOrientation();
//    }
//
//    public void setForceOrientation(int orientation) {
//        LogUtil.d(TAG, "setForceOrientation");
//        if (mForceOrientation == orientation) {
//            return;
//        }
//        mForceOrientation = orientation;
//        if (mForceOrientation != 0) {
//            if (mForceOrientation != mOrientation) {
//                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
//                    mScreenOrientationHelper.portrait();
//                } else {
//                    mScreenOrientationHelper.landscape();
//                }
//            }
//            mScreenOrientationHelper.disableSensorOrientation();
//        } else {
//            updateOrientation();
//        }
//    }

//    private void updateOrientation() {
//        LogUtil.d(TAG, "updateOrientation");
//        if (mIsOnTalk) {
//            if (mEZPlayer != null && mStub.getSupportTalk() == EZRealPlayConstants.TALK_FULL_DUPLEX) {
//                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//            } else {
//                setForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            }
//        } else {
//            if (mStatus == RealPlayStatus.STATUS_PLAY) {
//                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//            } else {
//                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
//                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                } else {
//                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//                }
//            }
//        }
//    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case GET_CAMERA_INFO:
                startRealPlay();
                break;
            case EZRealPlayConstants.MSG_GET_CAMERA_INFO_SUCCESS:
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                break;
            case EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                break;
            case EZRealPlayConstants.MSG_REALPLAY_CONNECTION_SUCCESS:
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                mHandler.removeCallbacksAndMessages(null);
                dismissDialog();
                mScreenOrientationHelper.enableSensorOrientation();
                handleStartRealPlaySuccess();
                setVideoLevelUI();
                mStatus = RealPlayStatus.STATUS_PLAY;
                mRealPlaySv.setBackgroundResource(0);
//                if (mIsOnTalk) {
//                    startVoiceTalk();
//                } else {
                if (mLocalInfo.isSoundOpen()) {
                    openSound();
                } else {
                    closeSound();
                }
//                }
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                mHandler.removeCallbacksAndMessages(null);
                dismissDialog();
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    Utils.showToast(mAppContext, R.string.camera_connect_fail);
                }
                mStatus = RealPlayStatus.STATUS_STOP;
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:
                handleSetVideoMode(true, 0);
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:
                handleSetVideoMode(false, msg.arg1);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_SUCCESS:
                handleVoiceTalkSucceed();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_STOP:
                handleVoiceTalkStopped();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_FAIL:
                handleVoiceTalkStopped();
                break;
            case TAKE_PHOTO_SAVE_IN_SYSTEM:
                ToastUtil.showToast(getString(R.string.camera_photo_success));
                break;
            case TAKE_PHOTO_SAVE_IN_ORVIBO:
                ToastUtil.showToast(getString(R.string.camera_photo_success_save_in_orvibo));
                break;
            case CAPTURE_PICTURE_FAIL:
                // 提示抓图失败
                Utils.showToast(mAppContext, R.string.camera_photo_fail);
                break;
            case TIME_OUT:
                dismissDialog();
                mStatus = RealPlayStatus.STATUS_STOP;
                Utils.showToast(mAppContext, R.string.camera_connect_fail);
                break;
            case EZRealPlayConstants.MSG_START_RECORD_SUCCESS:
                handleRecordSuccess();
                break;
            case EZRealPlayConstants.MSG_START_RECORD_FAIL:
                handleRecordFail(msg.arg1);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_OP_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_HARDWARE, this, null);
//                SecureValidate.secureValidateDialog(this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }

    private class StubPlayer {
        private DeviceInfoEx mDeviceInfoEx = null;
        private CameraInfoEx mCameraInfoEx = null;

        public void setCameraId(final String cameId) {
            final Object lock = new Object();
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    String cameraId = Utils.getCameraId(cameId);
                    CameraInfoEx cameraInfoEx = CameraManager.getInstance().getAddedCameraById(cameraId);
                    if (cameraInfoEx == null) {
                        try {
                            CameraMgtCtrl.getCameraDetail(cameraId);
                            cameraInfoEx = CameraManager.getInstance().getAddedCameraById(cameraId);
                        } catch (BaseException e) {
                            e.printStackTrace();
//                            LogUtil.infoLog(TAG, "startRealPlayTask: " + " Thread exist!");
                            return;
                        }
                    }

                    setCameraInfo(cameraInfoEx);

                    DeviceInfoEx deviceInfoEx = null;
                    try {
                        deviceInfoEx = DeviceManager.getInstance().getDeviceInfoExById(cameraInfoEx.getDeviceID());
                    } catch (InnerException e) {
                        e.printStackTrace();
                    }
                    setDeviceInfo(deviceInfoEx);

                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            };
            Thread thread = new Thread(run);
            thread.start();

            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setCameraInfo(CameraInfoEx cameraInfo) {
            mCameraInfoEx = cameraInfo;
        }

        public void setDeviceInfo(DeviceInfoEx deviceInfo) {
            mDeviceInfoEx = deviceInfo;
        }

        public String getCapability() {
            return mCameraInfoEx.getCapability();
        }

        public int getSupportTalk() {
            return mDeviceInfoEx.getSupportTalk();
        }

        public long getStreamFlow() {
            //return mEZMediaPlayerWrapper.getStreamFlow();
            return 0l;
        }

        public int getSupportPtzLeftRight() {
            return mDeviceInfoEx.getSupportPtzLeftRight();
        }

        public int getVideoLevel() {
            return mCameraInfoEx.getVideoLevel();
        }

        // 云台上下
        public int getSupportPtzTopBottom() {
            return mDeviceInfoEx.getSupportPtzTopBottom();
        }

        // 云台Zoom
        public int getSupportPtzZoom() {
            return mDeviceInfoEx.getSupportPtzZoom();
        }

        public int getPrivacyStatus() {
            return 0;
        }

        public int getSupportSsl() {
            return 0;
        }

        public void switchOperateTask(final Handler handler, final int status, final int enType) {

        }
    }
}
