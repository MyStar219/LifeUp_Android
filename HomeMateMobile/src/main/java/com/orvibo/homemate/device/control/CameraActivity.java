package com.orvibo.homemate.device.control;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.camera.CallbackService;
import com.orvibo.homemate.camera.CamObj;
import com.orvibo.homemate.camera.IAVListener;
import com.orvibo.homemate.camera.TouchedView;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.manage.SetDeviceActivity;
import com.orvibo.homemate.util.AnimationHelper;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.FileUtils;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.VibratorUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.p2p.AlarmBean;
import com.p2p.MSG_CONNECT_STATUS;
import com.p2p.SEARCH_RESP;
import com.p2p.SEP2P_Define;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Allen on 2015/4/8.
 * Modified by Smagret on 2015/06/16
 * 添加摄像头全屏
 */
public class CameraActivity extends BaseActivity implements CallbackService.ILANSearch, NavigationCocoBar.OnRightClickListener,
        IAVListener, CallbackService.IEvent, View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, TouchedView.OnRequestLayoutListener {
    private final String TAG = CameraActivity.class.getSimpleName();
    private NavigationCocoBar nb_title;
    private LinearLayout      control_ll;
    private LinearLayout      fullscreen_ll;
    public static final int           MAX_NUM_CAM_OBJ = 1;
    private             TouchedView[] m_arrViewLive   = new TouchedView[MAX_NUM_CAM_OBJ];
    private LinearLayout llDisconnectedTips;
    private ImageView    ivFullscreen, ivNotFullscreen, ivUp, ivLeft, ivRight, ivDown;
    private TextView tvConnect, tvScreenshot, tvTalk, tvAudio;
    private TextView tvConnectFullscreen, tvScreenshotFullscreen, tvTalkFullscreen, tvAudioFullscreen;
    public CamObj[]  m_arrObjCam = new CamObj[MAX_NUM_CAM_OBJ];
    public AlarmBean alermBean   = null;
    private static String STR_DID, STR_USER, STR_PWD;
    private Device device;

    private boolean isconnect = false;
    private boolean isvideo   = false;
    private boolean isaudio   = false;
    private boolean isrecord  = false;
    private boolean istalk    = false;
//    private boolean isBack = false;

    /**
     * 摄像头是否一直动作
     */
    private boolean isAction = false;

    private final int    ALERMPARAMS = 3;
    private       String strMsg      = "";

    private static final int PTZ_ONE_STEP_TIME_MS = 500;
    private static final int WHAT_ptzStop         = 213;
    private static final int WHAT_ptzUp           = 214;
    private static final int WHAT_ptzDown         = 215;
    private static final int WHAT_ptzLeft         = 216;
    private static final int WHAT_ptzRight        = 217;

    /**
     * true 竖屏；false横屏
     */
    private boolean sensor_flag  = true;
    /**
     * true 竖屏；false横屏
     */
    private boolean stretch_flag = true;
    private SensorManager             sm;
    private OrientationSensorListener listener;
    private Sensor                    sensor;

    private SensorManager              sm1;
    private Sensor                     sensor1;
    private OrientationSensorListener2 listener1;

    private Animation fadeInFullscreen_ll;
    private Animation fadeInIvFullscreen;
    private Animation fadeInIvNotFullscreen;
    private Animation fadeOutFullscreen_ll;
    private Animation fadeOutIvFullscreen;
    private Animation fadeOutIvNotFullscreen;

    private static final int CHANGE_FULL_SCREEN        = 888;
    private static final int FULL_SCREEN_HIND_ANIM     = 666;
    private static final int NOT_FULL_SCREEN_HIND_ANIM = 777;
    private int screenWidth;
    private int screenHeight;

    /**
     * 全屏时，动画是否正在执行 true 执行； false 执行完毕或没执行
     */
    private boolean FULL_SCREEN_IS_ANIM = false;

    /**
     * 全屏时，按钮是否显示 true 显示； false 隐藏
     */
    private boolean FULL_SCREEN_IS_SHOW = true;

    /**
     * 非全屏时，动画是否正在执行 true 执行； false 执行完毕或没执行
     */
    private boolean NOT_FULL_SCREEN_IS_ANIM = false;

    /**
     * 非全屏时，按钮是否显示 true 显示； false 隐藏
     */
    private boolean NOT_FULL_SCREEN_IS_SHOW = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        setContentView(R.layout.activity_camera);
        findViews();
        init();

        initAnim();
        for (CamObj cam : m_arrObjCam) {
            cam = null;
        }
        connect();
    }

    protected void onResume() {
        super.onResume();
        initSensor();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!isconnect) {
            isvideo = false;
            connect();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        LogUtil.w(TAG, "onTrimMemory()-level:" + level);
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_COMPLETE
                || level == TRIM_MEMORY_MODERATE
                || level == TRIM_MEMORY_BACKGROUND
                || level == TRIM_MEMORY_UI_HIDDEN) {
            dismissDialog();
            isconnect = true;//调用connect时，如果当前isconnect为true，connect函数才会断开连接
            isvideo = true;
            //处于后台，内存不足
            connect();
        }
    }

    private void findViews() {
        nb_title = (NavigationCocoBar) findViewById(R.id.nbTitle);
        control_ll = (LinearLayout) findViewById(R.id.control_ll);
        fullscreen_ll = (LinearLayout) findViewById(R.id.Fullscreen_ll);
        m_arrViewLive[0] = (TouchedView) findViewById(R.id.touchedView);
        m_arrViewLive[0].registerRequestLayoutListener(this);
        llDisconnectedTips = (LinearLayout) findViewById(R.id.llDisconnectedTips);
        ivFullscreen = (ImageView) findViewById(R.id.ivFullscreen);
        ivNotFullscreen = (ImageView) findViewById(R.id.ivNotFullscreen);
        ivUp = (ImageView) findViewById(R.id.ivUp);
        ivLeft = (ImageView) findViewById(R.id.ivLeft);
        ivRight = (ImageView) findViewById(R.id.ivRight);
        ivDown = (ImageView) findViewById(R.id.ivDown);

        tvConnect = (TextView) findViewById(R.id.tvConnect);
        tvScreenshot = (TextView) findViewById(R.id.tvScreenshot);
        tvTalk = (TextView) findViewById(R.id.tvTalk);
        tvAudio = (TextView) findViewById(R.id.tvAudio);

        tvConnectFullscreen = (TextView) findViewById(R.id.tvConnectFullscreen);
        tvScreenshotFullscreen = (TextView) findViewById(R.id.tvScreenshotFullscreen);
        tvTalkFullscreen = (TextView) findViewById(R.id.tvTalkFullscreen);
        tvAudioFullscreen = (TextView) findViewById(R.id.tvAudioFullscreen);
        nb_title.setOnRightClickListener(this);
        nb_title.setRightDrawableLeft(getResources().getDrawable(R.drawable.setting_white_selector));
    }

    private void init() {
        CamObj.initAPI();

        m_arrObjCam[0] = new CamObj(); // 先只处理1个Camera
        m_arrObjCam[0].regAVListener(this);
        alermBean = new AlarmBean();

        Intent intent = new Intent();
        intent.setClass(this, CallbackService.class);
        startService(intent);
        CallbackService.setLANSearchInterface(this);
        CallbackService.setEventInterface(this); // add new code

        LogUtil.d(TAG, "version = " + CamObj.getAPIVer());

        device = (Device) getIntent().getSerializableExtra("device");
        nb_title.setCenterText(device.getDeviceName());


        STR_DID = device.getExtAddr();
        STR_USER = "admin";
        STR_PWD = "123456";

        ivFullscreen.setOnClickListener(this);
        ivNotFullscreen.setOnClickListener(this);
        ivUp.setOnClickListener(this);
        ivLeft.setOnClickListener(this);
        ivRight.setOnClickListener(this);
        ivDown.setOnClickListener(this);
        tvConnect.setOnClickListener(this);
        tvScreenshot.setOnClickListener(this);
        tvTalk.setOnClickListener(this);
        tvAudio.setOnClickListener(this);
        m_arrViewLive[0].setOnClickListener(this);

        tvConnectFullscreen.setOnClickListener(this);
        tvScreenshotFullscreen.setOnClickListener(this);
        tvTalkFullscreen.setOnClickListener(this);
        tvAudioFullscreen.setOnClickListener(this);

        ivUp.setOnLongClickListener(this);
        ivLeft.setOnLongClickListener(this);
        ivRight.setOnLongClickListener(this);
        ivDown.setOnLongClickListener(this);

        ivUp.setOnTouchListener(this);
        ivLeft.setOnTouchListener(this);
        ivRight.setOnTouchListener(this);
        ivDown.setOnTouchListener(this);

        m_arrViewLive[0].setImageBitmap(getLastFrame());
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        Rect viewRect = new Rect();
//        m_arrViewLive[0].getGlobalVisibleRect(viewRect);
//        int[] p = AppTool.getScreenPixels(CameraActivity.this);
//        LogUtil.d(TAG,"onWindowFocusChanged() - viewRect:" + viewRect + " p[0]:" + p[0] + " p[1]:" + p[1]);
//    }

    private void initSensor() {
        unregisterSensor(sm, listener, sensor);
        unregisterSensor(sm1, listener1, sensor1);
        listener = null;
        listener1 = null;

        //注册重力感应器  屏幕旋转
        if (sm == null) {
            sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensor == null) {
            sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        listener = new OrientationSensorListener();
//        listener = new OrientationSensorListener(mHandler);
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);


        //根据  旋转之后 点击 符合之后 激活sm
        if (sm1 == null) {
            sm1 = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensor1 == null) {
            sensor1 = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        listener1 = new OrientationSensorListener2();
        sm1.registerListener(listener1, sensor1, SensorManager.SENSOR_DELAY_UI);
    }

    private void initAnim() {
        fadeInFullscreen_ll = AnimationHelper.createHideRightTranslateInAnimation(fullscreen_ll);
        fadeInIvFullscreen = AnimationHelper.createHideRightTranslateInAnimation(ivFullscreen);
        fadeInIvNotFullscreen = AnimationHelper.createShowRightTranslateInAnimation(ivNotFullscreen);
        fadeInFullscreen_ll.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int left = fullscreen_ll.getLeft();
                int top = fullscreen_ll.getTop();
                int width = fullscreen_ll.getWidth();
                int height = fullscreen_ll.getHeight();
                fullscreen_ll.clearAnimation();
                fullscreen_ll.layout(left + width, top, left + 2 * width, top + height);
                FULL_SCREEN_IS_ANIM = false;
                FULL_SCREEN_IS_SHOW = false;
            }
        });

        fadeInIvFullscreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int left = ivFullscreen.getLeft();
                int top = ivFullscreen.getTop();
                int width = ivFullscreen.getWidth();
                int height = ivFullscreen.getHeight();
                ivFullscreen.clearAnimation();
                ivFullscreen.layout(left + width, top, left + 2 * width, top + height);
                NOT_FULL_SCREEN_IS_ANIM = false;
                NOT_FULL_SCREEN_IS_SHOW = false;
            }
        });

        fadeInIvNotFullscreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int left = ivNotFullscreen.getLeft();
                int top = ivNotFullscreen.getTop();
                int width = ivNotFullscreen.getWidth();
                int height = ivNotFullscreen.getHeight();
                ivNotFullscreen.clearAnimation();
                ivNotFullscreen.layout(left - width, top, left, top + height);
                FULL_SCREEN_IS_ANIM = false;
                FULL_SCREEN_IS_SHOW = false;
            }
        });

        fadeOutFullscreen_ll = AnimationHelper.createShowRightTranslateInAnimation(fullscreen_ll);
        fadeOutIvFullscreen = AnimationHelper.createShowRightTranslateInAnimation(ivFullscreen);
        fadeOutIvNotFullscreen = AnimationHelper.createHideRightTranslateInAnimation(ivNotFullscreen);
        fadeOutFullscreen_ll.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int left = fullscreen_ll.getLeft();
                int top = fullscreen_ll.getTop();
                int width = fullscreen_ll.getWidth();
                int height = fullscreen_ll.getHeight();
                fullscreen_ll.clearAnimation();
                fullscreen_ll.layout(left - width, top, left, top + height);
                FULL_SCREEN_IS_ANIM = false;
                FULL_SCREEN_IS_SHOW = true;
                if (mHandler != null && mHandler.hasMessages(FULL_SCREEN_HIND_ANIM)) {
                    mHandler.removeMessages(FULL_SCREEN_HIND_ANIM);
                }
                mHandler.sendEmptyMessageDelayed(FULL_SCREEN_HIND_ANIM, 3000);
            }
        });

        fadeOutIvFullscreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int left = ivFullscreen.getLeft();
                int top = ivFullscreen.getTop();
                int width = ivFullscreen.getWidth();
                int height = ivFullscreen.getHeight();
                ivFullscreen.clearAnimation();
                ivFullscreen.layout(left - width, top, left, top + height);
                NOT_FULL_SCREEN_IS_ANIM = false;
                NOT_FULL_SCREEN_IS_SHOW = true;
                if (mHandler != null && mHandler.hasMessages(NOT_FULL_SCREEN_HIND_ANIM)) {
                    mHandler.removeMessages(NOT_FULL_SCREEN_HIND_ANIM);
                }
                mHandler.sendEmptyMessageDelayed(NOT_FULL_SCREEN_HIND_ANIM, 3000);
            }
        });

        fadeOutIvNotFullscreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int left = ivNotFullscreen.getLeft();
                int top = ivNotFullscreen.getTop();
                int width = ivNotFullscreen.getWidth();
                int height = ivNotFullscreen.getHeight();
                ivNotFullscreen.clearAnimation();
                ivNotFullscreen.layout(left + width, top, left + 2 * width, top + height);
                FULL_SCREEN_IS_ANIM = false;
                FULL_SCREEN_IS_SHOW = true;
                if (mHandler != null && mHandler.hasMessages(FULL_SCREEN_HIND_ANIM)) {
                    mHandler.removeMessages(FULL_SCREEN_HIND_ANIM);
                }
                mHandler.sendEmptyMessageDelayed(FULL_SCREEN_HIND_ANIM, 3000);
            }
        });
    }

    public void isShowBtn(boolean isshow) {
        ivUp.setEnabled(isshow);
        ivLeft.setEnabled(isshow);
        ivRight.setEnabled(isshow);
        ivDown.setEnabled(isshow);
        tvScreenshot.setEnabled(isshow);
        tvTalk.setEnabled(isshow);
        tvAudio.setEnabled(isshow);
        tvScreenshotFullscreen.setEnabled(isshow);
        tvTalkFullscreen.setEnabled(isshow);
        tvAudioFullscreen.setEnabled(isshow);
    }

    private void connect() {
        if (isconnect) {
            isconnect = false;
            m_arrObjCam[0].disconnectDev();
            // connect_btn.setText(getResources().getString(R.string.str_con));
            tvConnect.setText(R.string.camera_connect);
            tvConnectFullscreen.setText(R.string.camera_connect);
            tvConnect.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_connect), null, null);
            tvConnectFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_connect), null, null);
            tvTalk.setText(R.string.camera_talk);
            tvTalk.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
            tvTalkFullscreen.setText(R.string.camera_talk);
            tvTalkFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
            tvAudio.setText(R.string.camera_audio);
            tvAudio.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
            tvAudioFullscreen.setText(R.string.camera_audio);
            tvAudioFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
            isaudio = false;
            istalk = false;

            llDisconnectedTips.setVisibility(View.VISIBLE);
            //m_arrViewLive[0].setVisibility(View.GONE);
            isShowBtn(false);
            play();
        } else {
            // STR_DID = didEdit.getText().toString();
            // STR_USER = userEdit.getText().toString();
            // STR_PWD = pwdEdit.getText().toString();

            showDialogNow();
//            m_arrViewLive[0].setDialog(getDialog());
            m_arrObjCam[0].setDid(STR_DID);
            m_arrObjCam[0].setUser(STR_USER);
            m_arrObjCam[0].setPwd(STR_PWD);
            m_arrObjCam[0].setName("abcd");
            int nRet = m_arrObjCam[0].connectDev();
//            tvConnect.setText(R.string.camera_disconnect);
//            tvConnectFullscreen.setText(R.string.camera_disconnect);
//            tvConnect.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_disconnect), null, null);
//            tvConnectFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_disconnect), null, null);

            System.out.println("m_arrObjCam[0].connectDev()=" + nRet);
        }
    }

    private void play() {
        if (isvideo) {
            isvideo = false;
            m_arrObjCam[0].stopVideo();
            m_arrViewLive[0].updateVWhenStop();
        } else {
            isvideo = true;
            m_arrViewLive[0].attachCamera(m_arrObjCam[0]);
            m_arrObjCam[0].startVideo();
        }
    }


    @Override
    public void onClick(View v) {
        VibratorUtil vibratorUtil = new VibratorUtil();
        switch (v.getId()) {
            case R.id.ivFullscreen:
            case R.id.ivNotFullscreen: {
                unregisterSensor(sm, listener, sensor);
                if (stretch_flag) {
                    //切换成横屏
                    stretch_flag = false;
                    setFullscreen();
                } else {
                    //切换成竖屏
                    stretch_flag = true;
                    setNotFullscreen();
                }
                break;
            }
            case R.id.touchedView: {
                if (!stretch_flag && !FULL_SCREEN_IS_ANIM) {
                    fullScreenAnim();
                } else if (stretch_flag && !NOT_FULL_SCREEN_IS_ANIM) {
                    notFullScreenAnim();
                }
                break;
            }
            case R.id.ivUp:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_UP, 0);
                    stopControl();
                }
                break;

            case R.id.ivDown:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_DOWN, 0);
                    stopControl();
                }
                break;

            case R.id.ivLeft:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_LEFT, 0);
                    stopControl();
                }
                break;

            case R.id.ivRight:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_RIGHT, 0);
                    stopControl();
                }
                break;
            case R.id.tvConnect: {
                connect();
                break;
            }
            case R.id.tvConnectFullscreen: {
                connect();
                break;
            }
            case R.id.tvScreenshot: {
                photograph();
                break;
            }
            case R.id.tvScreenshotFullscreen: {
                photograph();
                break;
            }
            case R.id.tvTalk: {
                if (istalk) {
                    istalk = false;
                    m_arrObjCam[0].stopTalk();
                    tvTalk.setText(R.string.camera_talk);
                    tvTalk.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
                    ToastUtil.showToast(
                            R.string.camera_talk_close_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                } else {
                    if (isaudio) {
                        isaudio = false;
                        m_arrObjCam[0].stopAudio();
                        tvAudio.setText(R.string.camera_audio);
                        tvAudio.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
                    }
                    istalk = true;
                    m_arrObjCam[0].startTalk();
                    tvTalk.setText(R.string.camera_stop_talk);
                    tvTalk.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_untalk), null, null);
                    ToastUtil.showToast(
                            R.string.camera_talk_open_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                }
                break;
            }
            case R.id.tvTalkFullscreen: {
                if (istalk) {
                    istalk = false;
                    m_arrObjCam[0].stopTalk();
                    tvTalkFullscreen.setText(R.string.camera_talk);
                    tvTalkFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
                    ToastUtil.showToast(
                            R.string.camera_talk_close_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                } else {
                    if (isaudio) {
                        isaudio = false;
                        m_arrObjCam[0].stopAudio();
                        tvAudioFullscreen.setText(R.string.camera_audio);
                        tvAudioFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
                    }
                    istalk = true;
                    m_arrObjCam[0].startTalk();
                    tvTalkFullscreen.setText(R.string.camera_stop_talk);
                    tvTalkFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_untalk), null, null);
                    ToastUtil.showToast(
                            R.string.camera_talk_open_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                }
                break;
            }
            case R.id.tvAudio: {
                if (isaudio) {
                    isaudio = false;
                    m_arrObjCam[0].stopAudio();
                    tvAudio.setText(R.string.camera_audio);
                    tvAudio.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
                    ToastUtil.showToast(
                            R.string.camera_audio_close_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                } else {
                    if (istalk) {
                        istalk = false;
                        m_arrObjCam[0].stopTalk();
                        tvTalk.setText(R.string.camera_talk);
                        tvTalk.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
                    }
                    isaudio = true;
                    m_arrObjCam[0].startAudio();
                    tvAudio.setText(R.string.camera_stop_audio);
                    tvAudio.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_unaudio), null, null);
                    ToastUtil.showToast(
                            R.string.camera_audio_open_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                }
                break;
            }
            case R.id.tvAudioFullscreen: {
                if (isaudio) {
                    isaudio = false;
                    m_arrObjCam[0].stopAudio();
                    tvAudioFullscreen.setText(R.string.camera_audio);
                    tvAudioFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
                    ToastUtil.showToast(
                            R.string.camera_audio_close_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                    // audio_btn.setText(getResources().getString(
                    // R.string.str_play_audio));
                } else {
                    if (istalk) {
                        istalk = false;
                        m_arrObjCam[0].stopTalk();
                        tvTalkFullscreen.setText(R.string.camera_talk);
                        tvTalkFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
                    }
                    isaudio = true;
                    m_arrObjCam[0].startAudio();
                    tvAudioFullscreen.setText(R.string.camera_stop_audio);
                    tvAudioFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_unaudio), null, null);
                    ToastUtil.showToast(
                            R.string.camera_audio_open_tips, ToastType.NULL,
                            Toast.LENGTH_SHORT);
                }
                break;
            }

        }
    }

    @Override
    public boolean onLongClick(View v) {
        VibratorUtil vibratorUtil = new VibratorUtil();
        switch (v.getId()) {
            case R.id.ivUp:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_UP, 0);
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(WHAT_ptzUp, 1200);
                    }
                }
                break;

            case R.id.ivDown:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_DOWN, 0);
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(WHAT_ptzDown, 1200);
                    }
                }
                break;

            case R.id.ivLeft:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_LEFT, 0);
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(WHAT_ptzLeft, 1200);
                    }
                }
                break;

            case R.id.ivRight:
                if (isvideo) {
                    vibratorUtil.setVirbrator(CameraActivity.this);
                    vibratorUtil = null;
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_RIGHT, 0);
                    if (mHandler != null) {
                        mHandler.sendEmptyMessageDelayed(WHAT_ptzRight, 1200);
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isAction = true;
                break;

            case MotionEvent.ACTION_UP:
                isAction = false;
                if (mHandler != null) {
                    if (mHandler.hasMessages(WHAT_ptzUp)) {
                        mHandler.removeMessages(WHAT_ptzUp);
                    }
                    if (mHandler.hasMessages(WHAT_ptzDown)) {
                        mHandler.removeMessages(WHAT_ptzDown);
                    }
                    if (mHandler.hasMessages(WHAT_ptzLeft)) {
                        mHandler.removeMessages(WHAT_ptzLeft);
                    }
                    if (mHandler.hasMessages(WHAT_ptzRight)) {
                        mHandler.removeMessages(WHAT_ptzRight);
                    }
                }
                stopControl();
                break;
        }
        return false;
    }

    /**
     * 摄像头拍照
     */
    public void photograph() {
        String dirName = getResources().getString(R.string.app_name) + "photo";
        File file = FileUtils.creatSDDir(dirName);
        String filePath = FileUtils.getSDPath() + dirName + File.separator
                + DateUtil.getNowStr2() + ".jpg";// 以name存在目录中

        try {
            saveBitmapToFile(file, getBitmap(), filePath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存摄像头关闭前最后一帧
     */
    public void saveLastFrame() {
        String dirName = getResources().getString(R.string.app_name) + "photo";
        File file = FileUtils.creatSDDir(dirName);
        String filePath = FileUtils.getSDPath() + dirName + File.separator
                + STR_DID + "lastFrame.jpg";// 以name存在目录中

        try {
            saveBitmapToFile(file, m_arrObjCam[0].getLastBitmap(), filePath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取摄像头上次关闭前最后一帧
     */
    public Bitmap getLastFrame() {
        String dirName = getResources().getString(R.string.app_name) + "photo";
        File file = FileUtils.creatSDDir(dirName);
        String filePath = FileUtils.getSDPath() + dirName + File.separator
                + STR_DID + "lastFrame.jpg";// 以name存在目录中

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        return bitmap;
    }

    public Bitmap getBitmap() {
        return m_arrViewLive[0].getLastFrame();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Save Bitmap to a file.保存图片到SD卡。
     *
     * @param file
     * @param bitmap
     * @param filePath        存储的文件路径
     * @param isSavaLastFrame 是拍照还是保存最后一帧图像  true：保存最后一帧；false 拍照
     * @throws java.io.IOException
     */
    public void saveBitmapToFile(File file, final Bitmap bitmap, final String filePath, boolean isSavaLastFrame)
            throws IOException {

        if (bitmap == null) {
            if (!isSavaLastFrame) {
                ToastUtil.showToast(
                        R.string.camera_photo_fail, ToastType.ERROR,
                        Toast.LENGTH_SHORT);
            }
            return;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        try {
            if (fos == null) {
                if (!isSavaLastFrame) {
                    ToastUtil.showToast(
                            R.string.camera_photo_fail, ToastType.ERROR,
                            Toast.LENGTH_SHORT);
                }
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }

        // 其次把文件插入到系统图库
        if (!isSavaLastFrame) {
            try {
                if (bitmap != null) {
                    MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                            filePath, filePath, null);
                    ToastUtil.showToast(
                            getResources().getString(R.string.camera_photo_success),
                            ToastType.NULL, Toast.LENGTH_SHORT);
                    // 最后通知图库更新
                    mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
                }
            } catch (FileNotFoundException e) {
                ToastUtil.showToast(
                        R.string.camera_photo_fail, ToastType.ERROR,
                        Toast.LENGTH_SHORT);
                e.printStackTrace();
            }
        }
    }

    //    Handler mHandler = new Handler(new CameraHandlerCallback());
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            VibratorUtil vibratorUtil = new VibratorUtil();
            switch (msg.what) {
                case 0:
                    isShowBtn(false);
                    break;

                case 1:
                    isShowBtn(true);
                    break;

                case ALERMPARAMS:
                    // motion_armed
//                    if (alermBean.getMotion_armed() == 0) {
//                        strMsg = "close motion armed";
//                    } else {
//                        strMsg = "start motion armed";
//                    }

                    // input_armed
                    if (alermBean.getInput_armed() == 0) {
                        strMsg = "close input armed";
                    } else {
                        strMsg = "start input armed";

                    }

                    // audio_armed
                    if (alermBean.getnAudioAlarmSensitivity() == 0) {
                        strMsg = "close audio armed";
                    } else {
                        strMsg = "start audio armed";
                    }
                    break;

                //横竖屏切换
                case CHANGE_FULL_SCREEN:
                    int orientation = msg.arg1;
                    LogUtil.d(TAG, "handleMessage() - orientation = " + orientation);
                    if (orientation > 45 && orientation < 135) {
                        System.out.println("切换成横屏翻转");
                        setReverseFullscreen();
                        sensor_flag = false;
                        stretch_flag = false;

                    } else if (orientation > 135 && orientation < 225) {
                        System.out.println("切换成竖屏翻转");
                        setReverseNotFullscreen();
                        sensor_flag = false;
                        stretch_flag = false;
                    } else if (orientation > 225 && orientation < 315) {
                        System.out.println("切换成横屏");
                        setFullscreen();
                        sensor_flag = false;
                        stretch_flag = false;
                    } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                        System.out.println("切换成竖屏");
                        setNotFullscreen();
                        sensor_flag = true;
                        stretch_flag = true;

                    }
                    break;

                //横竖屏切换
                case FULL_SCREEN_HIND_ANIM:
                    if (FULL_SCREEN_IS_SHOW) {
                        animate(fullscreen_ll, fadeInFullscreen_ll);
                        animate(ivNotFullscreen, fadeInIvNotFullscreen);
                        FULL_SCREEN_IS_ANIM = true;
                    }
                    break;

                //横竖屏切换
                case NOT_FULL_SCREEN_HIND_ANIM:
                    if (NOT_FULL_SCREEN_IS_SHOW) {
                        animate(ivFullscreen, fadeInIvFullscreen);
                        NOT_FULL_SCREEN_IS_ANIM = true;
                    }
                    break;

                case WHAT_ptzStop:
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_STOP, 0);
                    break;

                case WHAT_ptzUp:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_UP, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzUp, 1200);
                        }
                    }
                    break;

                case WHAT_ptzDown:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_DOWN, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzDown, 1200);
                        }
                    }
                    break;

                case WHAT_ptzLeft:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_LEFT, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzLeft, 1200);
                        }
                    }
                    break;

                case WHAT_ptzRight:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_RIGHT, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzRight, 1200);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onRightClick(View v) {
        Intent intent = new Intent(this, SetDeviceActivity.class);
        intent.putExtra(Constant.DEVICE, device);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SetDeviceActivity.RESULT_DELETE) {
            finish();
        }

        if (data != null) {
            device = (Device) data.getSerializableExtra(Constant.DEVICE);
            if (device != null) {
                nb_title.setCenterText(device.getDeviceName());
            }
        }
    }

    @Override
    public void onRequestLayout() {
        dismissDialog();
        resetScreen();
    }

    class CameraHandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            VibratorUtil vibratorUtil = new VibratorUtil();
            switch (msg.what) {
                case 0:
                    isShowBtn(false);
                    break;

                case 1:
                    isShowBtn(true);
                    break;

                case ALERMPARAMS:
                    // motion_armed
//                    if (alermBean.getMotion_armed() == 0) {
//                        strMsg = "close motion armed";
//                    } else {
//                        strMsg = "start motion armed";
//                    }

                    // input_armed
                    if (alermBean.getInput_armed() == 0) {
                        strMsg = "close input armed";
                    } else {
                        strMsg = "start input armed";

                    }

                    // audio_armed
                    if (alermBean.getnAudioAlarmSensitivity() == 0) {
                        strMsg = "close audio armed";
                    } else {
                        strMsg = "start audio armed";
                    }
                    break;

                //横竖屏切换
                case CHANGE_FULL_SCREEN:
                    int orientation = msg.arg1;
                    LogUtil.d(TAG, "handleMessage() - orientation = " + orientation);
                    if (orientation > 45 && orientation < 135) {
                        System.out.println("切换成横屏翻转");
                        setReverseFullscreen();
                        sensor_flag = false;
                        stretch_flag = false;

                    } else if (orientation > 135 && orientation < 225) {
                        System.out.println("切换成竖屏翻转");
                        setReverseNotFullscreen();
                        sensor_flag = false;
                        stretch_flag = false;
                    } else if (orientation > 225 && orientation < 315) {
                        System.out.println("切换成横屏");
                        setFullscreen();
                        sensor_flag = false;
                        stretch_flag = false;
                    } else if ((orientation > 315 && orientation < 360) || (orientation > 0 && orientation < 45)) {
                        System.out.println("切换成竖屏");
                        setNotFullscreen();
                        sensor_flag = true;
                        stretch_flag = true;

                    }
                    break;

                //横竖屏切换
                case FULL_SCREEN_HIND_ANIM:
                    if (FULL_SCREEN_IS_SHOW) {
                        animate(fullscreen_ll, fadeInFullscreen_ll);
                        animate(ivNotFullscreen, fadeInIvNotFullscreen);
                        FULL_SCREEN_IS_ANIM = true;
                    }
                    break;

                //横竖屏切换
                case NOT_FULL_SCREEN_HIND_ANIM:
                    if (NOT_FULL_SCREEN_IS_SHOW) {
                        animate(ivFullscreen, fadeInIvFullscreen);
                        NOT_FULL_SCREEN_IS_ANIM = true;
                    }
                    break;

                case WHAT_ptzStop:
                    m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_STOP, 0);
                    break;

                case WHAT_ptzUp:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_UP, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzUp, 1200);
                        }
                    }
                    break;

                case WHAT_ptzDown:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_DOWN, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzDown, 1200);
                        }
                    }
                    break;

                case WHAT_ptzLeft:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_LEFT, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzLeft, 1200);
                        }
                    }
                    break;

                case WHAT_ptzRight:
                    if (isvideo) {
//                        vibratorUtil.setVirbrator(CameraActivity.this);
//                        vibratorUtil = null;
                        m_arrObjCam[0].PTZCtrol(SEP2P_Define.PTZ_CTRL_RIGHT, 0);
                        stopControl();
                        if (mHandler != null && isAction) {
                            mHandler.sendEmptyMessageDelayed(WHAT_ptzRight, 1200);
                        }
                    }
                    break;
            }
            // super.handleMessage(msg);
            return true;
        }
    }

    @Override
    public void onLANSearch(byte[] pData, int nDataSize) {
        SEARCH_RESP objSearchResp = new SEARCH_RESP(pData);
        System.out.println("Searched: DID=" + objSearchResp.getDID() + ", ip="
                + objSearchResp.getIpAddr());
    }

    protected void onPause() {
        LogUtil.d(TAG, "onPause() - start");
        saveLastFrame();
        unregisterSensor(sm, listener, sensor);
        unregisterSensor(sm1, listener1, sensor1);
        listener = null;
        listener1 = null;
        super.onPause();
        LogUtil.d(TAG, "onPause() - end");
    }


    @SuppressWarnings("static-access")
    protected void doDestroy() {
        LogUtil.d(TAG, "doDestroy() - start");
        m_arrObjCam[0].stopSearchInLAN();
        m_arrObjCam[0].stopRecord();
        m_arrObjCam[0].stopTalk();
        m_arrObjCam[0].stopAudio();
        m_arrObjCam[0].stopVideo();

        m_arrObjCam[0].disconnectDev();
        CamObj.deinitAPI();

        CallbackService.setEventInterface(null);
        CallbackService.setLANSearchInterface(null);
        Intent intent = new Intent();
        intent.setClass(this, CallbackService.class);
        stopService(intent);

        if (sm != null && listener != null) {
            if (sensor == null) {
                sm.unregisterListener(listener);
            } else {
                sm.unregisterListener(listener, sensor);
            }
        }
        if (sm1 != null && listener1 != null) {
            if (sensor1 == null) {
                sm1.unregisterListener(listener1);
            } else {
                sm1.unregisterListener(listener1, sensor1);
            }
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        listener = null;
        listener1 = null;

        sensor = null;
        sensor1 = null;

        LogUtil.d(TAG, "doDestroy() - end");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LogUtil.d(TAG, "onKeyDown() - 开始注销传感器 sm = " + sm + " listener = " + listener + " sensor = " + sensor + " stretch_flag = " + stretch_flag);
            unregisterSensor(sm, listener, sensor);
            if (!stretch_flag) {
                //切换成竖屏
                stretch_flag = true;
                setNotFullscreen();
            } else {
                doDestroy();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void unregisterSensor(SensorManager sm, SensorEventListener listener, Sensor sensor) {
        LogUtil.d(TAG, "unregisterSensor() - start");
        if (sm != null && listener != null) {
            if (sensor == null) {
                sm.unregisterListener(listener);
            } else {
                sm.unregisterListener(listener, sensor);
            }
        }
        LogUtil.d(TAG, "unregisterSensor() - end");
    }

    @Override
    protected void onDestroy() {
        doDestroy();
        if (m_arrObjCam != null) {
            for (CamObj cam : m_arrObjCam) {
                if (cam != null) {
                    // cam.regAVListener(null);
                    cam = null;
                }
            }
        }
        super.onDestroy();
    }

    @Override
    public void updateFrameInfo(Object obj, int nWidth, int nHeigh) {
    }

    @Override
    public void updateBmpFrame(Object obj, byte[] rawVideoData, Bitmap bmp) {
    }

    @Override
    public void updateMsg(Object obj, int nMsgType, byte[] pMsg, int nMsgSize,
                          int pUserData) {
        LogUtil.d(TAG, "updateMsg()-" + Thread.currentThread());
        if (isFinishingOrDestroyed()) {
            LogUtil.w(TAG, "updateMsg()-Activity is finishing or destroyed.");
            return;
        }
        Message msg = MsgHandler.obtainMessage();
        msg.what = nMsgType;
        msg.obj = pMsg;
        MsgHandler.sendMessage(msg);
    }

    Handler MsgHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(Message msg) {
            LogUtil.e(TAG, "handleMessage()-what:" + msg.what);
            switch (msg.what) {
                case SEP2P_Define.SEP2P_MSG_CONNECT_STATUS:
                    int msgParam = m_arrObjCam[0].getStatus();
                    @SuppressWarnings("unused")
                    String did = m_arrObjCam[0].getDid();
                    if (msgParam == MSG_CONNECT_STATUS.CONNECT_STATUS_INVALID_ID
                            || msgParam == MSG_CONNECT_STATUS.CONNECT_STATUS_WRONG_USER_PWD) {
                        m_arrObjCam[0].disconnectDev();
                        //   setConnectButton(isconnect);
                    } else if (msgParam == MSG_CONNECT_STATUS.CONNECT_STATUS_CONNECT_TIMEOUT) {
                        m_arrObjCam[0].disconnectDev();
                        dismissDialog();
                        isconnect = false;
                        isShowBtn(isconnect);
                        llDisconnectedTips.setVisibility(View.VISIBLE);
                        //m_arrViewLive[0].setVisibility(View.GONE);
                        ToastUtil.showToast(
                                R.string.camera_connect_timeout, ToastType.ERROR,
                                Toast.LENGTH_SHORT);
                        // setConnectButton(false);
                    } else if (msgParam == MSG_CONNECT_STATUS.CONNECT_STATUS_CONNECT_FAILED) {
                        m_arrObjCam[0].disconnectDev();
                        dismissDialog();
                        isconnect = false;
                        isShowBtn(isconnect);
                        llDisconnectedTips.setVisibility(View.VISIBLE);
                        //m_arrViewLive[0].setVisibility(View.GONE);
                        ToastUtil.showToast(
                                R.string.camera_connect_fail, ToastType.ERROR,
                                Toast.LENGTH_SHORT);
                        //   setConnectButton(false);
                    } else if (msgParam == MSG_CONNECT_STATUS.CONNECT_STATUS_DEVICE_NOT_ONLINE) {
                        m_arrObjCam[0].disconnectDev();
                        dismissDialog();
                        isconnect = false;
                        isShowBtn(isconnect);
                        llDisconnectedTips.setVisibility(View.VISIBLE);
                        //m_arrViewLive[0].setVisibility(View.GONE);
                        ToastUtil.showToast(
                                R.string.camera_connect_offline, ToastType.ERROR,
                                Toast.LENGTH_SHORT);
                        //  setConnectButton(false);
                    } else if (msgParam == MSG_CONNECT_STATUS.CONNECT_STATUS_EXCEED_MAX_USER) {
                        m_arrObjCam[0].disconnectDev();
                        dismissDialog();
                        isconnect = false;
                        isShowBtn(isconnect);
                        llDisconnectedTips.setVisibility(View.VISIBLE);
                        //m_arrViewLive[0].setVisibility(View.GONE);
                        ToastUtil.showToast(
                                R.string.camera_connect_exceed_max_user, ToastType.ERROR,
                                Toast.LENGTH_SHORT);
                        setConnectButton(false);
                    } else if (msgParam == MSG_CONNECT_STATUS.CONNECT_STATUS_CONNECTED) {
                        // connect_btn.setText(getResources().getString(
                        // R.string.str_discon));
                        isconnect = true;
                        llDisconnectedTips.setVisibility(View.GONE);
                        m_arrViewLive[0].setVisibility(View.VISIBLE);
                        isShowBtn(isconnect);
//                        play();
                        if (isvideo == false) {
                            isvideo = true;
                            m_arrViewLive[0].attachCamera(m_arrObjCam[0]);
                            m_arrObjCam[0].startVideo();
                        }
//                        resetScreen();
                        // setConnectButton(true);
                    }
                    setConnectButton(isconnect);
                    break;
                case SEP2P_Define.SEP2P_MSG_GET_ALARM_INFO_RESP: {
                    //setConnectButton(false);
                }
                break;
                case SEP2P_Define.SEP2P_MSG_SET_ALARM_INFO_RESP: {
                    if (msg.obj == null)
                        break;
                    byte[] byts = (byte[]) msg.obj;
                    if (byts != null) {
                        if (byts[0] == 0)
                            mHandler.sendEmptyMessage(1);
                        else
                            mHandler.sendEmptyMessage(0);
                    }
                }
                break;

            }

        }
    };

    private void setConnectButton(boolean isConnected) {
        if (isConnected) {
            tvConnect.setText(R.string.camera_disconnect);
            tvConnectFullscreen.setText(R.string.camera_disconnect);
            tvConnect.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_disconnect), null, null);
            tvConnectFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_disconnect), null, null);

        } else {
            tvConnect.setText(R.string.camera_connect);
            tvConnectFullscreen.setText(R.string.camera_connect);
            tvConnect.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_connect), null, null);
            tvConnectFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_connect), null, null);
        }
    }

    // receive alarm event
    @Override
    public void onEvent(String pDID, int nEventType, int pUserData) {
        if (!STR_DID.equals(pDID))
            return;

        switch (nEventType) {
            case SEP2P_Define.EVENT_TYPE_MOTION_ALARM:
                strMsg = "EVENT_TYPE_MOTION_ALARM";
                System.out.println(strMsg);
                break;
            case SEP2P_Define.EVENT_TYPE_INPUT_ALARM:
                strMsg = "EVENT_TYPE_INPUT_ALARM";
                System.out.println(strMsg);
                break;
            case SEP2P_Define.EVENT_TYPE_AUDIO_ALARM:
                strMsg = "EVENT_TYPE_AUDIO_ALARM";
                System.out.println(strMsg);
                break;
        }
    }

    public void back(View v) {
        doDestroy();
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (stretch_flag) {
            LogUtil.d(TAG, "竖屏");
            if (isaudio) {
                tvAudio.setText(R.string.camera_stop_audio);
                tvAudio.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_unaudio), null, null);
            } else {
                tvAudio.setText(R.string.camera_audio);
                tvAudio.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
            }
            if (istalk) {
                tvTalk.setText(R.string.camera_stop_talk);
                tvTalk.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_untalk), null, null);
            } else {
                tvTalk.setText(R.string.camera_talk);
                tvTalk.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
            }
            setNotFullscreen();
        } else {
            LogUtil.d(TAG, "横屏");
            if (isaudio) {
                tvAudioFullscreen.setText(R.string.camera_stop_audio);
                tvAudioFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_unaudio), null, null);
            } else {
                tvAudioFullscreen.setText(R.string.camera_audio);
                tvAudioFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_audio), null, null);
            }
            if (istalk) {
                tvTalkFullscreen.setText(R.string.camera_stop_talk);
                tvTalkFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_untalk), null, null);
            } else {
                tvTalkFullscreen.setText(R.string.camera_talk);
                tvTalkFullscreen.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.camera_talk), null, null);
            }
            setFullscreen();
        }
    }

    /**
     * 全屏
     */
    private void setFullscreen() {
        if (!FULL_SCREEN_IS_ANIM) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            nb_title.setVisibility(View.GONE);
            control_ll.setVisibility(View.GONE);
            ivFullscreen.setVisibility(View.GONE);
            fullscreen_ll.setVisibility(View.VISIBLE);
            ivNotFullscreen.setVisibility(View.VISIBLE);
            FULL_SCREEN_IS_SHOW = true;
            if (mHandler != null && mHandler.hasMessages(NOT_FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(NOT_FULL_SCREEN_HIND_ANIM);
            }

            if (mHandler != null && mHandler.hasMessages(FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(FULL_SCREEN_HIND_ANIM);
            }
            mHandler.sendEmptyMessageDelayed(FULL_SCREEN_HIND_ANIM, 3000);
        }
    }

    /**
     * 切换成竖屏，并且3s后执行隐藏动画
     */
    private void setNotFullscreen() {
        if (!NOT_FULL_SCREEN_IS_ANIM) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            nb_title.setVisibility(View.VISIBLE);
            control_ll.setVisibility(View.VISIBLE);
            ivFullscreen.setVisibility(View.VISIBLE);
            LogUtil.d(TAG, "setNotFullscreen()");
            fullscreen_ll.setVisibility(View.GONE);
            ivNotFullscreen.setVisibility(View.GONE);
            NOT_FULL_SCREEN_IS_SHOW = true;
            if (mHandler != null && mHandler.hasMessages(FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(FULL_SCREEN_HIND_ANIM);
            }

            if (mHandler != null && mHandler.hasMessages(NOT_FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(NOT_FULL_SCREEN_HIND_ANIM);
            }
            mHandler.sendEmptyMessageDelayed(NOT_FULL_SCREEN_HIND_ANIM, 3000);
        }
    }

    /**
     * 切换成横屏翻转，并且3s后执行隐藏动画
     */
    private void setReverseFullscreen() {
        if (!FULL_SCREEN_IS_ANIM) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            nb_title.setVisibility(View.GONE);
            control_ll.setVisibility(View.GONE);
            ivFullscreen.setVisibility(View.GONE);
            fullscreen_ll.setVisibility(View.VISIBLE);
            ivNotFullscreen.setVisibility(View.VISIBLE);
            FULL_SCREEN_IS_SHOW = true;
            if (mHandler != null && mHandler.hasMessages(NOT_FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(NOT_FULL_SCREEN_HIND_ANIM);
            }

            if (mHandler != null && mHandler.hasMessages(FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(FULL_SCREEN_HIND_ANIM);
            }
            mHandler.sendEmptyMessageDelayed(FULL_SCREEN_HIND_ANIM, 3000);
        }
    }

    /**
     * 切换成竖屏翻转，并且3s后执行隐藏动画
     */
    private void setReverseNotFullscreen() {
        if (!NOT_FULL_SCREEN_IS_ANIM) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            nb_title.setVisibility(View.VISIBLE);
            control_ll.setVisibility(View.VISIBLE);
            ivFullscreen.setVisibility(View.VISIBLE);
            fullscreen_ll.setVisibility(View.GONE);
            ivNotFullscreen.setVisibility(View.GONE);
            NOT_FULL_SCREEN_IS_SHOW = true;
            if (mHandler != null && mHandler.hasMessages(FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(FULL_SCREEN_HIND_ANIM);
            }

            if (mHandler != null && mHandler.hasMessages(NOT_FULL_SCREEN_HIND_ANIM)) {
                mHandler.removeMessages(NOT_FULL_SCREEN_HIND_ANIM);
            }
            mHandler.sendEmptyMessageDelayed(NOT_FULL_SCREEN_HIND_ANIM, 3000);
        }
    }

    private void resetScreen() {
        if (stretch_flag) {
            setNotFullscreen();
        } else {
            setFullscreen();
        }
    }

    private void stopControl() {
        if (mHandler.hasMessages(WHAT_ptzStop)) {
            mHandler.removeMessages(WHAT_ptzStop);
        }
        mHandler.sendEmptyMessageDelayed(WHAT_ptzStop,
                PTZ_ONE_STEP_TIME_MS);
    }

    /**
     * 画面全屏时，按钮隐藏显示动画
     */
    private void fullScreenAnim() {
        if (FULL_SCREEN_IS_SHOW) {
            animate(fullscreen_ll, fadeInFullscreen_ll);
            animate(ivNotFullscreen, fadeInIvNotFullscreen);
            FULL_SCREEN_IS_ANIM = true;
        } else {
            animate(fullscreen_ll, fadeOutFullscreen_ll);
            animate(ivNotFullscreen, fadeOutIvNotFullscreen);
            FULL_SCREEN_IS_ANIM = true;
        }
    }

    /**
     * 画面非全屏时，按钮隐藏显示动画
     */
    private void notFullScreenAnim() {
        if (NOT_FULL_SCREEN_IS_SHOW) {
            animate(ivFullscreen, fadeInIvFullscreen);
            NOT_FULL_SCREEN_IS_ANIM = true;
        } else {
            animate(ivFullscreen, fadeOutIvFullscreen);
            NOT_FULL_SCREEN_IS_ANIM = true;
        }
    }

    private void animate(View view, Animation animation) {
        view.startAnimation(animation);
    }


    /**
     * 重力感应监听者
     */
    public class OrientationSensorListener implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        public static final int ORIENTATION_UNKNOWN = -1;

//        private Handler rotateHandler;
//
//        public OrientationSensorListener(Handler handler) {
//            rotateHandler = handler;
//        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        public void onSensorChanged(SensorEvent event) {

            if (sensor_flag != stretch_flag)  //只有两个不相同才开始监听行为
            {
                float[] values = event.values;
                int orientation = ORIENTATION_UNKNOWN;
                float X = -values[_DATA_X];
                float Y = -values[_DATA_Y];
                float Z = -values[_DATA_Z];
                float magnitude = X * X + Y * Y;
                // Don't trust the angle if the magnitude is small compared to the y value
                if (magnitude * 4 >= Z * Z) {
                    //屏幕旋转时
                    float OneEightyOverPi = 57.29577957855f;
                    float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                    orientation = 90 - (int) Math.round(angle);
                    // normalize to 0 - 359 range
                    while (orientation >= 360) {
                        orientation -= 360;
                    }
                    while (orientation < 0) {
                        orientation += 360;
                    }
                    //LogUtil.d(TAG,"OrientationSensorListener onSensorChanged() - orientation = " + orientation);
                }
                //LogUtil.d(TAG,"1111111OrientationSensorListener onSensorChanged() - orientation = " + orientation);
                if (mHandler != null) {
                    mHandler.obtainMessage(CHANGE_FULL_SCREEN, orientation, 0).sendToTarget();
                }

            }
        }
    }


    public class OrientationSensorListener2 implements SensorEventListener {
        private static final int _DATA_X = 0;
        private static final int _DATA_Y = 1;
        private static final int _DATA_Z = 2;

        public static final int ORIENTATION_UNKNOWN = -1;

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        public void onSensorChanged(SensorEvent event) {

            float[] values = event.values;

            int orientation = ORIENTATION_UNKNOWN;
            float X = -values[_DATA_X];
            float Y = -values[_DATA_Y];
            float Z = -values[_DATA_Z];

            float magnitude = X * X + Y * Y;
            // Don't trust the angle if the magnitude is small compared to the y value
            if (magnitude * 4 >= Z * Z) {
                //屏幕旋转时
                float OneEightyOverPi = 57.29577957855f;
                float angle = (float) Math.atan2(-Y, X) * OneEightyOverPi;
                orientation = 90 - (int) Math.round(angle);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
//                LogUtil.d(TAG, "OrientationSensorListener2 onSensorChanged() - orientation = " + orientation);
            }

            //LogUtil.d(TAG,"2222222OrientationSensorListener2 onSensorChanged() - orientation = " + orientation);
            if (orientation > 225 && orientation < 315 || orientation > 45 && orientation < 135) {  //横屏
                sensor_flag = false;
            } else if ((orientation > 315 && orientation < 360)
                    || (orientation > 0 && orientation < 45)) {  //竖屏
                sensor_flag = true;
            }

            //LogUtil.d(TAG,"onSensorChanged() - stretch_flag = " + stretch_flag + " sensor_flag = " + sensor_flag);
            if (stretch_flag == sensor_flag) {  //点击变成横屏  屏幕 也转横屏 激活
//                System.out.println("激活");
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }
}
