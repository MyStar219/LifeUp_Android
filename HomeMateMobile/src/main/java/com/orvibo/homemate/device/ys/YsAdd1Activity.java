package com.orvibo.homemate.device.ys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.smartgateway.app.R;
import com.tencent.stat.StatService;

/**
 * Created by allen on 2015/11/12.
 * update by yuwei 添加小欧摄像头
 */
public class YsAdd1Activity extends BaseActivity {
    //    private NavigationCocoBar navigationCocoBar;
    private Button nextButton;
    private ImageView blueGrayImageView;
    private TextView tipTextView1,tipTextView2;
    private NavigationCocoBar navigationCocoBar;
    private int productNameId;
    private String danaleAddDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zigbee_device);
        productNameId = getIntent().getIntExtra(Constant.CONFIG_TITLE, 0);
        findViews();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        blueGrayImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        tipTextView2 = (TextView) findViewById(R.id.tipTextView2);
        nextButton = (Button) findViewById(R.id.nextButton);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
    }

    private void init() {
//        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
//        navigationCocoBar.setCenterText(getString(R.string.add_ys_device_title));
        blueGrayImageView.setImageResource(R.drawable.bg_yingshi_picture);

        nextButton.setOnClickListener(this);
        if (productNameId == R.string.xiao_ou_camera){
            tipTextView1.setText(R.string.xiao_ou_camera_add_tips);
            tipTextView2.setVisibility(View.VISIBLE);
            tipTextView2.setText(Html.fromHtml("<u>"+getString(R.string.do_not_hear_voice_prompt)+"</u>"));
            tipTextView2.setOnClickListener(this);
            nextButton.setText(R.string.i_had_hear_voice_prompt);
            blueGrayImageView.setImageResource(R.drawable.bg_xiaoou);
        }else {
            tipTextView1.setText(R.string.add_ys_device_tips);
            nextButton.setText(R.string.add_ys_device_scan);
        }
        String add = getString(R.string.add);
        navigationCocoBar.setCenterText(add + getString(productNameId));
    }

    private String phone;
    private String email;
    private boolean check () {
        if (!isWifiConnect()){
            return false;
        }

        int logoutStatus = UserCache.getLoginStatus(this, userName);
        if (logoutStatus != LoginStatus.SUCCESS && logoutStatus != LoginStatus.FAIL) {
            showLoginDialog();
            return false;
        }
        Account account = new AccountDao().selMainAccountdByUserName(userName);
        phone = account==null?"":account.getPhone();
        email = account==null?"":account.getEmail();
        if (TextUtils.isEmpty(phone) && TextUtils.isEmpty(email)){
            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
            dialogFragmentOneButton.setTitle(getString(R.string.warm_tips));
            dialogFragmentOneButton.setContent(getString(R.string.need_bind_phone_or_email));
            dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
                @Override
                public void onButtonClick(View view) {}
            });
            dialogFragmentOneButton.show(getFragmentManager(),"");
            return false;
        }
        return true;
    }

    private boolean isWifiConnect(){
        if (!NetUtil.isWifi(mAppContext)){
            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
            dialogFragmentTwoButton.setTitle(getString(R.string.please_connect_wifi));
            dialogFragmentTwoButton.setRightButtonText(getString(R.string.ap_config_reconnect_go));
            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
            dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
                @Override
                public void onLeftButtonClick(View view) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_BeingAdded_PopViewCancel), null);
                }

                @Override
                public void onRightButtonClick(View view) {
                    StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_AddCoCo_ToConnect), null);
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                }
            });
            dialogFragmentTwoButton.show(getFragmentManager(), "");
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton:
                if (productNameId == R.string.xiao_ou_camera){
                    if (check())
                        finish();
                }else {
                    Intent intent = new Intent(this, CaptureActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.tipTextView1:

                break;
            case R.id.tipTextView2:
                showTipDialog();
                break;

        }
    }

    private void showTipDialog(){
        final Dialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = -100;
        dialog.show();
        window.setContentView(R.layout.add_sensor_dialog);
        ImageView tipImageView = (ImageView) window.findViewById(R.id.imageView);
        tipImageView.setImageResource(R.drawable.bg_xiaoou_back);
        TextView tipTextView1 = (TextView) window.findViewById(R.id.tipTextView1);
        tipTextView1.setText(getString(R.string.not_hear_sound_tip));
        window.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
