package com.orvibo.homemate.view.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Acpanel;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.DeviceIrDao;
import com.orvibo.homemate.data.AppDeviceId;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.ErrorMessage;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.model.StartLearning;
import com.orvibo.homemate.model.StartLearningResult;
import com.orvibo.homemate.model.StopLearning;
import com.orvibo.homemate.model.control.ControlDevice;
import com.orvibo.homemate.util.DeviceUtil;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.util.VibratorUtil;
import com.orvibo.homemate.view.dialog.IrLearnToastDialog;
import com.orvibo.homemate.view.dialog.MyProgressDialog;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

/**
 * @author smagret
 *         <p/>
 *         红外设备button
 *         <p/>
 *         长按button给主机发送红外转发器进入学习状态接口，按下红外遥控器按键，红外转发器收到红外码后发送给主机。
 *         主机收到红外转发器发的红外码之后，生成一条设备红外码表数据，之后主机给客户端发送红外码学习完成接口，
 *         客户端收到后将这一条设备红外码表数据，存入数据库。（红外转发器不保存红外码）
 */
public class IrButton extends Button {
    private static final String TAG = IrButton.class.getSimpleName();

    private Context mContext;
    private String order;
    private DeviceIrDao deviceIrDao;
    private DeviceIr deviceIr;
    private String deviceId;
    private String uid;
    private String userName;
    private StartLearningControl startLearningControl;
    private StopLearningControl stopLearningControl;
    private IrLearnToastDialog irLearnToastDialog;
    private String keyName;
    private Activity activity;
    private Drawable unLearnedBg;
    private Drawable learnedBg;
    private Drawable checkedBg;
    private VibratorUtil vibratorUtil = new VibratorUtil();
    private OnLearningResultListener onLearningResultListener;
    private OnControlResultListener onControlResultListener;
    private OnCheckedResultListener onCheckedResultListener;
    private Dialog progressDialog;
    private ControlDeviceControl controlDeviceControl;
    private StartLearningResultControl startLearningResultControl;
    private DeviceDao mDeviceDao;
    //进度对话框
    private ProgressDialogFragment mDialogFragment;
    /**
     * 该按键是否被学习
     */
    private boolean IS_LEARNED = false;

    /**
     * 该按键是否被选中
     */
    private boolean IS_CHECKED = false;

    /**
     * true学习红外码时可以红外码先回来，然后学习接口才回来。
     */
    private volatile boolean isHasCallback = false;

    private ConfirmAndCancelPopup mConfirmAndCancelPopup;
    private Acpanel mAcpanel;
    private Device mAcDevice;
    protected int mColor;
    protected int tmp;

    public IrButton(Context context) {
        super(context);
        mContext = context;
    }

