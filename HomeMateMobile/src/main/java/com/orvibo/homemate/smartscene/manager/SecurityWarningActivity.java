package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.SecLeftCallCount;
import com.orvibo.homemate.bo.Security;
import com.orvibo.homemate.bo.SecurityWarning;
import com.orvibo.homemate.bo.WarningMember;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.SecLeftCallCountDao;
import com.orvibo.homemate.dao.SecurityWarningDao;
import com.orvibo.homemate.dao.WarningMemberDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.SecurityWarningType;
import com.orvibo.homemate.model.GetSecurityCallCount;
import com.orvibo.homemate.model.SetSecurityWarning;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.ToastUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Created by allen on 2016/6/24.
 */
public class SecurityWarningActivity extends BaseActivity {
    private View app, appAndPhone, contact;
    private TextView membersTextView, leftCountTitleTextView;
    private ImageView appHookImageView, appAndPhoneHookImageView;
    private TextView leftCountTextView;
    private Security security;
    private SecurityWarning securityWarning;
    private SecurityWarningDao securityWarningDao;
    private SecLeftCallCount secLeftCallCount;
    private SecLeftCallCountDao secLeftCallCountDao;
    private SetSecurityWarning setSecurityWarning;
    private GetSecurityCallCount getSecurityCallCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_warning);
        Intent intent = getIntent();
        Serializable serializable = intent.getSerializableExtra(IntentKey.SECURITY);
        if (serializable == null){
            finish();
            return;
        }
        security = (Security) serializable;
        init();
    }

    private void init() {
        app = findViewById(R.id.app);
        app.setOnClickListener(this);
        appAndPhone = findViewById(R.id.appAndPhone);
        appAndPhone.setOnClickListener(this);
        contact = findViewById(R.id.contact);
        contact.setOnClickListener(this);
        appHookImageView = (ImageView) findViewById(R.id.appHookImageView);
        appAndPhoneHookImageView = (ImageView) findViewById(R.id.appAndPhoneHookImageView);
        membersTextView = (TextView) findViewById(R.id.membersTextView);
        leftCountTitleTextView = (TextView) findViewById(R.id.leftCountTitleTextView);
        leftCountTextView = (TextView) findViewById(R.id.leftCountTextView);
        securityWarningDao = new SecurityWarningDao();
        securityWarning = securityWarningDao.selSecurityWarning(UserCache.getCurrentUserId(mAppContext), security.getSecurityId());
        if (securityWarning == null || securityWarning.getWarningType()== SecurityWarningType.APP) {
            checkWarning(true);
        } else {
            checkWarning(false);
        }
        secLeftCallCountDao = new SecLeftCallCountDao();
        resetLeftCount();
        initSetSecurityWarning();
        initGetSecurityCallCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSecurityCallCount.startGetSecurityCallCount();
    }

    private void checkWarning(boolean isApp) {
        if (isApp) {
            appHookImageView.setVisibility(View.VISIBLE);
            appAndPhoneHookImageView.setVisibility(View.GONE);
            membersTextView.setTextColor(getResources().getColor(R.color.gray));
            contact.setEnabled(false);
            leftCountTitleTextView.setTextColor(getResources().getColor(R.color.gray));
        } else {
            appHookImageView.setVisibility(View.GONE);
            appAndPhoneHookImageView.setVisibility(View.VISIBLE);
            membersTextView.setTextColor(getResources().getColor(R.color.black));
            contact.setEnabled(true);
            leftCountTitleTextView.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void resetLeftCount() {
        secLeftCallCount = secLeftCallCountDao.selSecLeftCallCount(UserCache.getCurrentUserId(mAppContext));
        if (secLeftCallCount != null) {
            leftCountTextView.setText(secLeftCallCount.getLeftCount() + getString(R.string.times));
        }
    }

    private void initSetSecurityWarning() {
        setSecurityWarning = new SetSecurityWarning(mAppContext){
            @Override
            public void onSetSecurityWarningResult(int serial, int result, String secWarningId) {
                super.onSetSecurityWarningResult(serial, result, secWarningId);
                if (result != ErrorCode.SUCCESS) {
                    ToastUtil.toastError(result);
                } else if(!TextUtils.isEmpty(secWarningId)) {
                    securityWarning = securityWarningDao.selSecurityWarning(secWarningId);
                    if (securityWarning != null && securityWarning.getWarningType() == SecurityWarningType.APP_AND_PHONE) {
                        List<WarningMember> warningMembers = new WarningMemberDao().selWarningMembers(secWarningId);
                        if (warningMembers.isEmpty()) {
                            toContactActivity();
                        }
                    }
                }
            }
        };
    }

    private void initGetSecurityCallCount() {
        getSecurityCallCount = new GetSecurityCallCount(mAppContext) {
            @Override
            public void onGetSecurityCallCountResult(int serial, int result, int leftCount) {
                super.onGetSecurityCallCountResult(serial, result, leftCount);
                if (result == ErrorCode.SUCCESS) {
                    leftCountTextView.setText(leftCount + getString(R.string.times));
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.app:
                setSecurityWarning.startSetSecurityWarning(userId, security.getSecurityId(), SecurityWarningType.APP, null, null, null);
                checkWarning(true);
                break;
            case R.id.appAndPhone:
                setSecurityWarning.startSetSecurityWarning(userId, security.getSecurityId(), SecurityWarningType.APP_AND_PHONE, null, null, null);
                checkWarning(false);
                break;
            case R.id.contact:
                toContactActivity();
                break;
        }
    }

    private void toContactActivity() {
        Intent intent = new Intent(this, SecurityWarningContactActivity.class);
        intent.putExtra(IntentKey.SECURITY, security);
        intent.putExtra(IntentKey.SECURITY_WARNING, securityWarning);
        startActivity(intent);
    }
}
