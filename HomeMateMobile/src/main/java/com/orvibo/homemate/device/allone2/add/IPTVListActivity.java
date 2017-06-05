package com.orvibo.homemate.device.allone2.add;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hzy.tvmao.KookongSDK;
import com.hzy.tvmao.interf.IRequestResult;
import com.hzy.tvmao.ir.Device;
import com.kookong.app.data.SpList;
import com.kookong.app.data.StbList;
import com.smartgateway.app.R;
import com.orvibo.homemate.device.allone2.ActivityFinishEvent;
import com.orvibo.homemate.device.allone2.BaseAlloneControlActivity;
import com.orvibo.homemate.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * IPTV品牌选择列表
 * Created by snown on 2016/2/22.
 */
public class IPTVListActivity extends BaseAlloneControlActivity {

    private ListView dataList;
    private SpList.Sp sp;
    private int areaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_iptv_list);
        this.dataList = (ListView) findViewById(R.id.stbList);
        sp = (SpList.Sp) getIntent().getSerializableExtra("spData");
        areaId = getIntent().getIntExtra("areaId", 0);
        getIPTV(sp.spId);
    }

    //获取指定AreaId下的运营商列表
    private void getIPTV(final int spId) {
        showDialog();
        KookongSDK.getIPTV(spId, new IRequestResult<StbList>() {
            @Override
            public void onSuccess(String s, StbList stbList) {
                dismissDialog();
                final List<StbList.Stb> stbs = stbList.stbList;
                List<String> names = new ArrayList<String>();
                for (StbList.Stb stb : stbs) {
                    names.add(stb.bname);
                }
                dataList.setAdapter(new StbDataAdapter(names));
                dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        StbList.Stb stb = stbs.get(position);
                        loadIrData(Device.STB, stb.bid, sp, areaId,null);
                    }
                });
            }

            @Override
            public void onFail(String s) {
                ToastUtil.showToast(R.string.allone_error_data_tip);
                dismissDialog();
            }
        });
    }

    /**
     * 添加遥控器成功后finish掉添加的activity
     * @param event
     */
    public void onEventMainThread(ActivityFinishEvent event) {
        finish();
    }
}
