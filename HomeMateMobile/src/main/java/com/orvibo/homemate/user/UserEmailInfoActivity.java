package com.orvibo.homemate.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.GetEmailType;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;


/**
 * Created by Allen on 2015/6/5.
 */
public class UserEmailInfoActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener{
    private static final String TAG = "UserEmailInfoActivity";
    private NavigationCocoBar navigationBar;
    private ImageView bindIconImageView;
    private TextView bindInfoTextView;
    private Button bindEmailChangeButton;
    private Account account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bind_info);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setCenterText(getString(R.string.user_email));
        navigationBar.setOnLeftClickListener(this);
        bindIconImageView = (ImageView) findViewById(R.id.bindIconImageView);
        bindIconImageView.setImageResource(R.drawable.user_email_icon);
        bindInfoTextView = (TextView) findViewById(R.id.bindInfoTextView);
        bindEmailChangeButton = (Button) findViewById(R.id.bindChangeButton);
        bindEmailChangeButton.setOnClickListener(this);
        bindEmailChangeButton.setText(R.string.user_email_change);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        account = new AccountDao().selMainAccountdByUserName(UserCache.getCurrentUserName(mAppContext));
        LogUtil.d(TAG,"refresh()-account:" +account);
        if (account != null) {
            String userPhone = String.format(getString(R.string.user_email_has_bind), StringUtil.hideEmailMiddleWord(account.getEmail()));
            bindInfoTextView.setText(userPhone);
        }
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bindChangeButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ChangeBindedEmail_Change), null);
                Intent intent = new Intent(this, UserEmailIdentifyActivity.class);
                intent.putExtra(Constant.USER_CONTACT, account.getEmail());
                intent.putExtra(Constant.GET_EMAIL_TYPE, GetEmailType.BEFORE_CHANGE_EMAIL);
                intent.putExtra(UserEmailIdentifyActivity.IS_AUTO_COUNTDOWN, false);
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
            Intent intent = new Intent(this, UserEmailBindActivity.class);
            startActivity(intent);
        }
//        finish();
    }
}
