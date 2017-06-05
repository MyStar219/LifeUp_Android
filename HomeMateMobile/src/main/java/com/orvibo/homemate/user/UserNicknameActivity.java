package com.orvibo.homemate.user;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.ModifyNickname;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.orvibo.homemate.view.custom.NavigationCocoBar;
import com.tencent.stat.StatService;


/**
 * Created by Allen on 2015/5/28.
 */
public class UserNicknameActivity extends BaseActivity implements View.OnClickListener, NavigationCocoBar.OnLeftClickListener, EditTextWithCompound.OnInputListener {
    private NavigationCocoBar navigationBar;
    private EditTextWithCompound userNicknameEditText;
    private Button saveButton;
    private ModifyNickname modifyNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_nickname);
        init();
    }

    private void init() {
        navigationBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationBar.setOnLeftClickListener(this);
        userNicknameEditText = (EditTextWithCompound) findViewById(R.id.userNicknameEditText);
        String nickname = getIntent().getStringExtra(Constant.NICKNAME);
        userNicknameEditText.setNeedRestrict(false);
        userNicknameEditText.setText(nickname);
        userNicknameEditText.setMaxLength(EditTextWithCompound.MAX_TEXT_LENGTH);
        userNicknameEditText.setOnInputListener(this);
        if (!StringUtil.isEmpty(nickname)) {
            userNicknameEditText.setSelection(userNicknameEditText.length());
        }
        saveButton = (Button) findViewById(R.id.saveButton);
        if (userNicknameEditText.getText().length() == 0) {
            saveButton.setEnabled(false);
        }
        saveButton.setOnClickListener(this);
        modifyNickname = new ModifyNickname(mAppContext) {
            @Override
            public void onModifyNicknameResult(int result) {
                super.onModifyNicknameResult(result);
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    ToastUtil.toastError(result);
                }
//                else if (result == ErrorCode.FAIL){
//                    ToastUtil.showToast(UserNicknameActivity.this, getString(R.string.user_nickname_change_fail));
//                } else if(result == ErrorCode.TIMEOUT_SN){
//                    ToastUtil.showToast(UserNicknameActivity.this, getString(R.string.TIMEOUT));
//                }
            }
        };
    }

    @Override
    public void onLeftClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ModifyNickName_Back), null);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveButton: {
                StatService.trackCustomKVEvent(mAppContext, getString(R.string.MTAClick_ModifyNickName_Save), null);
                showDialog(null, getString(R.string.user_nickname_changing));
                modifyNickname.startModifyNickname( UserCache.getCurrentUserName(mAppContext), userNicknameEditText.getText().toString());
                break;
            }
        }
    }

    @Override
    public void onRightful() {
        saveButton.setEnabled(true);
    }

    @Override
    public void onUnlawful() {
        saveButton.setEnabled(false);
    }

    @Override
    public void onClearText() {

    }
}
