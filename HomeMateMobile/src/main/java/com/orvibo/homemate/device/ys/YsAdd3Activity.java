package com.orvibo.homemate.device.ys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

/**
 * Created by allen on 2015/11/12.
 */
public class YsAdd3Activity extends BaseActivity {
    private NavigationCocoBar navigationCocoBar;
    private ImageView blueGrayImageView;
    private TextView tipTextView1, tipTextView2;
    private String model;
    private Button nextButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zigbee_device);
        findViews();
        init();
    }

    private void findViews() {
        blueGrayImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        tipTextView2 = (TextView) findViewById(R.id.tipTextView2);
        nextButton = (Button) findViewById(R.id.nextButton);
    }

    private void init() {
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        navigationCocoBar.setCenterText(getString(R.string.add_ys_device_title));
        model = getIntent().getStringExtra(IntentKey.YS_DEVICE_MODEL);
        if (model.contains("C2C")) {
            blueGrayImageView.setImageResource(R.drawable.bg_yingshi_c2c);
        } else if (model.contains("CO6")){
            blueGrayImageView.setImageResource(R.drawable.bg_yingshi_c6);
        } else {
            blueGrayImageView.setVisibility(View.INVISIBLE);
        }
        tipTextView1.setText(R.string.add_ys_device_tips2);
        tipTextView2.setText(R.string.add_ys_device_tips3);
        tipTextView2.setOnClickListener(this);
        tipTextView2.setVisibility(View.VISIBLE);
        nextButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.tipTextView2:
                showTipDialog();
                break;
            case R.id.nextButton:
                Intent intent = getIntent();
                intent.setClass(this, YsAdd4Activity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    public void showTipDialog() {
        final Dialog dialog = new AlertDialog.Builder(this).create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = -100;
        dialog.show();
        window.setContentView(R.layout.add_human_body_infrared_dialog);
        ImageView tipImageView = (ImageView) window.findViewById(R.id.tipImageView);
        if (model.contains("C2C")) {
            tipImageView.setImageResource(R.drawable.bg_yingshi_c2c_2);
        } else if (model.contains("CO6")){
            tipImageView.setImageResource(R.drawable.bg_yingshi_c6_2);
        } else {
            tipImageView.setVisibility(View.INVISIBLE);
        }
        TextView tipTextView1 = (TextView) window.findViewById(R.id.tipTextView1);
        tipTextView1.setText(getString(R.string.add_ys_device_tips4));
        window.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}
