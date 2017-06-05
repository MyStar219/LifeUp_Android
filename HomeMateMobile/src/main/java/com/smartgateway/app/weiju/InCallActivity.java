package com.smartgateway.app.weiju;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.evideo.voip.sdk.EVVideoView;
import com.evideo.voip.sdk.EVVoipAccount;
import com.evideo.voip.sdk.EVVoipCall;
import com.evideo.voip.sdk.EVVoipCallParams;
import com.evideo.voip.sdk.EVVoipException;
import com.evideo.voip.sdk.EVVoipManager;
import com.evideo.weiju.WeijuManage;
import com.evideo.weiju.callback.CommandCallback;
import com.evideo.weiju.command.unlock.CloudUnlockCommand;
import com.evideo.weiju.info.CommandError;
import com.evideo.weiju.info.voip.MonitorInfo;
import com.smartgateway.app.R;
import com.smartgateway.app.WeijuHelper;

/**
 * 通话界面
 */
public class InCallActivity extends FragmentActivity implements View.OnClickListener {

    private final String TAG = "weiju_sdk";
    private String apartmentId;
    private String toNumber;//呼叫的号码
    private TextView callInfoView;//显示当前通话信息
    private EVVideoView displayView;//显示当前通话视频
    private Button btnSpeaker/*扬声器开关*/, btnMic/*静音开关*/, btnVideo/*视频开关*/, btnSnapshot/*抓拍*/, btnUnlock/*开锁*/, btnHangup/*挂断*/;

    private HandleThread handleThread;
    private Handler myHandler;//子线程Handler
    private Handler mainHandler;//主线程Handler

    private MonitorInfo monitorInfo;//当前监视信息

    //private String photoPath = Environment.getExternalStorageDirectory() + File.separator + "evideo_voip_shot.jpg";//截图保存路径

    public static EVVoipCall evVoipCall;//当前新来电


    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onAttachedToWindow();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        super.setContentView(R.layout.activity_call);
        callInfoView = (TextView) findViewById(R.id.call_info);
        displayView = (EVVideoView) getSupportFragmentManager().findFragmentById(R.id.display_view);
        btnSpeaker = (Button) findViewById(R.id.btn_speaker);
        btnMic = (Button) findViewById(R.id.btn_mic);
        btnVideo = (Button) findViewById(R.id.btn_switch_video);
        btnSnapshot = (Button) findViewById(R.id.btn_snapshot);
        btnUnlock = (Button) findViewById(R.id.btn_unlock);
        btnHangup = (Button) findViewById(R.id.btn_hangup);
        btnSpeaker.setOnClickListener(this);
        btnMic.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnSnapshot.setOnClickListener(this);
        btnUnlock.setOnClickListener(this);
        btnHangup.setOnClickListener(this);
        mainHandler = new Handler(Looper.getMainLooper());

		/*启动子线程*/
        handleThread = new HandleThread();
        handleThread.start();
        myHandler = new Handler(handleThread.getLooper());

