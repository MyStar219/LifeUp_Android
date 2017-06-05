package com.orvibo.homemate.device.allone2;

import android.content.Intent;
import android.os.Bundle;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.kookong.app.data.BrandList;
import com.kookong.app.data.RemoteList;
import com.kookong.app.data.SpList;
import com.kookong.app.data.api.IrData;
import com.kookong.app.data.api.IrDataList;
import com.smartgateway.app.R;
import com.orvibo.homemate.data.AlloneSaveData;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.allone2.add.DeviceBrandListActivity;
import com.orvibo.homemate.device.control.BaseControlActivity;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.util.AlloneUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * allone基础类
 * Created by snown on 2016/3/3.
 */
public class BaseAlloneControlActivity extends BaseControlActivity {
    private int areaId;

    protected BrandList.Brand deviceBrand;
    protected int deviceType;
    protected SpList.Sp sp;
    protected AlloneSaveData saveData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tv_remote_control);
        areaId = getIntent().getIntExtra("areaId", areaId);
        deviceBrand = (BrandList.Brand) getIntent().getSerializableExtra(DeviceBrandListActivity.BRAND_KEY);
        sp = (SpList.Sp) getIntent().getSerializableExtra(IntentKey.SPLIST_SP_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * 红外码数据下载
     *
     * @param deviceTypeId
     * @param sp
     * @param areaId
     * @param brand
     */
    public void loadIrData(final int deviceTypeId, final int brandId, final SpList.Sp sp, final int areaId, final BrandList.Brand brand) {
        showDialog();
        KookongSDK.getAllRemoteIds(deviceTypeId, brandId, sp == null ? 0 : sp.spId, areaId, new IRequestResult<RemoteList>() {

            @Override
            public void onSuccess(String msg, RemoteList result) {
                final List<Integer> remoteids = result.rids;
                if (remoteids != null && remoteids.size() > 0) {
                    KookongSDK.getIRDataById(AlloneDataUtil.getLoadIrData(remoteids, 0), new IRequestResult<IrDataList>() {

                        @Override
                        public void onSuccess(String msg, IrDataList result) {
                            dismissDialog();
                            saveData = new AlloneSaveData();
                            saveData.setAreaId(areaId);
                            saveData.setBrandId(brandId);
                            saveData.setSpId(sp == null ? 0 : sp.spId);
                            saveData.setSpType(sp == null ? 0 : sp.type);
                            List<IrData> irDatas = result.getIrDataList();
                            irDatasLoaded(irDatas, deviceTypeId, sp, brand, remoteids);
                        }

                        @Override
                        public void onFail(String msg) {
                            dismissDialog();
                            ToastUtil.showToast(R.string.allone_error_data_tip);
                        }
                    });
                }
            }

            @Override
            public void onFail(String msg) {
                dismissDialog();
            }
        });
    }


    /**
     * 红外码数据下载结果跳转
     *
     * @param irDatas
     * @param deviceType
     * @param sp
     * @param brand
     */
    protected void irDatasLoaded(List<IrData> irDatas, int deviceType, SpList.Sp sp, BrandList.Brand brand, List<Integer> remoteids) {
        Intent intent = new Intent(BaseAlloneControlActivity.this, RemoteControlActivity.class);
        intent.putExtra(IntentKey.DEVICE, device);
        deviceType = AlloneUtil.getLocalDeviceType(deviceType);
        intent.putExtra(IntentKey.ALL_ONE_DATA, (Serializable) irDatas);
        intent.putExtra(IntentKey.DEVICE_ADD_TYPE, deviceType);
        intent.putExtra(IntentKey.ALL_ONE_SAVE_DATA, saveData);
        intent.putIntegerArrayListExtra(IntentKey.ALL_ONE_REMOTE_IDS, (ArrayList<Integer>) remoteids);
        if (null != sp)
            intent.putExtra(IntentKey.SPLIST_SP_KEY, sp);
        if (brand != null)
            intent.putExtra(DeviceBrandListActivity.BRAND_KEY, brand);
        startActivity(intent);
    }

    public AlloneSaveData getSaveData() {
        return saveData;
    }
}
