package com.orvibo.homemate.user.family;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.api.listener.OnLogin365Listener;
import com.orvibo.homemate.bo.InfoPushMsg;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.data.BottomTabType;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.InviteType;
import com.orvibo.homemate.model.FamilyMemberResponse;
import com.orvibo.homemate.model.login.LoginX;
import com.orvibo.homemate.model.main.MainEvent;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Allen on 2015/9/29.
 * 家庭成员邀请页面
 */
public class FamilyInviteDialogActivity extends BaseActivity implements OnLogin365Listener, DialogInterface.OnCancelListener {
    private static final String TAG = FamilyInviteDialogActivity.class.getName();
    private DialogFragmentTwoButton dialogFragmentTwoButton;
    private FamilyMemberResponse familyMemberResponse;
    int inviteType = InviteType.REFUS;
    private LoginX mLoginX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        dealPushInviteFamily(infoPushMsg);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (dialogFragmentTwoButton != null) {
            dialogFragmentTwoButton.dismiss();
        }
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        dealPushInviteFamily(infoPushMsg);
    }

    private void dealPushInviteFamily(final InfoPushMsg infoPushMsg) {
        if (infoPushMsg == null) {
            finish();
            return;
        }
        LogUtil.d(TAG, "dealPushInviteFamily:" + infoPushMsg);
        if (familyMemberResponse == null) {
            familyMemberResponse = new FamilyMemberResponse(mAppContext) {
                @Override
                public void onFamilyMemberResponseResult(int serial, int result) {
                    if (result == ErrorCode.SUCCESS && inviteType == InviteType.ACCEPT) {
//                    recreate();
                        mLoginX = LoginX.getInstance(mAppContext);
                        mLoginX.setOnLogin365Listener(FamilyInviteDialogActivity.this);
                        mLoginX.autoLogin();
                    } else {
                        dismissDialog();
                        if (result != ErrorCode.SUCCESS) {
                            ToastUtil.toastError(result);
                        }
                        finish();
                    }
                }
            };
        }
        dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(infoPushMsg.getText());
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.family_invite_refus));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.family_invite_accept));
        dialogFragmentTwoButton.setOnCancelListener(this);
        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {
                inviteType = InviteType.REFUS;
                familyMemberResponse.startResponse(infoPushMsg.getPushInviteFamilyInfo().getInviteId(), InviteType.REFUS);
            }

            @Override
            public void onRightButtonClick(View view) {
                inviteType = InviteType.ACCEPT;
                familyMemberResponse.startResponse(infoPushMsg.getPushInviteFamilyInfo().getInviteId(), InviteType.ACCEPT);
                showDialogNow();
            }
        });
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onLoginFinish(List<String> gateways, List<String> cocos, int result, int serverLoginResult) {
        dismissDialog();
        if (result == ErrorCode.USER_PASSWORD_ERROR || serverLoginResult == ErrorCode.USER_PASSWORD_ERROR) {
            EventBus.getDefault().post(new MainEvent(true));
        } else {
            int bottomType = (gateways != null && !gateways.isEmpty()) ? BottomTabType.FOUR_BOTTOM_TAB : BottomTabType.TWO_BOTTOM_TAB;
            EventBus.getDefault().post(new MainEvent(bottomType, true));
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mLoginX != null) {
            mLoginX.removeListener(this);
            mLoginX.cancelLogin();
        }
        super.onDestroy();
    }
}
