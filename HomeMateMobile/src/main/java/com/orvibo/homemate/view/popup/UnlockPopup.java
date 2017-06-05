package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.sharedPreferences.LockCache;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.MD5;
import com.orvibo.homemate.view.custom.EditTextWithCompound;

/**
 * 对话框
 *
 * @author smagret
 */
public abstract class UnlockPopup extends CommonPopup {
    private final String TAG = UnlockPopup.class.getSimpleName();
    private Context mContext;
    private TextView tvForgetPassword;
    private TextView tipsTextView;
    private TextView deleteTextView;
    private EditText etPassword;
    private Button btnConfirm;
    private Button btnCancel;

    private long currentTime;
    private long lockTime;
    private int remainingTime;

    private String userName;
    private String title;
    private String yes;
    private ControlDevice mControlDevice;
    private String uid;
    private String deviceId;

    /**
     * @param context
     */
    public void showPopup(Context context, ControlDevice mControlDevice, String uid, String deviceId) {
        this.mControlDevice = mControlDevice;
        this.uid = uid;
        this.deviceId = deviceId;
        mContext = context;
        userName = UserCache.getCurrentUserName(mContext);
        View contentView = LayoutInflater.from(context).inflate(R.layout.unlock_popup,
                null);
        tvForgetPassword = (TextView) contentView.findViewById(R.id.tvForgetPassword);
        tipsTextView = (TextView) contentView.findViewById(R.id.tipsTextView);
        deleteTextView = (TextView) contentView.findViewById(R.id.deleteTextView);
        etPassword = (EditText) contentView.findViewById(R.id.etPassword);
        etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        btnConfirm = (Button) contentView.findViewById(R.id.btnConfirm);
        btnCancel = (Button) contentView.findViewById(R.id.btnCancel);
        btnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                open();
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });
        tvForgetPassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                forgetPassword();
            }
        });

        deleteTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dismiss();
            }
        });

        show(contentView, true);
    }

    /**
     * 点击确定按钮
     */
    public abstract void forgetPassword();

    private void open() {
        String password = etPassword.getText().toString();
        //add by Allen start
        if (TextUtils.isEmpty(password)) {//输入为空，不处理
            return;
        }
        //add by Allen end
        String md5Password = MD5.encryptMD5(password);
        int unLockTimes = LockCache.getUnLockTimes(mContext, userName);
        LogUtil.d(TAG, "open()　 password=" + password + "unLockTimes = "
                + unLockTimes);
        if (unLockTimes > 0) {
            unlock(md5Password, unLockTimes);
        } else if (unLockTimes == 0) {
            currentTime = System.currentTimeMillis() / 1000;// 当前手机时间
            lockTime = LockCache.getLockTime(mContext, userName);
            remainingTime = (int) (60 - (currentTime - lockTime) / 60);

            if (currentTime - lockTime >= 60 * 60) {
                LockCache.saveUnLockTimes(mContext, userName,
                        Constant.MAX_UNLOCKTIME);
                unLockTimes = Constant.MAX_UNLOCKTIME;
                unlock(md5Password, unLockTimes);
            } else {
                title = mContext.getResources().getString(R.string.lock_password_error_tips);
                String titleFinal = String.format(title, remainingTime);
                yes = mContext.getResources().getString(R.string.lock_know);
                showInputErrorPopup(titleFinal, yes);

                //inputErrorPopup.showPopup(LockActivity.this, titleFinal, yes, no);
            }
        }
    }

//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            mControlDevice.unlock(uid, deviceId);
//        }
//    };

    private void unlock(String password, int unLockTimes) {
        if (password.equals(UserCache.getMd5Password(mContext, userName))) {
            mControlDevice.unlock(uid, deviceId);
            LockCache.saveUnLockTimes(mContext, userName,
                    Constant.MAX_UNLOCKTIME);
//            handler.sendEmptyMessageDelayed(1,4 * 1000);
        } else if (unLockTimes > 1) {
            LockCache.saveUnLockTimes(mContext, userName, --unLockTimes);
            String str = mContext.getResources().getString(
                    R.string.lock_password_error);
            String sFinal = String.format(str, unLockTimes);
            tipsTextView.setText(sFinal);
            long currentTime = System.currentTimeMillis() / 1000;// 当前手机时间
            LockCache.saveLockTime(mContext, userName, currentTime);
        } else {
            LockCache.saveUnLockTimes(mContext, userName, --unLockTimes);
            title = mContext.getResources().getString(
                    R.string.lock_password_error_tips);
            yes = mContext.getResources().getString(
                    R.string.lock_know);
            String titleFinal = String.format(title, 60);
            showInputErrorPopup(titleFinal, yes);
        }
    }


    public abstract void showInputErrorPopup(String title, String yes);

}
