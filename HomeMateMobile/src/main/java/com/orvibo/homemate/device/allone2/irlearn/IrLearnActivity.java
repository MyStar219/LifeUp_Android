package com.orvibo.homemate.device.allone2.irlearn;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.IrApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.data.DeviceOrder;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.AlloneLearnedEvent;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.PartViewEvent;
import com.orvibo.homemate.util.MathUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.LinearTipView;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

import de.greenrobot.event.EventBus;

/**
 * Created by snow on 2016/4/11.
 * 红外学习按钮
 */
public class IrLearnActivity extends BaseControlActivity {
    private KKIr kkIr;
    private static final int TIME = 15;//倒计时时间
    private CountDownTimer countDownTimer;
    private LinearTipView tipView;
    private com.orvibo.homemate.view.custom.NavigationGreenBar nbTitle;
    private android.widget.TextView deleteIr;
    private boolean isLearned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ir_learn);
        this.deleteIr = (TextView) findViewById(R.id.deleteIr);
        deleteIr.setOnClickListener(this);
        this.nbTitle = (NavigationGreenBar) findViewById(R.id.nbTitle);
        tipView = (LinearTipView) findViewById(R.id.tipView);
        kkIr = (KKIr) getIntent().getSerializableExtra(IntentKey.ALL_ONE_IR_KEY);
        nbTitle.setText(deviceName);
        tipView.setText(String.format(getString(R.string.allone_learn_tip1), TIME));
        tipView.isShowDeleteBtn(false);
        isLearned = getIntent().getBooleanExtra(IntentKey.IS_START_LEARN, false);
        if (!isLearned) {
            deleteIr.setVisibility(View.GONE);
        }
        startLearn();
    }

    /**
     * 红外学习
     */
    private void startLearn() {
        showDialog();
        //红外进入学习状态
        IrApi.irDeviceToLearn(userName, uid, deviceId, DeviceOrder.IR_CONTROL, kkIr.getFid(), kkIr.getfKey(), kkIr.getfName(), new BaseResultListener() {
            @Override
            public void onResultReturn(BaseEvent baseEvent) {
                dismissDialog();
                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                    //加100毫秒，防止到1时，有少许偏差，实际少于1秒不执行onTick方法
                    countDownTimer = new CountDownTimer(TIME * 1000 + 100, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            tipView.setText(String.format(getString(R.string.allone_learn_tip1), MathUtil.getRoundData((float) millisUntilFinished / 1000)));
                        }

                        @Override
                        public void onFinish() {
                            finish();
                        }
                    }.start();
                } else {
                    ToastUtil.toastError(baseEvent.getResult());
                    finish();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteIr:
                showDialog();
                //删除所学的红外码
                IrApi.deleteIrKey(userName, uid, null, kkIr.getKkIrId(), 1, new BaseResultListener() {
                    @Override
                    public void onResultReturn(BaseEvent baseEvent) {
                        cancelLearn();
                        dismissDialog();
                        if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                            EventBus.getDefault().post(new PartViewEvent());
                            finish();
                        } else {
                            ToastUtil.showToast(
                                    ErrorMessage.getError(mContext, baseEvent.getResult()),
                                    ToastType.ERROR, ToastType.SHORT);
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void leftTitleClick(View v) {
        cancelLearn();
        finish();
    }

    @Override
    public void onBackPressed() {
        cancelLearn();
        finish();
    }

    /**
     * 退出时将红外转发器退出学习状态
     */
    private void cancelLearn() {
        IrApi.irDeviceToOut(userName, uid, deviceId, new BaseResultListener() {
            @Override
            public void onResultReturn(BaseEvent baseEvent) {
            }
        });
    }

    public void onEventMainThread(AlloneLearnedEvent event) {
        if (event.getResult() == ErrorCode.SUCCESS) {
            EventBus.getDefault().post(new PartViewEvent());
            finish();
        } else {
            ToastUtil.showToast(
                    ErrorMessage.getError(mContext, event.getResult()),
                    ToastType.ERROR, ToastType.SHORT);
        }
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null)
            countDownTimer.cancel();
        super.onDestroy();
    }
}
