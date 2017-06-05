package com.orvibo.homemate.device.searchdevice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.orvibo.homemate.bo.DeviceQueryUnbind;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.manage.add.AddUnbindDeviceActivity;
import com.orvibo.homemate.model.QueryUnbinds;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;
import com.smartgateway.app.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by baoqi on 2016/8/18.
 */
public class SearchWifiDevice {
    private final static String TAG = SearchWifiDevice.class.getSimpleName();
    private Context mAppContext;
    private QueryUnbinds queryUnbinds;
    private LinkedHashMap<String, DeviceQueryUnbind> mDeviceQueryUnbindsMap = new LinkedHashMap<String, DeviceQueryUnbind>();
    private ArrayList<DeviceQueryUnbind> mDeviceQueryUnbinds = new ArrayList<DeviceQueryUnbind>();
    private TextView unbindDeviceTextView;

    public SearchWifiDevice(Context context, TextView textView) {
        mAppContext = context;
        unbindDeviceTextView = textView;
        initQueryUnbinds();
    }

    private void initQueryUnbinds() {
        queryUnbinds = new QueryUnbinds(mAppContext) {
            @Override
            public void onQueryResult(final ArrayList<DeviceQueryUnbind> deviceQueryUnbinds, int serial, int result) {
                super.onQueryResult(deviceQueryUnbinds, serial, result);
                LogUtil.d(TAG, "deviceQueryUnbinds=" + deviceQueryUnbinds);
                if (result == ErrorCode.SUCCESS && deviceQueryUnbinds != null && deviceQueryUnbinds.size() != 0) {
                    //  mDeviceQueryUnbinds.addAll(0, deviceQueryUnbinds);
                    for (DeviceQueryUnbind deviceQueryUnbind : deviceQueryUnbinds) {
                        mDeviceQueryUnbindsMap.put(deviceQueryUnbind.getUid(), deviceQueryUnbind);
                    }
                    //  toBind(mDeviceQueryUnbinds);
                }
                //这里屏蔽了大拿小欧摄像头的的搜索(如果需要搜索小欧摄像头把)
                //  mSearchDanaleDevice.toLoginDanaleplatform();
                //********(如果需要搜索小欧摄像头把上面的注释的一行代码打开，下面的两行注释)
                List<DeviceQueryUnbind> deviceQueryUnbinds1 = mapToList(mDeviceQueryUnbindsMap);
                toBind(deviceQueryUnbinds1);
                //********
            }
        };
    }

    @SuppressLint("StringFormatMatches")
    public void toBind(final List<DeviceQueryUnbind> deviceQueryUnbinds) {

        //筛选wifi设备
        final ArrayList<DeviceQueryUnbind> wifiDeviceQueryUnbinds = new ArrayList<>();
        if (deviceQueryUnbinds != null && !deviceQueryUnbinds.isEmpty()) {
            for (DeviceQueryUnbind deviceQueryUnbind : deviceQueryUnbinds) {
                if (ProductManage.getInstance().isWifiDevice(deviceQueryUnbind)) {
                    wifiDeviceQueryUnbinds.add(deviceQueryUnbind);
                } else {
                    continue;
                }
            }
        }
        if (!wifiDeviceQueryUnbinds.isEmpty()) {
            unbindDeviceTextView.setVisibility(View.VISIBLE);
            String tips = String.format(mAppContext.getString(R.string.unbind_devices_found_tips), wifiDeviceQueryUnbinds.size());
            if (wifiDeviceQueryUnbinds.size() == 1) {
                String wifiDeviceName = DeviceTool.getWifiDeviceName(wifiDeviceQueryUnbinds.get(0));
                tips = String.format(mAppContext.getString(R.string.unbind_device_found_tips), wifiDeviceName);
            }
            SpannableStringBuilder builder = new SpannableStringBuilder(tips);
            ForegroundColorSpan greenSpan = new ForegroundColorSpan(mAppContext.getResources().getColor(R.color.green));
            if (PhoneUtil.isCN(mAppContext)) {
                builder.setSpan(greenSpan, tips.length() - 4, tips.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.setSpan(greenSpan, tips.length() - 3, tips.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            unbindDeviceTextView.setText(builder);
            unbindDeviceTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mAppContext, AddUnbindDeviceActivity.class);
                    intent.putExtra(IntentKey.DEVICE, wifiDeviceQueryUnbinds);
                    mAppContext.startActivity(intent);
                }
            });
        } else {
            return;
        }
    }

    public void searchWifiDevice(boolean needRequestKey) {
        clear();
        queryUnbinds.queryAllWifiDevices(mAppContext, needRequestKey);
    }

    public void clear() {
        //   mDeviceQueryUnbinds.clear();
        mDeviceQueryUnbindsMap.clear();
    }

    public List<DeviceQueryUnbind> getDeviceQueryUnbinds() {
        List<DeviceQueryUnbind> deviceQueryUnbinds = mapToList(mDeviceQueryUnbindsMap);
        return deviceQueryUnbinds;
    }

    /**
     * 把存储在map集合的数据转换成list存储
     *
     * @param deviceQueryUnbindMap
     * @return
     */
    private List<DeviceQueryUnbind> mapToList(Map<String, DeviceQueryUnbind> deviceQueryUnbindMap) {
        ArrayList<DeviceQueryUnbind> deviceQueryUnbinds = new ArrayList<DeviceQueryUnbind>();
        for (String uid : deviceQueryUnbindMap.keySet()) {
            deviceQueryUnbinds.add(deviceQueryUnbindMap.get(uid));
        }
        return deviceQueryUnbinds;
    }
}
