package com.orvibo.homemate.device.manage.add;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.orvibo.homemate.bo.DeviceDesc;
import com.orvibo.homemate.bo.DeviceLanguage;
import com.orvibo.homemate.bo.GatewayBindInfo;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.core.product.WifiFlag;
import com.orvibo.homemate.dao.DeviceDescDao;
import com.orvibo.homemate.dao.DeviceLanguageDao;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.model.adddevice.vicenter.BindVicenter;
import com.orvibo.homemate.model.gateway.SearchNewHubResult;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;

import java.util.List;

/**
 * Created by huangqiyao on 2016/7/9 17:08.
 * 添加局域网内可以绑定的主机
 *
 * @version v1.9
 */
public class AddUnbindHubActivity extends BaseAddUnbindDeviceActivity implements BindVicenter.OnBindVicenterListener {
    private static final String TAG = "AddUnbindHubActivity";
    private SearchNewHubResult.HubResult mHubResult;
    private BindVicenter mBindVicenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mHubResult = (SearchNewHubResult.HubResult) intent.getSerializableExtra(IntentKey.HUB_RESULT);
        } else {
            finish();
        }
        if (mHubResult == null || TextUtils.isEmpty(mHubResult.uid)) {
            finish();
        }
        mBindVicenter = new BindVicenter(mAppContext);
        mBindVicenter.setOnBindListener(this);

        LogUtil.d(TAG, "onCreate()-mHubResult:" + mHubResult);
        DeviceDesc deviceDesc = new DeviceDescDao().selDeviceDesc(mHubResult.model);
        DeviceLanguage deviceLanguage = null;
        if (deviceDesc == null) {
            //获取不到对应设备信息，使用中性信息
            int wifiFlag = ProductManage.getInstance().getWifiFlagByModel(mHubResult.model);
            if (wifiFlag == WifiFlag.HUB) {
                mHubResult.model = ModelID.HUB_COMMON;
            } else {
                mHubResult.model = ModelID.MINI_HUB;
            }
        }

        if (deviceDesc != null) {
            setDeviceImg(deviceDesc.getPicUrl());
            DeviceLanguageDao deviceLanguageDao = new DeviceLanguageDao();
            String language = PhoneUtil.getPhoneLanguage(mAppContext);
            deviceLanguage = deviceLanguageDao.selDeviceLanguage(deviceDesc.getDeviceDescId(), language);
            if (deviceLanguage == null && language.contains("-")) {
                deviceLanguage = deviceLanguageDao.selDeviceLanguage(deviceDesc.getDeviceDescId(), language.substring(0, language.indexOf("-") - 1));
            }
            if (deviceLanguage == null) {
                deviceLanguage = deviceLanguageDao.selDeviceLanguage(deviceDesc.getDeviceDescId(), "zh");
            }
        }

        if (deviceLanguage != null) {
            setDeviceInfo(deviceLanguage.getProductName(), deviceLanguage.getManufacturer());
        } else {
            setDeviceInfo("", "");
        }
    }

    @Override
    protected void onStartBind() {
        super.onStartBind();
        LogUtil.d(TAG, "onStartBind()-uid:" + mHubResult.uid);
        if (mBindVicenter != null) {
            mBindVicenter.bind(mHubResult.uid);
        } else {
            LogUtil.e(TAG, "onStartBind()-mBindVicenter is null");
        }
    }

    @Override
    public void onBindVicenterResult(List<GatewayBindInfo> gatewayBindInfos, List<String> uids, final int result, int sec) {
        LogUtil.d(TAG, "onBindVicenterResult()-gatewayBindInfos:" + gatewayBindInfos);
        LogUtil.d(TAG, "onBindVicenterResult()-uids:" + uids + ",result:" + result);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
                processAddDeviceResult(result);
            }
        });
    }

    @Override
    public void onCountdown(int sec) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBindVicenter != null) {
            mBindVicenter.cancel();
        }
    }
}
