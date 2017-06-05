package com.orvibo.homemate.device.smartlock;

import android.content.ClipboardManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.DeviceApi;
import com.orvibo.homemate.api.listener.BaseResultListener;
import com.orvibo.homemate.bo.AuthUnlockData;
import com.orvibo.homemate.bo.DoorUserData;
import com.orvibo.homemate.bo.PayloadData;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.AuthUnlockDao;
import com.orvibo.homemate.dao.DoorUserDao;
import com.orvibo.homemate.data.DeleteFlag;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.event.BaseEvent;
import com.orvibo.homemate.event.ViewEvent;
import com.orvibo.homemate.sharedPreferences.SmartLockCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ActivityJumpUtil;
import com.orvibo.homemate.util.DateUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.popup.CommonPopup;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.util.Map;

/**
 * 智能门锁结果操作界面
 * Created by snown on 2015/11/27.
 */
public class AuthLockResultActivity extends BaseControlActivity {

    private TextView textLaveTime;
    private TextView textLockTip;
    private TextView btnLockCancel;
    private TextView btnLockResend;
    private AuthUnlockData authUnlockData;
    private DoorUserData doorUserData;
    private static final int MSG_SMS_SEND_DEALY = 1;
    private int diffSecendTime;
    private CountDownTimer countDownTimer;
    private StringBuilder phoneInfo;
    private ConfirmAndCancelPopup confirmAndCancelPopup;
    private TextView smsCode;
    private TextView codeCopy;
    private boolean isBLLock;//是否是霸陵门锁
    private boolean isCn;//是否是中文

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isBLLock = ProductManage.isBLLock(device);
        isCn = PhoneUtil.isCN(mContext);
        if (isBLLock || !isCn) {
            setContentView(R.layout.activity_auth_lock_result);
        } else {
            setContentView(R.layout.activity_auth_lock_result_cn);
        }
        initView();
    }

    private void initView() {
        authUnlockData = (AuthUnlockData) getIntent().getSerializableExtra(IntentKey.AUTH_UNLOCK_DATA);
        doorUserData = (DoorUserData) getIntent().getSerializableExtra(IntentKey.DOOR_USER_DATA);
        if (doorUserData == null) {
            doorUserData = DoorUserDao.getInstance().getDoorUser(authUnlockData.getDeviceId(), authUnlockData.getAuthorizedId());
        }
        btnLockResend = (TextView) findViewById(R.id.btnLockResend);
        btnLockResend.setOnClickListener(this);
        setSmsResendBg();
        btnLockCancel = (TextView) findViewById(R.id.btnLockCancel);
        btnLockCancel.setOnClickListener(this);
        textLockTip = (TextView) findViewById(R.id.textLockTip);
        phoneInfo = new StringBuilder();
        String phoneName = null;
        String phone = authUnlockData.getPhone();
        if (doorUserData != null) {
            phoneName = TextUtils.isEmpty(doorUserData.getName()) ? "" : doorUserData.getName();
        }
        if (TextUtils.isEmpty(phoneName)) {
            Map<String, String> hashPhone = SmartLockCache.getInstance().getPhoneInfo(UserCache.getCurrentMainUid(getApplicationContext()));
            phoneName = hashPhone.get(phone);
            if (TextUtils.isEmpty(phoneName)) {
                phoneName = PhoneUtil.getContactName(phone);
            }
        }
        phoneName = StringUtil.splitString(phoneName);
        phoneInfo.append(phoneName);
        textLockTip.setText(String.format(getString(R.string.smart_lock_result_tip), phoneInfo.toString()));
        textLaveTime = (TextView) findViewById(R.id.textLaveTime);
        diffSecendTime = authUnlockData.getTime() * 60 - (DateUtil.getUTCTime() - authUnlockData.getStartTime());
        long ms = (long) diffSecendTime * 1000;
        textLaveTime.setText(isCn && !isBLLock ? dealWithTime(diffSecendTime * 1000) : dealWithEnglisTime(ms));
        countDownTimer = new CountDownTimer(ms, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textLaveTime.setText(isCn && !isBLLock ? dealWithTime(millisUntilFinished) : dealWithEnglisTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                onBackPressed();
            }
        }.start();
        initEnglishView();
    }

    /**
     * 海外门锁版本
     */
    private void initEnglishView() {
        if (!isCn || isBLLock) {
            smsCode = (TextView) findViewById(R.id.smsCode);
            codeCopy = (TextView) findViewById(R.id.codeCopy);
            codeCopy.setOnClickListener(this);
            if (!TextUtils.isEmpty(getCode()))
                smsCode.setText(getCode());
            else
                codeCopy.setVisibility(View.GONE);
            if (doorUserData != null) {
                phoneInfo.setLength(0);
                phoneInfo.append(TextUtils.isEmpty(doorUserData.getName()) ? "" : doorUserData.getName());
                textLockTip.setText(String.format(getString(R.string.smart_lock_result_tip1), phoneInfo.toString()));
            }
        }
    }

    /**
     * 授权时间倒计时展示模块
     *
     * @param ms
     * @return
     */
    private SpannableString dealWithTime(long ms) {
        Long times[] = TimeUtil.formatTime(ms);
        Long hour = times[0];
        Long minute = times[1];
        Long secend = times[2];
        String h = getString(R.string.time_hours);
        String m = getString(R.string.time_minutes);
        String s = getString(R.string.time_second);
        String suffix = getString(R.string.common_lave);
        StringBuilder stringBuilder = new StringBuilder();
        if (hour == 0) {
            stringBuilder.append(suffix).append(minute).append(m).append(secend).append(s);
            SpannableString spanString = new SpannableString(stringBuilder);
            spanString.setSpan(new AbsoluteSizeSpan(45, true), suffix.length(), stringBuilder.lastIndexOf(m), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), suffix.length(), stringBuilder.lastIndexOf(m), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanString.setSpan(new AbsoluteSizeSpan(45, true), stringBuilder.lastIndexOf(secend + s), stringBuilder.lastIndexOf(secend + s) + String.valueOf(secend).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), stringBuilder.lastIndexOf(secend + s), stringBuilder.lastIndexOf(secend + s) + String.valueOf(secend).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spanString;
        } else {
            stringBuilder.append(suffix).append(hour).append(h).append(minute).append(m);
            SpannableString spanString = new SpannableString(stringBuilder);
            spanString.setSpan(new AbsoluteSizeSpan(45, true), suffix.length(), stringBuilder.lastIndexOf(h), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), suffix.length(), stringBuilder.lastIndexOf(h), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanString.setSpan(new AbsoluteSizeSpan(45, true), stringBuilder.lastIndexOf(minute + m), stringBuilder.lastIndexOf(minute + m) + String.valueOf(minute).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanString.setSpan(new StyleSpan(Typeface.BOLD), stringBuilder.lastIndexOf(minute + m), stringBuilder.lastIndexOf(minute + m) + String.valueOf(minute).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spanString;
        }
    }

    /**
     * 授权时间倒计时展示模块
     *
     * @param ms
     * @return
     */
    private String dealWithEnglisTime(long ms) {
        if (isBLLock) {
            Long times[] = TimeUtil.formatTimeDay(ms);
            Long day = times[0];
            Long hour = times[1];
            Long minute = times[2];
            Long secend = times[3];
            if (day == 0)
                return String.format("%d:%d:%d", hour, minute, secend);
            else if (hour == 0 && day == 0)
                return String.format("%d:%d", minute, secend);
            return String.format("%d%s%d%s%d%s", day, getString(R.string.day), hour, getString(R.string.time_hours), minute, getString(R.string.time_minute));
        } else {
            Long times[] = TimeUtil.formatTime(ms);
            Long hour = times[0];
            Long minute = times[1];
            Long secend = times[2];
            if (hour == 0)
                return String.format("%d:%d", minute, secend);
            return String.format("%d:%d:%d", hour, minute, secend);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLockResend:
                if (!authUnlockData.isSmsResend()) {
                    showDialog();
                    DeviceApi.authResendUnlock(UserCache.getCurrentUserName(getApplicationContext()), UserCache.getCurrentMainUid(getApplicationContext()), deviceId, authUnlockData.getPhone(), new BaseResultListener() {
                        @Override
                        public void onResultReturn(BaseEvent baseEvent) {
                            dismissDialog();
                            int result = baseEvent.getResult();
                            if (result == ErrorCode.AUTH_UNLOCK_RESEND_NO_DATA) {
                                ToastUtil.toastError(result);
                                onBackPressed();
                            } else if (result == ErrorCode.SUCCESS) {
                                authUnlockData.setIsSmsResend(true);
                                setSmsResendBg();
                                AuthUnlockDao.getInstance().insertAuthUnlock(authUnlockData);
                                ToastUtil.showToast(getString(R.string.success));
                            } else {
                                ToastUtil.toastError(result);
                            }
                        }
                    });
                }
                break;
            case R.id.btnLockCancel:
                DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                dialogFragmentTwoButton.setTitle(getString(R.string.smart_lock_auth_cancel));
                dialogFragmentTwoButton.setContent(getString(R.string.dialog_lock_cancel_content));
                dialogFragmentTwoButton.setRightButtonText(getString(R.string.smart_lock_auth_cancel));
                dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                    @Override
                    public void onLeftButtonClick(View view) {

                    }

                    @Override
                    public void onRightButtonClick(View view) {
                        showDialog();
                        DeviceApi.authCancelUnlock(UserCache.getCurrentUserName(getApplicationContext()), UserCache.getCurrentMainUid(getApplicationContext()), authUnlockData.getAuthorizedUnlockId(), new BaseResultListener() {
                            @Override
                            public void onResultReturn(BaseEvent baseEvent) {
                                dismissDialog();
                                if (baseEvent.getResult() == ErrorCode.SUCCESS) {
                                    ToastUtil.showToast(getString(R.string.success));
                                    AuthUnlockDao.getInstance().delAuthUnlock(authUnlockData);
                                    if (doorUserData != null) {
                                        doorUserData.setDelFlag(DeleteFlag.DELETED);
                                        doorUserData.setUpdateTime(System.currentTimeMillis());
                                        DoorUserDao.getInstance().insertDoorUser(doorUserData);
                                    }
                                    onBackPressed();
                                } else {
                                    ToastUtil.toastError(baseEvent.getResult());
                                }
                            }
                        });
                    }
                });
                dialogFragmentTwoButton.show(getFragmentManager(), "");
                break;
            case R.id.codeCopy:
                ClipboardManager c = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                c.setText(getCode());
                ToastUtil.showToast(R.string.copy_success);
                break;
        }

    }

    /**
     * 获取短信验证码
     *
     * @return
     */
    private String getCode() {
        if (authUnlockData != null)
            return SmartLockCache.getSmsCode(deviceId + authUnlockData.getAuthorizedUnlockId());
        return null;
    }


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SMS_SEND_DEALY:
                    btnLockResend.setClickable(true);
                    break;
            }
            return false;
        }
    });

    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (countDownTimer != null)
            countDownTimer.cancel();
        super.onDestroy();
    }

    public void onPropertyReport(String deviceId, int statsType, int value1, int value2, int value3, int value4, int alarmType, PayloadData payloadData) {
        if (payloadData != null && deviceId.equalsIgnoreCase(this.deviceId)) {
            if (payloadData.getAuthorizedId() == authUnlockData.getAuthorizedId() && payloadData.getType() == DoorUserDao.TYPE_TMP_USER) {
                authUnlockData.setDelFlag(DeleteFlag.DELETED);
                AuthUnlockDao.getInstance().insertAuthUnlock(authUnlockData);
                showDialogTip();
            }
        }
    }

    private void setSmsResendBg() {
        if (authUnlockData.isSmsResend()) {
            Drawable topDrawable = getResources().getDrawable(R.drawable.wait_icon_resend_pressed);
            topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
            btnLockResend.setCompoundDrawables(null, topDrawable, null, null);
        }
    }

    /**
     * 弹出开锁提示框
     */
    private void showDialogTip() {
        if (confirmAndCancelPopup == null) {
            confirmAndCancelPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                    onBackPressed();
                }
            };
            confirmAndCancelPopup.setOnPopupDismissListener(new CommonPopup.OnPopupDismissListener() {
                @Override
                public void onPopupDismiss() {
                    onBackPressed();
                }
            });
        }
        if (confirmAndCancelPopup != null && !confirmAndCancelPopup.isShowing()) {
            confirmAndCancelPopup.showPopup(mContext, getString(R.string.lock_open_success), String.format(getString(R.string.smart_lock_open_result_tip), phoneInfo.toString()), getString(R.string.device_set_find_btn), null);
        }
    }


    @Override
    protected void onRefresh(ViewEvent event) {
        if (confirmAndCancelPopup != null && confirmAndCancelPopup.isShowing()) {
            return;
        } else {
            AuthUnlockData authUnlockData = AuthUnlockDao.getInstance().getAvailableAuth(deviceId);
            if (!AuthUnlockDao.getInstance().isAvailableData(authUnlockData)) {
                onBackPressed();
            }
        }
    }

    @Override
    public void leftTitleClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        //点击返回按钮时回到门锁记录界面
        ActivityJumpUtil.jumpActFinish(AuthLockResultActivity.this, LockRecordActivity.class);
        super.onBackPressed();
    }
}


