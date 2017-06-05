package com.orvibo.homemate.view.popup;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.data.ModelID;
import com.orvibo.homemate.device.allone2.SelectAlloneTVAdapter;
import com.orvibo.homemate.util.AlloneUtil;
import com.orvibo.homemate.util.PopupWindowUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * allone机顶盒界面，绑定电视。
 *
 * @author Smagret
 * @date 2016/4/6
 */
public abstract class SelectAlloneTVPopup implements AdapterView.OnItemClickListener {
    private PopupWindow popup;
    private DeviceDao mDeviceDao;
    private SelectAlloneTVAdapter selectAlloneTVAdapter;
    private List<Device> devices;
    private Device selectedDevice;

    public SelectAlloneTVPopup() {
        mDeviceDao = new DeviceDao();
    }

    /**
     * @param context
     * @param uid
     * @param deviceType 查询的绑定的设备的type
     *  @param mainDeviceType 需要绑定的设备类型
     */
    public void showPopup(Activity context,String uid, int deviceType,int mainDeviceType) {
        dismiss();
        View view = LayoutInflater.from(context).inflate(R.layout.select_allone_tv,
                null);

        if (devices == null) {
            devices = new ArrayList<Device>();
        } else {
            devices.clear();
        }

        devices = mDeviceDao.selAlloneTvDevices(uid, deviceType, ModelID.Allone2);
        selectAlloneTVAdapter = new SelectAlloneTVAdapter(context, devices);
        ListView device_lv = (ListView) view.findViewById(R.id.device_lv);
        TextView title_tips = (TextView) view.findViewById(R.id.title_tips);
        Button leftButton = (Button) view.findViewById(R.id.leftButton);
        Button rightButton = (Button) view.findViewById(R.id.rightButton);
        device_lv.setOnItemClickListener(this);
        device_lv.setAdapter(selectAlloneTVAdapter);

        String textTip = String.format(context.getString(R.string.allone_bind_tv_tips), AlloneUtil.getDeviceName(mainDeviceType));
        title_tips.setText(textTip);
        if (devices.isEmpty()) {
            title_tips.setText(textTip + "\n\n" + context.getString(R.string.allone_bind_tv_tips1));
        }
        popup = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        PopupWindowUtil.initPopup(popup,
                context.getResources().getDrawable(R.drawable.gray_ligth), 1);
        popup.showAtLocation(view, Gravity.CENTER, 0, 0);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popup.isShowing()) {
                    popup.dismiss();
                }
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedDevice != null)
                    bindTV(selectedDevice);
                dismiss();
            }
        });
    }

    public void dismiss() {
        PopupWindowUtil.disPopup(popup);
    }

    public boolean isShowing() {
        return popup != null && popup.isShowing();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedDevice = (Device) view.getTag(R.id.tag_device);

        for (int i = 0; i < devices.size(); i++) {
            if (position == i) {
                if (SelectAlloneTVAdapter.isSelected.get(i) == false) {
                    SelectAlloneTVAdapter.isSelected.put(i, true);
                } else {
                    SelectAlloneTVAdapter.isSelected.put(i, false);
                }
            } else {
                SelectAlloneTVAdapter.isSelected.put(i, false);
            }
        }
        selectAlloneTVAdapter.notifyDataSetChanged();

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbDevice);
        checkBox.setChecked(true);

    }

    public abstract void bindTV(Device device);
}
