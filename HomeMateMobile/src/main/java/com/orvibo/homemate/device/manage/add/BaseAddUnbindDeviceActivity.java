package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;

/**
 * updated by huangqiyao on 2016/7/9.
 * 支持添加主机。
 */
public class BaseAddUnbindDeviceActivity extends BaseActivity {
    private Button nextButton;
    private ImageView productImageView;
    private TextView productNameTextView;
    private TextView companyNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_unbind_device);
        findViews();
        init();
    }

    private void findViews() {
        nextButton = (Button) findViewById(R.id.nextButton);
        productImageView = (ImageView) findViewById(R.id.productImageView);
        productNameTextView = (TextView) findViewById(R.id.productNameTextView);
        companyNameTextView = (TextView) findViewById(R.id.companyNameTextView);
    }

    private void init() {
        nextButton.setOnClickListener(this);
    }

    /**
     * @param productName 产品名称
     * @param companyName 产品公司名
     */
    protected void setDeviceInfo(String productName, String companyName) {
        productNameTextView.setText(productName);
        companyNameTextView.setText(companyName);
    }

    protected void setDeviceImg(String imgUrl) {
        ImageLoader.getInstance().displayImage(imgUrl, productImageView, ViHomeApplication.getImageOptions());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int status = UserCache.getLoginStatus(mAppContext, UserCache.getCurrentUserName(mAppContext));
        if (status == LoginStatus.SUCCESS) {
            showDialog(null, getString(R.string.ap_config_add_device));
            onStartBind();
        } else {
            showLoginDialog();
        }
    }

    /**
     * 开始绑定设备
     */
    protected void onStartBind() {

    }

    protected void processAddDeviceResult(int result) {
        if (result == ErrorCode.SUCCESS) {
            Intent intent = new Intent(mAppContext, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (result == ErrorCode.DEVICE_HAS_BIND || result == ErrorCode.USER_NOT_BINDED) {
            //wifi设备或者主机已经被其他账号绑定，弹框提示
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setTitle(getString(R.string.add_device_fail_title));
            dialogFragmentOneButton.setContent(getString(R.string.DEVICE_HAS_BIND));
            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
            dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                @Override
                public void onButtonClick(View view) {
                    //回首页
                    finish();
                }
            });
            dialogFragmentOneButton.show(getFragmentManager(), "");
        } else {
            if (ErrorCode.isCommonError(result)) {
                //需要详细提示的信息
                ToastUtil.toastError(result);
            } else {
                //统一提示添加失败
                ToastUtil.showToast(getString(R.string.add_device_fail_title));
            }
        }
    }
}
