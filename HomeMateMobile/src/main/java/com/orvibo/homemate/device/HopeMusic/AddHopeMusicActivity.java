package com.orvibo.homemate.device.HopeMusic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

/**
 * Created by allen on 2015/11/12.
 */
public class AddHopeMusicActivity extends BaseActivity {
//    private NavigationCocoBar navigationCocoBar;
    private Button nextButton;
    private ImageView blueGrayImageView;
    private TextView tipTextView1,tipTextView2;
    private NavigationCocoBar navigationCocoBar;
    private int productNameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zigbee_device);
        productNameId = getIntent().getIntExtra(Constant.CONFIG_TITLE, 0);
        findViews();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        blueGrayImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        tipTextView2 = (TextView) findViewById(R.id.tipTextView2);
        nextButton = (Button) findViewById(R.id.nextButton);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
    }

    private void init() {
//        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
//        navigationCocoBar.setCenterText(getString(R.string.add_ys_device_title));
        blueGrayImageView.setImageResource(R.drawable.pic_line_intelligent_sound);

        nextButton.setOnClickListener(this);
        nextButton.setText(R.string.qr_scanning_title);
        tipTextView1.setText(R.string.add_hope_music_tips);
        String add = getString(R.string.add);
        navigationCocoBar.setCenterText(add + getString(productNameId));
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nextButton:
                    Intent intent = new Intent(this, CaptureActivity.class);
                    startActivity(intent);
                finish();
                break;
            case R.id.tipTextView1:

        }
    }
}
