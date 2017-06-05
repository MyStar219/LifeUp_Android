package com.orvibo.homemate.view.custom;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.add.RemoteNameAddActivity;

/**
 * Created by yuwei on 2016/4/1.
 * 自定义allone2界面的对话框
 */
public class AlloneNotFitTipsDialog extends Dialog implements View.OnClickListener {

    private Activity mContext;


    private TextView title;
    private TextView content;
    private TextView cancel;


    private Device device;
    private int deviceType;

    public AlloneNotFitTipsDialog(Activity context, Device device, int deviceType) {
        super(context);
        mContext = context;
        this.device = device;
        this.deviceType = deviceType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);//设置dialog圆角效果
        setContentView(R.layout.dialog_allone_not_fit_tips);

        initView();
        initListener();
    }

    private void initView() {
        this.cancel = (TextView) findViewById(R.id.cancel);
        this.content = (TextView) findViewById(R.id.content);
        this.title = (TextView) findViewById(R.id.title);
        if (isToCopyRemote()) {
            title.setText(mContext.getString(R.string.dialog_allone_title_copy));
            content.setText(mContext.getString(R.string.dialog_allone_content_copy));
        } else {
            title.setText(mContext.getString(R.string.dialog_allone_title_custom));
            content.setText(mContext.getString(R.string.dialog_allone_content_custom));
        }
    }

    private void initListener() {
        cancel.setOnClickListener(this);
        content.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.content:
                jumpActivity();
                break;
        }
        dismiss();
    }

    /**
     * 根据设备类型，跳转到复制遥控器还是自定义遥控器
     */
    private void jumpActivity() {
        Intent intent = new Intent(mContext, RemoteNameAddActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        if (isToCopyRemote()) {
            intent.putExtra(IntentKey.DEVICE_ADD_TYPE, deviceType);
        } else {
            intent.putExtra(IntentKey.DEVICE_ADD_TYPE, DeviceType.SELF_DEFINE_IR);
        }
        mContext.startActivity(intent);
        mContext.finish();
    }

    /**
     * 是否是到复制遥控器界面
     *
     * @return
     */
    private boolean isToCopyRemote() {
        return deviceType == DeviceType.TV || deviceType == DeviceType.STB;
    }
}
