package com.orvibo.homemate.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.ThirdAccount;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.ThirdAccountDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.RegisterType;

import java.io.Serializable;
import java.util.List;

public class UserThirdActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout    mWeChat;
    private RelativeLayout    mQq;
    private RelativeLayout    mSina;
    private TextView weChatNickNameTextView, qqNickNameTextView, sinaNickNameTextView;
    private ThirdAccountDao thirdAccountDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_third);
        initView();
        initEvent();
    }

    private void initEvent() {
        mWeChat.setOnClickListener(this);
        mQq.setOnClickListener(this);
        mSina.setOnClickListener(this);

        thirdAccountDao = new ThirdAccountDao();
    }

    private void initView() {
        mWeChat = (RelativeLayout) findViewById(R.id.weChatRelativeLayout);
        mQq = (RelativeLayout) findViewById(R.id.qqRelativeLayout);
        mSina = (RelativeLayout) findViewById(R.id.sinaRelativeLayout);
        weChatNickNameTextView = (TextView) findViewById(R.id.weChatNickNameTextView);
        qqNickNameTextView = (TextView) findViewById(R.id.qqNickNameTextView);
        sinaNickNameTextView = (TextView) findViewById(R.id.sinaNickNameTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        weChatNickNameTextView.setText(R.string.user_phone_no);
        mWeChat.setTag(null);
        qqNickNameTextView.setText(R.string.user_phone_no);
        mQq.setTag(null);
        sinaNickNameTextView.setText(R.string.user_phone_no);
        mSina.setTag(null);
        List<ThirdAccount> thirdAccounts = thirdAccountDao.selThirdAccountByUserId(userId);
        for (ThirdAccount thirdAccount : thirdAccounts) {
            if (thirdAccount.getRegisterType() == RegisterType.WEIXIN_USER) {
                weChatNickNameTextView.setText(thirdAccount.getThirdUserName());
                mWeChat.setTag(thirdAccount);
            } else if (thirdAccount.getRegisterType() == RegisterType.QQ_USER) {
                qqNickNameTextView.setText(thirdAccount.getThirdUserName());
                mQq.setTag(thirdAccount);
            } else if (thirdAccount.getRegisterType() == RegisterType.SINA_USER) {
                sinaNickNameTextView.setText(thirdAccount.getThirdUserName());
                mSina.setTag(thirdAccount);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int registerType = RegisterType.WEIXIN_USER;
        switch (v.getId()) {
            case R.id.weChatRelativeLayout:
                registerType = RegisterType.WEIXIN_USER;
                break;
            case R.id.qqRelativeLayout:
                registerType = RegisterType.QQ_USER;
                break;
            case R.id.sinaRelativeLayout:
                registerType = RegisterType.SINA_USER;
                break;
        }
        Object tag = v.getTag();
        Intent intent = new Intent(mContext, UserThirdInfoActivity.class);
        intent.putExtra(Constant.REGISTER_TYPE, registerType);
        intent.putExtra(Constant.THIRD_ACCOUNT, (Serializable) tag);
        startActivity(intent);
    }

}

