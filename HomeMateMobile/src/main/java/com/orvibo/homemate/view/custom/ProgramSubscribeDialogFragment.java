package com.orvibo.homemate.view.custom;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.epg.ProgramSubscribeActivity;
import com.orvibo.homemate.sharedPreferences.SubscribeTipsCache;

/**
 * Created by allen on 2016/7/21.
 */
public class ProgramSubscribeDialogFragment extends DialogFragment implements View.OnClickListener{
    private Context context;
    private Device device;
    private View rootView;
    private TextView hasALook, neverFuckMe, cancel;

    public void init(Context context, Device device) {
        this.context = context;
        this.device = device;
        rootView = View.inflate(context, R.layout.dialog_subcribe_success_tips, null);
        hasALook = (TextView) rootView.findViewById(R.id.hasALook);
        hasALook.setOnClickListener(this);
        neverFuckMe = (TextView) rootView.findViewById(R.id.neverFuckMe);
        neverFuckMe.setOnClickListener(this);
        cancel = (TextView) rootView.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.MyDialog);
        dialog.setContentView(rootView);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hasALook:
                Intent intent = new Intent(context, ProgramSubscribeActivity.class);
                intent.putExtra(IntentKey.DEVICE, device);
                context.startActivity(intent);
                break;
            case R.id.neverFuckMe:
                SubscribeTipsCache.saveSubcribeTips(context, true);
                break;
            case R.id.cancel:
                break;
        }
        dismiss();
    }

}