    public IrButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init(attrs);
    }

    public IrButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
        TypedArray a = mContext.obtainStyledAttributes(attrs,
                R.styleable.IrButton);
        order = a.getString(R.styleable.IrButton_command);
        keyName = a.getString(R.styleable.IrButton_keyName);
        unLearnedBg = a.getDrawable(R.styleable.IrButton_unLearnedBg);
        learnedBg = a.getDrawable(R.styleable.IrButton_learnedBg);
        checkedBg = a.getDrawable(R.styleable.IrButton_checkedBg);
        mDeviceDao = new DeviceDao();
        a.recycle();
        //refresh();
    }

    public void initStatus(Activity activity, String uid, String userName, String deviceId) {
        this.activity = activity;
        this.uid = uid;
        this.deviceId = deviceId;
        this.userName = userName;
        irLearnToastDialog = new IrLearnToastDialog(activity) {
            @Override
            public void stopLearn() {
                learnFail();
            }
        };
        initACDevice();
        //  LogUtil.d(TAG, "initStatus(" + this + ")- uid = " + uid + " deviceId = " + deviceId + "userName = " + userName + ",text:" + getText());
        refresh();
    }

    private void initACDevice() {
        mAcDevice = mDeviceDao.selDevice(deviceId);

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public void onlongClick() {
        vibratorUtil.setVirbrator(mContext);
        refresh();
        if (IS_LEARNED) {
            showConfirmAndCancelPopup();
        } else {
            showRequestLearnProgressDialog();
            startLearning();
        }
    }

    private void initProgressDialog() {
//        mDialogFragment = ProgressDialogFragment.newInstance();
        mDialogFragment = new ProgressDialogFragment();
        mDialogFragment.setContent(activity.getString(R.string.ir_learn_request_learn));
        mDialogFragment.setOnCancelClickListener(new ProgressDialogFragment.OnCancelClickListener() {
            @Override
            public void onCancelClick(View view) {
                if (startLearningControl != null) {
                    startLearningControl.cancel();
                }
                learnFail();
            }
        });
    }

    private void showRequestLearnProgressDialog() {
        if (mDialogFragment == null) {
            initProgressDialog();
        }
        mDialogFragment.show(activity.getFragmentManager(), getClass().getName());
    }

    private void dismissProgressDialog() {
        if (mDialogFragment != null) {
            try {
                mDialogFragment.dismissAllowingStateLoss();
            } catch (Exception e) {
            }
        }
    }

    private void startLearning() {
        isHasCallback = false;
        if (startLearningResultControl == null) {
            startLearningResultControl = new StartLearningResultControl(mContext);
        }
        startLearningResultControl.acceptEvent();
        if (startLearningControl == null) {
            startLearningControl = new StartLearningControl(mContext);
        }
        startLearningControl.startStartLearning(uid, userName, deviceId, order, 0, null, null);
    }

    private void showConfirmAndCancelPopup() {
        if (mConfirmAndCancelPopup == null) {
            mConfirmAndCancelPopup = new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    dismiss();
                    showRequestLearnProgressDialog();
                    startLearning();
                }

                @Override
                public void cancel() {
                    dismiss();
                }
            };
        }
        mConfirmAndCancelPopup.showPopup(mContext, R.string.ir_already_learned, R.string.yes, R.string.no);
    }

    public boolean isLearned() {
        return IS_LEARNED;
    }

    public void onClick() {
        if ((mAcDevice != null && mAcDevice.getDeviceType() == DeviceType.AC_PANEL)
                || (mAcDevice != null && mAcDevice.getDeviceType() == DeviceType.AC && mAcDevice.getAppDeviceId() == AppDeviceId.AC_WIIF)) {
            //空调面板等属性报告回来再刷新状态
        } else {
            refresh();
        }
        vibratorUtil.setVirbrator(mContext);
        if (IS_LEARNED) {
            if (!DeviceUtil.isControlNoDefaultResponeByOrder(order)) {
                if (progressDialog == null) {
                    progressDialog = MyProgressDialog.getMyDialog(mContext);
                }
                progressDialog.show();
            }
            LogUtil.d(TAG, "onClick()- uid = " + uid + " deviceId = " + deviceId + "order = " + order);
            if (controlDeviceControl == null) {
                controlDeviceControl = new ControlDeviceControl(mContext);
            }
            if ((mAcDevice != null && mAcDevice.getDeviceType() == DeviceType.AC_PANEL)
                    || (mAcDevice != null && mAcDevice.getDeviceType() == DeviceType.AC && mAcDevice.getAppDeviceId() == AppDeviceId.AC_WIIF)) {
                controlDeviceControl.acControl(userName, uid, deviceId, order, mAcpanel.getValue1(), mAcpanel.getValue2(), mAcpanel.getValue3(), mAcpanel.getValue4());
            } else {
                controlDeviceControl.irControl(userName, uid, deviceId, order);
            }

        } else {
            if (DeviceUtil.isIrDevice(uid, deviceId)) {
                String message = getResources().getString(R.string.ir_key_not_learned);
                ToastUtil.showToast(
                        message,
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    public void onChecked() {
        if (IS_LEARNED) {
            IS_CHECKED = true;
        } else {
            IS_CHECKED = false;
        }
        vibratorUtil.setVirbrator(mContext);
        refresh();
        if (!IS_LEARNED) {
            if (DeviceUtil.isIrDevice(uid, deviceId)) {
                String message = getResources().getString(R.string.ir_key_not_learned);
                ToastUtil.showToast(
                        message,
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
        onCheckedResultListener.onCheckedResult(keyName, order, IS_LEARNED);
    }

    public void onUnChecked() {
        IS_CHECKED = false;
        refresh();
    }

    public void refresh() {
        // 判断是否是空调面板,wifi空调
        if ((mAcDevice != null && mAcDevice.getDeviceType() == DeviceType.AC_PANEL) || (mAcDevice != null && mAcDevice.getDeviceType() == DeviceType.AC && mAcDevice.getAppDeviceId() == AppDeviceId.AC_WIIF)) {
            setBackgroundDrawable(learnedBg);
            return;
        }
        if (StringUtil.isEmpty(order)) {
            setBackgroundDrawable(unLearnedBg);
            IS_LEARNED = false;
        } else {
            if (deviceIrDao == null) {
                deviceIrDao = new DeviceIrDao();
            }
            deviceIr = deviceIrDao.selDeviceIr(uid, deviceId, order);

            if (deviceIr != null && deviceIr.getIr() != null && deviceIr.getIr().length > 0) {
                setBackgroundDrawable(learnedBg);

                if (order.contains("311")) {
                    //空调温度字体不需要设置颜色
                    int temperature = getTemperatureByOrder(order);
                    if (temperature > 15) {
                        //选择温度，不需要修改文字颜色，使用彩色就表示该按键已经学习
                    } else {
                        setTextColor(getResources().getColor(R.color.font_learned_white));
                    }
                } else if (order.equals("310124") || order.equals("310224")) {
                    setTextColor(getResources().getColor(R.color.green));
                } else {
                    setTextColor(getResources().getColor(R.color.font_learned_white));
                }
                IS_LEARNED = true;
            } else {
                setBackgroundDrawable(unLearnedBg);
                IS_LEARNED = false;
                setTextColor(getResources().getColor(R.color.font_white_gray));
            }

            if (IS_CHECKED) {
                setBackgroundDrawable(checkedBg);
            }
        }
        LogUtil.d(TAG, "refresh()- uid = " + uid + " deviceId = " + deviceId + " userName = " + userName + " order = " + order + " IS_LEARNED = " + IS_LEARNED);
    }

    public void learnFail() {
        LogUtil.d(TAG, "learnFail()");
        if (startLearningResultControl != null) {
            startLearningResultControl.stop();
        }

        if (stopLearningControl == null) {
            stopLearningControl = new StopLearningControl(mContext);
        }
        stopLearningControl.stopStartLearning(uid, userName, deviceId);
//        String message = getResources().getString(R.string.ir_learn_fail);
//        ToastUtil.showToast(
//                message,
//                ToastType.NULL, ToastType.SHORT);
    }


    private class StartLearningControl extends StartLearning {

        public StartLearningControl(Context context) {
            super(context);
        }

        @Override
        public void onStartLearningResult(String uid, int serial, int result) {
            LogUtil.d(TAG, "onStartLearningResult() - result = " + result);
            dismissProgressDialog();
            if (result == ErrorCode.SUCCESS) {
                if (irLearnToastDialog != null && irLearnToastDialog.isShowing()) {
                    irLearnToastDialog.dismiss();
                }
                if (!isHasCallback) {
                    irLearnToastDialog.show(activity, keyName);
                }
            } else if (result == ErrorCode.STORAGE_FULL_ERROR) {
                ToastUtil.showToast(
                        ErrorMessage.getError(mContext, ErrorCode.STORAGE_FULL_ERROR),
                        ToastType.ERROR, ToastType.SHORT);
            } else {
                if (!ToastUtil.toastCommonError(result)) {
                    ToastUtil.showToast(
                            ErrorMessage.getError(mContext, ErrorCode.ERROR_SL),
                            ToastType.ERROR, ToastType.SHORT);
                }
            }
        }
    }

    private class StartLearningResultControl extends StartLearningResult {

        private StartLearningResultControl(Context context) {
            super(context);
        }

        @Override
        public void onStartLearningResult(int status) {
//            if (simplePopup != null && simplePopup.isShowing()) {
//                simplePopup.dismiss();
//            }
            isHasCallback = true;
            if (irLearnToastDialog != null && irLearnToastDialog.isShowing()) {
                irLearnToastDialog.dismiss();
            }
            dismissProgressDialog();
            if (onLearningResultListener != null) {
                onLearningResultListener.onLearningResult(status);
               // onLearningResultListener.onLearningResult(status, mColor, tmp);
            }
            if (status == ErrorCode.SUCCESS) {
                String message = getResources().getString(R.string.ir_learn_success);
                ToastUtil.showToast(
                        message,
                        ToastType.RIGHT, ToastType.SHORT);
                refresh();
            } else {
                ToastUtil.showToast(
                        ErrorMessage.getError(mContext, status),
                        ToastType.ERROR, ToastType.SHORT);
            }
        }
    }

    private class StopLearningControl extends StopLearning {

        private StopLearningControl(Context context) {
            super(context);
        }

        @Override
        public void onStopLearningResult(String uid, int serial, int result) {
            LogUtil.d(TAG, "onStopLearningResult() - result = " + result);
        }
    }

    private class ControlDeviceControl extends ControlDevice {

        public ControlDeviceControl(Context context) {
            super(context);
        }

        @Override
        public void onControlDeviceResult(String uid, String deviceId, int result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (onControlResultListener != null) {
                onControlResultListener.onControlResult(result);
            }
            unregisterEvent(this);
            if (result == ErrorCode.SUCCESS) {
                if (mAcDevice.getDeviceType() == DeviceType.AC_PANEL || mAcDevice.getDeviceType() == DeviceType.AC && mAcDevice.getAppDeviceId() == AppDeviceId.AC_WIIF) {
                    return;
                }
                ToastUtil.showToast(
                        getResources().getString(R.string.control_success),
                        ToastType.NULL, ToastType.SHORT);

            } else {
//                ToastUtil.showToast(
//                        getResources().getString(R.string.control_fail),
//                        ToastType.NULL, ToastType.SHORT);
                ToastUtil.toastError(result);
            }
        }

    }


    private int getTemperatureByOrder(String order) {
        String temperatureString = order.substring(4, 6);
        return Integer.parseInt(temperatureString);
    }

//    public void setDrawable(Drawable unLearnedBg, Drawable learnedBg, Drawable checkedBg) {
//        this.unLearnedBg = unLearnedBg;
//        this.learnedBg = learnedBg;
//        this.checkedBg = checkedBg;
//    }

    public void setLearnDrawable(Drawable learnedBg) {
        this.learnedBg = learnedBg;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrder() {
        return order;
    }

    /**
     * @param onLearningResultListener
     */
    public void setOnLearningResultListener(OnLearningResultListener onLearningResultListener) {
        this.onLearningResultListener = onLearningResultListener;
    }

    public interface OnLearningResultListener {

        /**
         * On Learning Result
         *
         * @param result the learning result
         */
        void onLearningResult(int result);

      //  void onLearningResult(int result, int color, int temperature);

    }

    /**
     * @param onControlResultListener
     */
    public void setOnControlResultListener(OnControlResultListener onControlResultListener) {
        this.onControlResultListener = onControlResultListener;
    }

    public interface OnControlResultListener {

        /**
         * On control Result
         *
         * @param result the control result
         */
        void onControlResult(int result);
    }

    /**
     * @param onCheckedResultListener
     */
    public void setOnCheckedResultListener(OnCheckedResultListener onCheckedResultListener) {
        this.onCheckedResultListener = onCheckedResultListener;
    }

    public interface OnCheckedResultListener {

        /**
         * On control Result
         *
         * @param order the order
         */
        void onCheckedResult(String keyName, String order, boolean is_learned);
    }

    /**
     * 当空调关闭时，所有的IrButton置灰
     */
    public void setCloseStatus(boolean isClose) {
        if (isClose) {
            setBackgroundDrawable(unLearnedBg);
        } else {
            setBackgroundDrawable(learnedBg);
        }
    }

    /**
     * wifi的空调面板不需要学习
     *
     * @param isLearned
     */
    public void setLearnedStatus(boolean isLearned) {
        IS_LEARNED = isLearned;
    }

    /**
     * 控制wifi空调面板时把Acpanel传递过来
     *
     * @param acPanel
     */
    public void setACPanel(Acpanel acPanel) {
        this.mAcpanel = acPanel;
    }

}

