package com.orvibo.homemate.common;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnLogin365Listener;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.LoadDataType;
import com.orvibo.homemate.event.login.Login365Event;
import com.orvibo.homemate.model.login.LoginX;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.ProgressDialogFragment;

import java.util.List;

/**
 * 获取数据dialog
 *
 */
public class LoadDataActivity extends BaseActivity implements DialogInterface.OnCancelListener,
        OnLogin365Listener,
        DialogFragmentOneButton.OnButtonClickListener,
        ProgressDialogFragment.OnCancelClickListener {
    private static final String TAG = LoadDataActivity.class.getSimpleName();
    private DialogFragmentOneButton mDialogFragment;
    //        private Login365 mLogin365;
    private LoginX mLoginX;
    private String mUid;

    /**
     * true:需要返回主界面
     */
    private boolean isGoToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate()");

//        mLoginX = LoginX.getInstance(mAppContext);
        Intent intent = getIntent();
        init(intent);
        int loadDataType = intent.getIntExtra(IntentKey.LOAD_DATA_TYPE, LoadDataType.LOAD_DATA_NOT_EXIST);
        if (loadDataType == LoadDataType.LOAD_DATA_NOT_EXIST) {
            //数据不存在，弹框提示用户。当用户点击确认时执行读表操作。
            mDialogFragment = new DialogFragmentOneButton();
            mDialogFragment.setTitle(getString(R.string.warm_tip_data_no_exist));
            mDialogFragment.setContent(getString(R.string.DATA_NOT_EXIST_content));
            mDialogFragment.setOnCancelListener(this);
            mDialogFragment.setOnButtonClickListener(this);
            try {
                mDialogFragment.show(getFragmentManager(), "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (loadDataType == LoadDataType.LOAD_ALL) {
            showDialogNow(this);
            loadData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        init(intent);
    }

    private void init(Intent intent) {
        if (intent != null) {
            mUid = intent.getStringExtra(IntentKey.UID);
            isGoToMain = intent.getBooleanExtra(IntentKey.GOTOMAIN, false);
            LogUtil.d(TAG, "init()-uid:" + mUid + ",isGoToMain:" + isGoToMain);
        }
    }

    private void loadData() {
        if (NetUtil.isNetworkEnable(mAppContext)) {
            mLoginX = LoginX.getInstance(mAppContext);
            mLoginX.setOnLogin365Listener(this);
            mLoginX.autoLogin();
//            if (mLogin365 == null) {
//                mLogin365 = new Login365(mAppContext);
//            } else {
//                mLogin365.cancel();
//            }
//            mLogin365.setOnLogin365Listener(this);
//            mLogin365.autoLogin();
        } else {
            ToastUtil.toastError(ErrorCode.NET_DISCONNECT);
            finish();
        }
    }

    @Override
    public void onLoginFinish(List<String> gateways, List<String> cocos, int result, int serverLoginResult) {
        LogUtil.d(TAG, "onLoginFinish()-gateways:" + gateways + ",cocos:" + cocos + ",result:" + result + ",serverLoginResult:" + serverLoginResult);
        if (result == ErrorCode.SUCCESS || serverLoginResult == ErrorCode.SUCCESS) {
            //交给MainActivity处理登录结果
            //EventBus.getDefault().post(new MainEvent(new Login365Event(gateways, cocos, result, serverLoginResult)));
            if (isGoToMain) {
                Login365Event login365Event = new Login365Event(gateways, cocos, result, serverLoginResult);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(IntentKey.EVENT_LOGIN_RESULT, login365Event);
                startActivity(intent);
            }
        } else {
//            String tCon = String.format(mContext.getString(R.string.ERROR_LOAD_FAIL), result);
//            ToastUtil.showToast(tCon);
            ToastUtil.toastError(result);
        }
        dissmissDialogFragment();
        finish();
    }

    @Override
    public void onButtonClick(View view) {
        showDialogNow(this);
        loadData();
    }

    @Override
    public void onCancelClick(View view) {
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mLoginX != null) {
            mLoginX.removeListener(this);
            mLoginX.cancelLogin();
        }
//        if (mLogin365 != null) {
//            mLogin365.cancel();
//            mLogin365.removeListener(this);
//        }
        dissmissDialogFragment();
        super.onDestroy();
    }

    private void dissmissDialogFragment() {
        if (mDialogFragment != null) {
            try {
                mDialogFragment.dismissAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
