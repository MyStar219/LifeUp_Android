package com.orvibo.homemate.user.family;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.orvibo.homemate.bo.InfoPushMsg;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.core.load.loadserver.LoadServer;
import com.orvibo.homemate.core.load.loadserver.OnLoadServerListener;
import com.orvibo.homemate.view.custom.DialogFragmentOneButton;

import java.util.List;

/**
 * Created by Allen on 2015/9/29.
 */
public class FamilyInviteResponseDialogActivity extends BaseActivity implements DialogInterface.OnCancelListener,  OnLoadServerListener {
    private DialogFragmentOneButton dialogFragmentOneButton;
    private LoadServer mLoadServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        dealPushInviteFamilyResult(infoPushMsg);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (dialogFragmentOneButton != null) {
            dialogFragmentOneButton.dismiss();
        }
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        dealPushInviteFamilyResult(infoPushMsg);
    }

    private void dealPushInviteFamilyResult(InfoPushMsg infoPushMsg) {
        if (infoPushMsg == null) {
            return;
        }
        dialogFragmentOneButton = new DialogFragmentOneButton();
        dialogFragmentOneButton.setTitle(infoPushMsg.getText());
        dialogFragmentOneButton.setOnCancelListener(this);
        dialogFragmentOneButton.setOnButtonClickListener(new DialogFragmentOneButton.OnButtonClickListener() {
            @Override
            public void onButtonClick(View view) {
                showDialogNow();
                mLoadServer = LoadServer.getInstance(mAppContext);
                mLoadServer.setOnLoadServerListener(FamilyInviteResponseDialogActivity.this);
                mLoadServer.loadServer();
            }
        });
        dialogFragmentOneButton.show(getFragmentManager(), "");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onLoadServerFinish(List<String> tableNames, int result) {
        dismissDialog();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadServer.getInstance(mAppContext).removeListener(this);
    }
}
