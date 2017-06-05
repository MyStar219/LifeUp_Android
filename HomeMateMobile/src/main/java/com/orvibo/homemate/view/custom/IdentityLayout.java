package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.CheckSmsCode;
import com.orvibo.homemate.model.GetSmsCode;
import com.orvibo.homemate.user.UserAgreementActivity;
import com.orvibo.homemate.util.InputUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Allen on 2015/4/14.
 * Modified bg smagret on 2015/05/20
 */
public class IdentityLayout extends LinearLayout implements View.OnClickListener {
    private BaseActivity baseActivity;
    private Context context;
    private TextView register_tips = null;
    private GetSmsCodeControl getSmsCodeControl;
    private CheckSmsCodeControl checkSmsCodeControl;

    private EditTextWithCompound phoneNumber_et;
    private EditTextWithCompound auth_code_et;
    private TextView send_tv;
    private Button register_next_btn;
    private int countDownTime = 60;
    private Timer timer;

    Handler handler = new MyHandler();
    /**
     * 发送获取短信验证码指令成功
     */
    private final int SEND_GET_SMSCODE_SUCCESS_MSG = 0;
    /**
     * 倒计时
     */
    private final int COUNTDOWN_MSG = 1;

    /**
     * 重置所有信息
     */
    private final int RESET_MSG = 2;
    private int getSmsType = -1;

