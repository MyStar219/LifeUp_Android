package com.orvibo.homemate.device.allone2.epg;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.InfoPushMsg;
import com.orvibo.homemate.bo.InfoPushProgram;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.view.custom.DialogFragmentTwoButton;

/**
 * Created by Allen on 2016/7/25.
 */
public class ProgramInfoPushDialogActivity extends BaseActivity implements DialogInterface.OnCancelListener {
    private DialogFragmentTwoButton dialogFragmentTwoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        dealInfoPush(infoPushMsg);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (dialogFragmentTwoButton != null) {
            dialogFragmentTwoButton.dismiss();
        }
        InfoPushMsg infoPushMsg = (InfoPushMsg) intent.getSerializableExtra("infoPushMsg");
        dealInfoPush(infoPushMsg);
    }

    private void dealInfoPush(InfoPushMsg infoPushMsg) {
        if (infoPushMsg == null) {
            return;
        }
        dialogFragmentTwoButton = new DialogFragmentTwoButton();
        dialogFragmentTwoButton.setTitle(infoPushMsg.getText());
        dialogFragmentTwoButton.setOnCancelListener(this);
        dialogFragmentTwoButton.setLeftButtonText(getString(R.string.goto_program_subscribe));
        dialogFragmentTwoButton.setLeftTextColor(getResources().getColor(R.color.green));
        dialogFragmentTwoButton.setRightButtonText(getString(R.string.subscribe_success_shutdown));
        dialogFragmentTwoButton.setRightTextColor(getResources().getColor(R.color.black));
        InfoPushProgram infoPushProgram = infoPushMsg.getInfoPushProgram();
        final Device device = new DeviceDao().selDevice(infoPushProgram.getUid(), infoPushProgram.getDeviceId());
        dialogFragmentTwoButton.setOnTwoButtonClickListener(new DialogFragmentTwoButton.OnTwoButtonClickListener() {
            @Override
            public void onLeftButtonClick(View view) {
                Intent intent = new Intent(mContext, ProgramSubscribeActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                mContext.startActivity(intent);
                finish();
            }

            @Override
            public void onRightButtonClick(View view) {
                finish();
            }
        });
        dialogFragmentTwoButton.show(getFragmentManager(), "");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
