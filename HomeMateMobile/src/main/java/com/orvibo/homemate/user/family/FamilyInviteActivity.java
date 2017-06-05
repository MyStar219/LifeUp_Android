package com.orvibo.homemate.user.family;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.InviteFamilyMember;
import com.orvibo.homemate.model.InviteUser;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;
import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;


/**
 * Created by Allen on 2015/9/9.
 */
public class FamilyInviteActivity extends BaseActivity implements EditTextWithCompound.OnInputListener, DialogFragmentTwoButton.OnTwoButtonClickListener {
    private EditTextWithCompound accountEditText;
    private TextView addTextView;

    private InviteFamilyMember inviteFamilyMember;
    private InviteUser inviteUser;
    private String accountName;
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;
    private String mNewContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_invite);
        findViews();
        init();
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(mAppContext, ViHomeProApp.WX_APP_ID, false);
        api.registerApp(ViHomeProApp.WX_APP_ID);
    }

    @Override
    protected void onDestroy() {
        api.unregisterApp();
        super.onDestroy();
    }

    private void findViews() {
        accountEditText = (EditTextWithCompound) findViewById(R.id.accountEditText);
        accountEditText.setRightfulBackgroundDrawable(getResources().getDrawable(R.color.white));
        accountEditText.setType(EditTextWithCompound.TYPE_PHONE_EMAIL);
        accountEditText.setOnInputListener(this);
        accountEditText.setNeedRestrict(false);
        addTextView = (TextView) findViewById(R.id.addTextView);
        addTextView.setEnabled(false);
        addTextView.setTextColor(getResources().getColor(R.color.gray));

    }


    private void init() {
        addTextView.setOnClickListener(this);
        initInviteFamilyMember();
    }

    private void initInviteFamilyMember() {
        inviteFamilyMember = new InviteFamilyMember(mAppContext) {
            @Override
            public void onInviteFamilyMemberResult(int serial, int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.family_invite_success);
                    accountEditText.setText("");
//                    Account account = new Account();
//                    account.setPhone(accountName);
//                    Intent intent = new Intent(FamilyInviteActivity.this, FamilyActivity.class);
//                    intent.putExtra(Constant.ACCOUNT, account);
//                    startActivity(intent);
                    finish();
                } else if (result == ErrorCode.TIMEOUT) {
                    ToastUtil.showToast(getString(R.string.TIMEOUT));
                } else if (result == ErrorCode.FAIL) {
                    ToastUtil.showToast(R.string.family_invite_fail);
                } else if (result == ErrorCode.INVITE_NO_DEVICE) {
                    ToastUtil.showToast(R.string.family_invite_no_device);
                } else if (result == ErrorCode.INVITE_UNREGISTER) {
                    if (isOverseasVersion && StringUtil.isEmail(accountName)) {
                        DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                        dialogFragmentTwoButton.setTitle(getString(R.string.family_invite_unregister_title));
                        dialogFragmentTwoButton.setContent(getString(R.string.family_invite_unregister_content));
                        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                        dialogFragmentTwoButton.setRightButtonText(getString(R.string.family_invite_by_email));
                        dialogFragmentTwoButton.setOnTwoButtonClickListener(FamilyInviteActivity.this);
                        dialogFragmentTwoButton.show(getFragmentManager(), "");
                    } else {
                        if (api.isWXAppInstalled()) {
                            DialogFragmentTwoButton dialogFragmentTwoButton = new DialogFragmentTwoButton();
                            dialogFragmentTwoButton.setTitle(getString(R.string.family_invite_unregister_title));
                            dialogFragmentTwoButton.setContent(getString(R.string.family_invite_try_weixin));
                            dialogFragmentTwoButton.setLeftButtonText(getString(R.string.cancel));
                            dialogFragmentTwoButton.setRightButtonText(getString(R.string.family_invite_from_weixin));
                            dialogFragmentTwoButton.setOnTwoButtonClickListener(FamilyInviteActivity.this);
                            dialogFragmentTwoButton.show(getFragmentManager(), "");
                        } else {
                            DialogFragmentOneButton dialogFragmentOneButton = new DialogFragmentOneButton();
                            dialogFragmentOneButton.setButtonText(getString(R.string.confirm));
                            dialogFragmentOneButton.setTitle(getString(R.string.family_invite_unregister_title));
                            dialogFragmentOneButton.setContent(getString(R.string.family_invite_unregister_content));
                            dialogFragmentOneButton.show(getFragmentManager(), "");
                        }
                    }
                } else if (result == ErrorCode.INVITE_YOURSELF) {
                    ToastUtil.showToast(R.string.family_invite_yourself);
                } else if (result == ErrorCode.INVITE_EXIST) {
                    ToastUtil.showToast(R.string.family_invite_exist);
                } else if (result == ErrorCode.INVITE_FORBID) {
                    ToastUtil.showToast(R.string.family_invite_forbid);
                } else if (!ToastUtil.toastCommonError(result)) {
                    ToastUtil.showToast(R.string.family_invite_fail);
                }
            }
        };

        inviteUser = new InviteUser() {
            @Override
            public void onInviteUserResult(int result) {
                dismissDialog();
                if (result == ErrorCode.SUCCESS) {
                    ToastUtil.showToast(R.string.family_invite_success);
                    finish();
                } else {
                    ToastUtil.toastError(result);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.addTextView: {
                // 判断网络是否连接
                if (!NetUtil.isNetworkEnable(mAppContext)) {
                    ToastUtil.showToast(R.string.network_canot_work, Toast.LENGTH_SHORT);
                    return;
                }
                accountName = accountEditText.getText().toString();
                showDialog(null, getString(R.string.family_invite_sending));
                inviteFamilyMember.startInvite(accountName);
//                share2WX();
                break;
            }
        }
    }

    private void share2WX() {
        WXWebpageObject wxWebpageObject = new WXWebpageObject();
//        wxAppExtendObject.extInfo = "0";
        wxWebpageObject.webpageUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.orvibo.homemate";
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = wxWebpageObject;
        msg.title = getString(R.string.family_invite_weixin_title);
        msg.description = getString(R.string.family_invite_weixin_content);
        msg.setThumbImage(BitmapFactory.decodeResource(getResources(), R.drawable.share_logo));
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    private void inviteByEmail() {
        inviteUser.startInvite(accountName);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    @Override
    public void onRightful() {
        addTextView.setEnabled(true);
        addTextView.setTextColor(getResources().getColor(R.color.green));
    }

    @Override
    public void onUnlawful() {
        addTextView.setEnabled(false);
        addTextView.setTextColor(getResources().getColor(R.color.gray));
    }

    @Override
    public void onClearText() {

    }

    @Override
    public void onLeftButtonClick(View view) {
    }

    @Override
    public void onRightButtonClick(View view) {
        if (isOverseasVersion && StringUtil.isEmail(accountName)) {
            inviteByEmail();
        } else {
            share2WX();
        }
    }
}
