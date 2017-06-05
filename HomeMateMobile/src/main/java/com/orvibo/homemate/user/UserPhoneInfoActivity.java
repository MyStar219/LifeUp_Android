package com.orvibo.homemate.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.GetSmsType;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;


/**
 * Created by Allen on 2015/5/28.
 */
public class UserPhoneInfoActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener{
    private NavigationCocoBar navigationBar;
    private Button bindPhoneChangeButton;
    private TextView bindInfoTextView;
    private Account account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bind_info);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        bindInfoTextView = (TextView) findViewById(R.id.bindInfoTextView);
        bindPhoneChangeButton = (Button) findViewById(R.id.bindChangeButton);
        bindPhoneChangeButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        account = new AccountDao().selMainAccountdByUserName(UserCache.getCurrentUserName(this));
        if (account != null) {
            String userPhone = String.format(getString(R.string.user_phone_has_bind), StringUtil.hidePhoneMiddleNumber(account.getPhone()));
            bindInfoTextView.setText(userPhone);
        }
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bindChangeButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedPhone_Change), null);
                Intent intent = new Intent(this, UserPhoneIdentifyActivity.class);
                intent.putExtra(Constant.USER_CONTACT, account.getPhone());
                intent.putExtra(Constant.GET_SMS_TYPE, GetSmsType.BEFORE_CHANGE_PHONE);
                intent.putExtra(UserPhoneIdentifyActivity.IS_AUTO_COUNTDOWN, false);
                startActivityForResult(intent, 0);
//                finish();
                break;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, UserPhoneBindActivity.class);
            startActivity(intent);
        }
//        finish();
    }
}
