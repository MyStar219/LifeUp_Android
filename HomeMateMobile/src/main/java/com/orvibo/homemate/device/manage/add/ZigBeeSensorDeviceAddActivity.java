package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

/**
 * 新添5种传感器的第一个添加界面
 * zigbee设备添加界面
 * Created by wenchao on 2016/2/23.
 */
public class ZigBeeSensorDeviceAddActivity extends BaseActivity {
    private static final String TAG = ZigBeeSensorDeviceAddActivity.class.getName();
    private NavigationCocoBar navigationCocoBar;
    private Button nextButton;
    private ImageView blueGrayImageView;
    private TextView tipTextView1;
    private int productNameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zigbee_device);
        productNameId = getIntent().getIntExtra(Constant.CONFIG_TITLE, 0);
        init();
    }

    private void init() {
        blueGrayImageView = (ImageView) findViewById(R.id.blueGrayImageView);
        blueGrayImageView.setImageResource(getResourceIdByNameId(productNameId));
        tipTextView1 = (TextView) findViewById(R.id.tipTextView1);
        tipTextView1.setText(getString(getStringByNameId(productNameId)));
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationBar);
        String add = getString(R.string.add);
        navigationCocoBar.setCenterText(add + getString(productNameId));
//        navigationCocoBar.setOnLeftClickListener(new NavigationCocoBar.OnLeftClickListener() {
//            @Override
//            public void onLeftClick(View v) {
////                exit();
//                currentMainUid = UserCache.getCurrentMainUid(mContext);
//                if (!StringUtil.isEmpty(currentMainUid) && ViHomeApplication.getInstance().isManage()) {
//                    new Logout(mContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
//                }
//                ViHomeApplication.getInstance().setIsManage(false);
//                finish();
//            }
//        });
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setText(R.string.next);
        nextButton.setOnClickListener(this);
    }


    private int getResourceIdByNameId(int nameId) {
        int resId = R.drawable.bg_add_switch;
        switch (nameId) {
            case R.string.device_add_smoke_sensor:
                resId = R.drawable.pic_smoke_electrify;
                break;
            case R.string.device_add_co_sensor:
                resId = R.drawable.pic_co_electrify;
                break;
            case R.string.device_add_flooding_detector:
                resId = R.drawable.pic_water_electrify;
                break;
            case R.string.device_add_temperature_and_humidity_probe:
                resId = R.drawable.pic_temperature_electrify;
                break;
            case R.string.device_add_emergency_button:
                resId = R.drawable.pic_sos_electrify;
                break;
        }
        return resId;
    }

    private int getStringByNameId(int nameId) {
        int textId = R.string.device_add_switch_text;
        switch (nameId) {
            case R.string.device_add_smoke_sensor:
                textId = R.string.device_add_smoke_sensor_text;
                break;
            case R.string.device_add_co_sensor:
                textId = R.string.device_add_co_sensor_text;
                break;
            case R.string.device_add_flooding_detector:
                textId = R.string.device_add_flooding_detector_text;
                break;
            case R.string.device_add_temperature_and_humidity_probe:
                textId = R.string.device_add_temperature_and_humidity_probe_text;
                break;
            case R.string.device_add_emergency_button:
                textId = R.string.device_add_sos_sensor_text;
                break;
        }
        return textId;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(ZigBeeSensorDeviceAddActivity.this, ZigBeeDeviceAddActivity.class);
        intent.putExtra(Constant.CONFIG_TITLE, productNameId);
        startActivity(intent);
    }

//    private void exit() {
//        // vihome need to exit admin login.
//        currentMainUid = UserCache.getCurrentMainUid(mContext);
//        if (!StringUtil.isEmpty(currentMainUid) && ViHomeApplication.getInstance().isManage()) {
//            new Logout(mContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
//        }
//        ViHomeApplication.getInstance().setIsManage(false);
//        setResult(ResultCode.FINISH);
//        finish();
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        LogUtil.d(TAG, "onActivityResult()-resultCode:" + resultCode);
//        if (resultCode == ResultCode.FINISH) {
//            finish();
////        } else if (resultCode == CODE_EXIT_ADD_DEVICE) {
////            setResult(ResultCode.FINISH);
////            finish();
//        }
//    }

//    @Override
//    protected void onDestroy() {
//        exit();
//        super.onDestroy();
//    }

//    @Override
//    public void onLeftButtonClick(View view) {
//
//    }
//
//    @Override
//    public void onRightButtonClick(View view) {
//        Intent intent = new Intent(mContext, AddVicenterTipActivity.class);
//        startActivityForResult(intent, 0);
//    }

}