    public IdentityLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        View view = View.inflate(context, R.layout.layout_identity, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(view);
        initView();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_GET_SMSCODE_SUCCESS_MSG:
                    // 设置发送验证码按钮可点击
                    send_tv.setBackgroundResource(R.drawable.button_green);
                    send_tv.setClickable(true);
                    phoneNumber_et.setFocusable(true);
                    phoneNumber_et.setClickable(true);
                    break;
                case COUNTDOWN_MSG:
                    // 设置发送验证码按钮倒计时
                    String countDown;
                    if (countDownTime == 0) {
                        cancleTimer();
                        send_tv.setBackgroundResource(R.drawable.button_green);
                        countDown = getResources().getString(
                                R.string.get_sms_code_again);
                        send_tv.setClickable(true);
                        phoneNumber_et.setClickable(true);
                        phoneNumber_et.setFocusable(true);
                        phoneNumber_et.setFocusableInTouchMode(true);
                        phoneNumber_et.showDeleteDrawable();
                    } else {
                        String second = getResources().getString(R.string.send_second);
                        countDown = String.format(second, countDownTime);
                        send_tv.setBackgroundResource(R.drawable.send_button_n);
                        send_tv.setClickable(false);
                        phoneNumber_et.setFocusable(false);
                        phoneNumber_et.setClickable(false);
                        phoneNumber_et.hideDeleteDrawable();
                    }
                    send_tv.setText(countDown);
                    break;

                case RESET_MSG: {
                    cancleTimer();
                    send_tv.setBackgroundResource(R.drawable.button_green);
                    countDown = getResources().getString(
                            R.string.send_auth_code);
                    send_tv.setText(countDown);
                    send_tv.setClickable(false);
                    phoneNumber_et.setText("");
                    auth_code_et.setText("");
                    phoneNumber_et.setClickable(true);
                    phoneNumber_et.setFocusable(true);
                    phoneNumber_et.setFocusableInTouchMode(true);
                }
                default:
                    break;
            }
        }
    }

    public void init(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    private void initView() {
        getSmsCodeControl = new GetSmsCodeControl(context);
        checkSmsCodeControl = new CheckSmsCodeControl(context);
        timer = new Timer();
        initphoneNumber();
        initRegisterTips();

        send_tv = (TextView) findViewById(R.id.send_tv);
        register_next_btn = (Button) findViewById(R.id.register_next_btn);
        initListener();
    }

    private void initRegisterTips() {
        register_tips = (TextView) this.findViewById(R.id.register_tips_tv);
        register_tips.setText(getClickableSpan());
        register_tips.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initphoneNumber() {
        phoneNumber_et = (EditTextWithCompound) findViewById(R.id.phoneNumber_et);
        auth_code_et = (EditTextWithCompound) findViewById(R.id.auth_code_et);
        phoneNumber_et.setOnInputListener(onInputListener);
    }

    private void initListener() {
        send_tv.setOnClickListener(this);
        send_tv.setClickable(false);
        register_next_btn.setOnClickListener(this);
    }

    public void hideRegisterTips() {
        register_tips.setVisibility(View.GONE);
    }

    public void setRegister() {
        getSmsType = GetSmsType.REGISTER;
    }

    public void setModifyPassword() {
        getSmsType = GetSmsType.MODIFY_PASSWORD;
    }

    private SpannableString getClickableSpan() {
        final String procotrol = context.getResources().getString(R.string.user_procotol);
        final String registerTip = String.format(context.getResources().getString(R.string.register_tip), procotrol);
        // 创建一个 SpannableString对象
        SpannableString spanableInfo = new SpannableString(registerTip);
        final int start = registerTip.length() - procotrol.length();
        final int end = registerTip.length();
        // 设置下划线
        spanableInfo.setSpan(new UnderlineSpan(), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spanableInfo.setSpan(new UnderlineSpan(), 19, 23,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 设置粗体
        spanableInfo.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start,
                end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置粗体
//        spanableInfo.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 19,
//                23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spanableInfo.setSpan(new Clickable(userAgreementClickListener), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

//        spanableInfo.setSpan(new Clickable(privacyAgreementclickListener), 19,
//                23, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置前景色
        spanableInfo.setSpan(
                new ForegroundColorSpan(getResources().getColor(
                        R.color.navigation_text_color)), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 设置前景色
//        spanableInfo.setSpan(
//                new ForegroundColorSpan(getResources().getColor(
//                        R.color.navigation_text_color)), 19, 23,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanableInfo;
    }

    class Clickable extends ClickableSpan implements OnClickListener {
        private final OnClickListener mListener;

        public Clickable(OnClickListener l) {
            mListener = l;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
    }

    OnClickListener userAgreementClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            context.startActivity(new Intent(context, UserAgreementActivity.class));
//            ToastUtil.showToast( "隐私协议", ToastType.RIGHT, 0);
        }
    };

    @Override
    public void onClick(View v) {
        final String authCode = auth_code_et.getText().toString();
        final String phoneNumber = phoneNumber_et.getText().toString();
        switch (v.getId()) {
            case R.id.send_tv:
                if (NetUtil.isNetworkEnable(context)) {
                    if (!InputUtil.isCorrectPhone(phoneNumber)) {
                        ToastUtil.showToast( R.string.PHONE_ERROR, ToastType.NULL, ToastType.SHORT);
                        return;
                    }
                    if (baseActivity != null) {
                        baseActivity.showDialog();
                    }
                    getSmsCodeControl.startGetSmsCode(phoneNumber_et.getText()
                            .toString(), getSmsType);
                } else {
                    ToastUtil.toastError( ErrorCode.NET_DISCONNECT);
                }
                break;
            case R.id.register_next_btn:
                // startActivity(new Intent(context,SetPasswordActivity.class));
                if (NetUtil.isNetworkEnable(context)) {
                    if (StringUtil.isEmpty(authCode)) {
                        ToastUtil.showToast( R.string.auth_code_error, ToastType.NULL, ToastType.SHORT);
                        return;
                    }
                    if (baseActivity != null) {
                        baseActivity.showDialog();
                    }
                    checkSmsCodeControl.startCheckSmsCode(phoneNumber, authCode);
                } else {
                    ToastUtil.toastError( ErrorCode.NET_DISCONNECT);
                }
                break;

            default:
                break;
        }
    }

    private class GetSmsCodeControl extends GetSmsCode {

        public GetSmsCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onGetSmsCodeResult(int result) {
            if (baseActivity != null) {
                baseActivity.dismissDialog();
            }
            if (result == ErrorCode.SUCCESS) {
                setTimer();

            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    private class CheckSmsCodeControl extends CheckSmsCode {

        public CheckSmsCodeControl(Context context) {
            super(context);
        }

        @Override
        public void onCheckSmsCodeResult(int result, String phoneNumber) {

            if (baseActivity != null) {
                baseActivity.dismissDialog();
            }
            if (onIdentityListener != null) {
                onIdentityListener.onIdentity(result, phoneNumber);
            }
            if (result == ErrorCode.SUCCESS) {

                if (handler.hasMessages(SEND_GET_SMSCODE_SUCCESS_MSG)) {
                    handler.removeMessages(SEND_GET_SMSCODE_SUCCESS_MSG);
                }
                if (handler.hasMessages(COUNTDOWN_MSG)) {
                    handler.removeMessages(COUNTDOWN_MSG);
                }

                if (handler.hasMessages(RESET_MSG)) {
                    handler.removeMessages(RESET_MSG);
                }
                handler.sendEmptyMessage(RESET_MSG);

            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mContext, result),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    private void setTimer() {
        countDownTime = 60;
        if (timer == null) {
            timer = new Timer();
        }

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                countDownTime--;
                if (countDownTime >= 0) {
                    handler.sendEmptyMessage(COUNTDOWN_MSG);
                }
            }
        }, 0, 1000);
    }

    private void cancleTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    private EditTextWithCompound.OnInputListener onInputListener = new EditTextWithCompound.OnInputListener() {

        @Override
        public void onRightful() {
            if(StringUtil.isPhone(phoneNumber_et.getText().toString())) {
                send_tv.setBackgroundResource(R.drawable.button_green);
                send_tv.setClickable(true);
            }
        }

        @Override
        public void onUnlawful() {
            send_tv.setBackgroundResource(R.drawable.send_button_n);
            send_tv.setClickable(false);
        }


        @Override
        public void onClearText() {

        }
    };

    private OnIdentityListener onIdentityListener;

    public void setOnIdentityListener(OnIdentityListener onIdentityListener) {
        this.onIdentityListener = onIdentityListener;
    }

    public interface OnIdentityListener {
        void onIdentity(int result, String phoneNum);
    }
}