		/*获取监视对象*/
        monitorInfo = (MonitorInfo) getIntent().getSerializableExtra("monitor");
		/*获取呼叫的号码*/
        toNumber = getIntent().getStringExtra("toNumber");
        apartmentId = getIntent().getStringExtra("apartmentId");
        handleCall();//处理通话

//        final VoipInfo voipInfo = (VoipInfo) getIntent().getSerializableExtra("voipInfo");
//        if (voipInfo == null) {
//            Log.d("weiju_sdk", "is null");
//        }
//        /*初始化VoipSDK*/
//        Log.d("weiju_sdk", "" + EVVoipManager.isReadly());
//        EVVoipManager.init(getApplicationContext(), new EVVoipManager.OnInitCallback() {
//
//            @Override
//            public void complete() {
//                Log.d("weiju_sdk", "VoipSDK 初始化成功");
//
//				/*添加来电提醒*/
//                EVVoipManager.setIncomingCallback(new InComingHandler());
//
//                /*登录Voip账号*/
//                try {
//                    if (EVVoipManager.isReadly()) {
//                        Log.d("weiju_sdk", "登录账号:" + voipInfo.getUsername() + " " + voipInfo.getDomain());
//                        evVoipAccount = EVVoipManager.login(voipInfo.getUsername(), voipInfo.getPassword(), voipInfo.getUsername(), voipInfo.getDomain(), voipInfo.getPort());
//                        if (evVoipAccount != null) {
//                            evVoipAccount.setAccountStateCallback(new EVVoipAccount.AccountStateCallback() {
//                                @Override
//                                public void onState(EVVoipAccount.AccountState state) {
//                                    Log.d("weiju_sdk", state.toString());
//                                    if (EVVoipAccount.AccountState.ONLINE == state) {
//                                        Toast.makeText(getApplicationContext(), "在线", Toast.LENGTH_SHORT).show();
//                                        handleCall();//处理通话
//                                    } else if (EVVoipAccount.AccountState.OFFLINE == state) {
//                                        Toast.makeText(getApplicationContext(), "离线", Toast.LENGTH_SHORT).show();
//                                    } else if (EVVoipAccount.AccountState.LOGINPROCESS == state) {
//                                        Toast.makeText(getApplicationContext(), "登录中。。。", Toast.LENGTH_SHORT).show();
//                                    } else if (EVVoipAccount.AccountState.NONE == state) {
//                                        Toast.makeText(getApplicationContext(), "未登录", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                        }
//                    }
//                } catch (EVVoipException e) {
//                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(), "Voip登录失败", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void error(Throwable throwable) {
//                throwable.printStackTrace();
//                Log.d("weiju_sdk", "VoipSDK 初始化失败 " + throwable.getLocalizedMessage());
//            }
//
//        });
    }

    @Override
    protected void onDestroy() {
        Log.d("weiju_sdk", "onDestroy");
        hangup();
        if (myHandler != null) {
            myHandler.removeCallbacksAndMessages(null);
        }
        if (handleThread != null) {
            handleThread.quit();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_speaker://开关扬声器
                if (evVoipCall != null) {
                    if (evVoipCall.isSpeakerEnabled()) {//扬声器开启状态
                        evVoipCall.enableSpeaker(false);
                        btnSpeaker.setText("扬声器-开");
                    } else {//扬声器关闭状态
                        evVoipCall.enableSpeaker(true);
                        btnSpeaker.setText("扬声器-关");
                    }
                }
                break;

            case R.id.btn_mic://开关静音
                if (evVoipCall != null) {
                    if (evVoipCall.isMicrophoneEnabled()) {//麦克风开启状态
                        evVoipCall.enableMicrophone(false);
                        btnMic.setText("静音-关");
                    } else {//静音关闭状态
                        evVoipCall.enableMicrophone(true);
                        btnMic.setText("静音-开");
                    }
                }
                break;
            case R.id.btn_switch_video://开关视频
                if (evVoipCall != null) {
                    if (evVoipCall.isEnableVideo()) {//视频开启状态
                        evVoipCall.enableVideo(false);
                    } else {//视频关闭状态
                        evVoipCall.enableVideo(true);
                    }
                }
                break;
            case R.id.btn_snapshot://抓拍
                Log.d(TAG, evVoipCall.getDuration() + "秒");
//                myHandler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (evVoipCall != null) {
//                            if (evVoipCall.snapshot(new File(photoPath))) {
//                                mainHandler.post(new Runnable() {
//                                    public void run() {
//                                        Toast.makeText(getApplicationContext(), "保存至" + photoPath, Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            }
//                        }
//                    }
//                });
                break;
            case R.id.btn_unlock://开锁
                if (evVoipCall != null) {
                    EVVoipAccount evVoipAccount = evVoipCall.getRemoteAccount();
                    int cid = 0;
                    String code = null;
                    if (EVVoipCall.CallDirection.OUTGOING == evVoipCall.getDirection()) {//呼出
                        code = monitorInfo.getDevice_code();
                        cid = monitorInfo.getCommunity_id();
                    } else if (EVVoipCall.CallDirection.INCOMING == evVoipCall.getDirection()) {//呼入
                        code = evVoipAccount.getPayLoad().getDeviceCode();
                        cid = evVoipAccount.getPayLoad().getCid();
                    }
                    if (cid != 0 && !TextUtils.isEmpty(code)) {
                        Log.d(TAG, "cid:" + cid + " code:" + code);
                        //创建命令开锁执行者
                        String apartmentId = WeijuHelper.getApartmentInfo().getId();
                        CloudUnlockCommand unlockCommand = new CloudUnlockCommand(getApplicationContext(), cid, code, apartmentId);
                        unlockCommand.setCallback(new CommandCallback() {

                            @Override
                            public void onSuccess() {
//                                Toast.makeText(getApplicationContext(), "开锁成功", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(CommandError error) {
                                Log.e(TAG, "错误码 " + error.getStatus() + " 具体信息:" + error.getMessage());
//                                Toast.makeText(getApplicationContext(), "开锁失败 错误码 " + error.getStatus(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        WeijuManage.execute(unlockCommand);//执行开锁请求
                    }
                }
                break;
            case R.id.btn_hangup://挂断
                finish();
                break;
        }
    }

    /**
     * 处理通话
     */
    private void handleCall() {
        if (evVoipCall == null && !TextUtils.isEmpty(toNumber)) {//呼出
            myHandler.post(new Runnable() {

                @Override
                public void run() {
					/*初始化呼叫配置参数集*/
                    EVVoipCallParams params = new EVVoipCallParams();
                    params.setDisplay(displayView);
					/*开始呼叫*/
                    try {
                        /*加入通话状态回调*/
                        evVoipCall = EVVoipManager.call(toNumber, params);
                        setupCallStateCallback();

						/*更新通话信息*/
                        mainHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                if (evVoipCall != null) {
                                    if (EVVoipCall.CallDirection.OUTGOING == evVoipCall.getDirection()) {//呼出
                                        callInfoView.setText("呼叫 " + monitorInfo.getVoip_username());
                                    }
                                }
                            }
                        });

                    } catch (EVVoipException e) {
                        e.printStackTrace();
                        finish();
                        return;
                    }
                }
            });
        }else if(evVoipCall != null && EVVoipCall.CallDirection.INCOMING == evVoipCall.getDirection()){//呼入
            setupCallStateCallback();

                /*更新通话信息*/
            if (EVVoipCall.CallDirection.INCOMING == evVoipCall.getDirection()) {
                callInfoView.setText("来电 " + evVoipCall.getRemoteAccount().getUsername());
            }


            myHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    try {
                        evVoipCall.accept(displayView);
                    } catch (EVVoipException e) {
                        e.printStackTrace();
                    }
                }
            }, 3000);//TODO 模拟延迟3秒后接听
        }
    }

    /**
     * 挂断通话
     */
    private void hangup() {
        new Thread() {
            public void run() {
                try {
                    if (evVoipCall != null) {
                        evVoipCall.hangup();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "通话总时长 " + evVoipCall.getDuration() + "秒", Toast.LENGTH_SHORT).show();
//                                Log.d("weiju_sdk", "voip deInit");
//                                EVVoipManager.deInit(getApplicationContext());
                                evVoipCall = null;
                            }
                        });
                    }

                } catch (EVVoipException e) {
                    e.printStackTrace();
                }
            }
        }.start();//实际项目中自行进行线程池
    }

    /**
     * 设置通话状态回调
     */
    private void setupCallStateCallback() {
        if (evVoipCall != null) {
            evVoipCall.setCallStateCallback(new EVVoipCall.CallStateCallback() {

                @Override
                public void onState(EVVoipCall.CallState state, EVVoipCall.EndReason reason) {
                    Log.i(TAG, "onState:" + state.toString());
                    if (EVVoipCall.CallState.INCOMING == state) {
                        Log.d(TAG, "新来电");
                    } else if (EVVoipCall.CallState.OUTGOING == state) {
                        Log.d(TAG, "呼出");
                    } else if (EVVoipCall.CallState.CONNECTED == state) {
                        Log.d(TAG, "进入通话状态 ");
                        if (evVoipCall != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    btnSpeaker.setText(evVoipCall.isSpeakerEnabled() ? "扬声器-关" : "扬声器-开");
                                    btnMic.setText(evVoipCall.isMicrophoneEnabled() ? "静音-开" : "静音-关");
                                    btnVideo.setText(evVoipCall.isEnableVideo() ? "视频-关" : "视频-开");
                                }
                            });
                        }
                    } else if (EVVoipCall.CallState.END == state) {
                        Log.d(TAG, "通话结束 ");
                        if (reason != null && EVVoipCall.EndReason.NONE != reason) {
                            if (EVVoipCall.EndReason.BUSY == reason) {
                                Log.e(TAG, "对方忙");
                            } else if (EVVoipCall.EndReason.NOTFOUND == reason) {
                                Log.e(TAG, "对方不在线");
                            } else if (EVVoipCall.EndReason.TIMEOUT == reason) {
                                Log.e(TAG, "呼叫超时");
                            } else if (EVVoipCall.EndReason.REJECTED == reason) {
                                Log.e(TAG, "挂断电话");
                            } else if (EVVoipCall.EndReason.UNKNOW == reason) {
                                Log.e(TAG, "未知错误:" + reason.getCode());
                            }
                        }
                        finish();
                    }
                }
            });
        }
    }

    private class HandleThread extends HandlerThread {

        public HandleThread() {
            super("EVVoipSDK", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        }
    }

//    private class InComingHandler implements EVVoipManager.IncomingCallback {
//
//        @Override
//        public void inComing(EVVoipCall call) {
//            EVVoipAccount account = call.getRemoteAccount();
//            Log.d("weiju_sdk", "新来电 " + (account != null ? account.getUsername() : "") + " code:" + call.hashCode());
//
//            if (evVoipCall == null) {
//                evVoipCall = call;
//                setupCallStateCallback();
//
//                /*更新通话信息*/
//                if (EVVoipCall.CallDirection.INCOMING == evVoipCall.getDirection()) {
//                    callInfoView.setText("来电 " + evVoipCall.getRemoteAccount().getUsername());
//                }
//
//
//                myHandler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        try {
//                            evVoipCall.accept(displayView);
//                        } catch (EVVoipException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            } else {
//                //TODO 呼入多路通话，根据需求自行处理，同一时间内只允许接听一个通话
//            }
//        }
//
//    }
}

