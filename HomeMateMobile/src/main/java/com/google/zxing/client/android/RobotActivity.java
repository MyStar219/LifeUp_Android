package com.google.zxing.client.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.ap.ApWifiHelper;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by snown on 2016/6/30.
 *
 * @描述: 机器人添加界面
 */
public class RobotActivity extends BaseActivity {

    private NavigationCocoBar navigationBar;
    private EditTextWithCompound wifiName;
    private EditTextWithCompound wifiPass;

    private Button next;
    private EditTextWithCompound userName;
    private EditTextWithCompound userPass;
    private ImageView userPassEyeView;
    private ImageView wifiPassEyeView;

    private static final int PASS_MIN_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);

        initView();
        initData();

    }

    private void initView() {
        this.navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        this.wifiPassEyeView = (ImageView) findViewById(R.id.wifiPassEyeView);
        this.userPassEyeView = (ImageView) findViewById(R.id.userPassEyeView);
        this.userPass = (EditTextWithCompound) findViewById(R.id.userPass);
        this.userName = (EditTextWithCompound) findViewById(R.id.userName);
        this.wifiPass = (EditTextWithCompound) findViewById(R.id.wifiPass);
        this.wifiName = (EditTextWithCompound) findViewById(R.id.wifiName);
        userPass.setNeedRestrict(false);
        userName.setNeedRestrict(false);
        wifiPass.setNeedRestrict(false);
        wifiName.setNeedRestrict(false);
        userPass.requestFocus();
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(this);
        userPassEyeView.setOnClickListener(this);
        wifiPassEyeView.setOnClickListener(this);
        showPassword(true, userPass, userPassEyeView);
        showPassword(true, wifiPass, wifiPassEyeView);
    }

    private void initData() {
        navigationBar.setCenterText(getString(R.string.add) + getString(R.string.device_add_robot));
        ApWifiHelper apWifiHelper = new ApWifiHelper(getApplicationContext());
        if (!TextUtils.isEmpty(apWifiHelper.getSSID())) {
            wifiName.setText(apWifiHelper.getSSID());
        }
        Account account = new AccountDao().selCurrentAccount(UserCache.getCurrentUserId(mAppContext));
        if (account != null) {
            if (!TextUtils.isEmpty(account.getPhone()))
                userName.setText(account.getPhone());
            else if (!TextUtils.isEmpty(account.getEmail()))
                userName.setText(account.getEmail());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                String qrText = getQrText();
                if (!TextUtils.isEmpty(qrText)) {
                    Intent intent = new Intent(RobotActivity.this, RobotQrActivity.class);
                    intent.putExtra("qrText", qrText);
                    startActivity(intent);
                }
                break;
            case R.id.userPassEyeView:
                showPassword(userPass.getTransformationMethod() instanceof PasswordTransformationMethod, userPass, userPassEyeView);
                break;
            case R.id.wifiPassEyeView:
                showPassword(wifiPass.getTransformationMethod() instanceof PasswordTransformationMethod, wifiPass, wifiPassEyeView);
                break;
        }
    }

    /**
     * 获取二维码json数据
     *
     * @return
     */
    private String getQrText() {
        String userName = this.userName.getText().toString();
        String userPass = this.userPass.getText().toString();
        String wifiName = this.wifiName.getText().toString();
        String wifiPass = this.wifiPass.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            ToastUtil.showToast(getString(R.string.robot_input_user_name));
        } else if (TextUtils.isEmpty(userPass)) {
            ToastUtil.showToast(getString(R.string.robot_input_user_pass));
        } else if (TextUtils.isEmpty(wifiName)) {
            ToastUtil.showToast(getString(R.string.robot_input_wifi_name));
        } else if (userPass.length() < PASS_MIN_LENGTH) {
            ToastUtil.showToast(getString(R.string.robot_input_user_pass_tip));
        } else {
            return getJson(userName, userPass, wifiName, wifiPass);
        }
        return null;
    }


    /**
     * json字符生成
     *
     * @param userName
     * @param userPass
     * @param strWifiName
     * @param strWifiPass
     * @return
     */
    private String getJson(String userName, String userPass, String strWifiName, String strWifiPass) {
        JSONObject json = new JSONObject();
        try {
            json.put("company", "Orvibo");
            json.put("ssid", strWifiName);
            json.put("psw", strWifiPass);
            JSONObject json1 = new JSONObject();
            json1.put("username", userName);
            json1.put("password", userPass);
            json.put("data", json1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void showPassword(boolean show, EditTextWithCompound editTextWithCompound, ImageView imageView) {
        int selectionStart = editTextWithCompound.getSelectionStart();
        if (show) {
            editTextWithCompound.setTransformationMethod(null);
            imageView.setImageResource(R.drawable.password_hide);
        } else {
            editTextWithCompound.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.password_show);
        }
        if (selectionStart > 0) {
            try {
                editTextWithCompound.setSelection(selectionStart);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
