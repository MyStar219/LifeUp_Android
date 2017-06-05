package com.orvibo.homemate.device;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.DeviceStatus;
import com.orvibo.homemate.core.product.ProductManage;
import com.orvibo.homemate.dao.DeviceStatusDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceStatusConstant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.OnlineStatus;
import com.orvibo.homemate.data.ToastType;
import com.orvibo.homemate.device.control.ClotheShorseActivity;
import com.orvibo.homemate.device.control.SocketStatusActivity;
import com.orvibo.homemate.device.ys.YsCameraActivity;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.NetUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.tencent.stat.StatService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceAdapter extends BaseAdapter {
    private static final String TAG = DeviceAdapter.class.getSimpleName();
    private Context context;
    private LayoutInflater mLayoutInflater;
    // 映射数据
    private List<Device> devices;

    private DeviceStatusDao deviceStatusDao;

    /**
     *
     */
    public Map<String, DeviceStatus> socketStatus = new HashMap<String, DeviceStatus>();

    public DeviceAdapter(Context context, List<Device> devices) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.devices = devices;
        deviceStatusDao = new DeviceStatusDao();
        for (Device device : devices) {
            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(device.getUid(), device.getDeviceId());
            socketStatus.put(device.getUid(), deviceStatus);
        }
    }

    public void refresh(List<Device> devices) {
        this.devices = devices;
        for (Device device : devices) {
            DeviceStatus deviceStatus = deviceStatusDao.selDeviceStatus(device.getUid(), device.getDeviceId());
            socketStatus.put(device.getUid(), deviceStatus);
        }
        this.notifyDataSetChanged();
    }

    public void refresh(String uid, DeviceStatus deviceStatus) {
        socketStatus.put(uid, deviceStatus);
        // LibLog.d(TAG, "refresh()-uid:" + uid + ",status:" + status);
        // LibLog.d(TAG, "refresh()-socketStatus:" + socketStatus);
        this.notifyDataSetChanged();
    }

    public void refreshOnline(String uid, String deviceId, int online) {
        LogUtil.d(TAG, "refreshOnline()-uid:" + uid + ",deviceId:" + deviceId + ",online:" + online);
        DeviceStatus deviceStatus = socketStatus.get(uid);
        if (deviceStatus != null) {
            deviceStatus.setOnline(online);
            socketStatus.put(uid, deviceStatus);
            this.notifyDataSetChanged();
        }
    }

    // 获取数据量
    @Override
    public int getCount() {
        return devices.size();
    }

    // 获取对应ID项对应的Item
    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    // 获取对应项ID
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            //由于我们只需要将XML转化为View，并不涉及到具体的布局，所以第二个参数通常设置为null
            convertView = mLayoutInflater.inflate(R.layout.device_item, null);
            holder.img = (ImageView) convertView.findViewById(R.id.deviceImageView);
            holder.title = (TextView) convertView.findViewById(R.id.deviceNameTextView);
            holder.offline = (TextView) convertView.findViewById(R.id.offlineTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Device device = devices.get(position);
        String uid = device.getUid();
        final DeviceStatus deviceStatus = socketStatus.get(uid);
        int iconRes = 0;
        boolean offLine = true;
        if (deviceStatus == null) {
            offLine = false;
            iconRes = getIcon(device, 0, offLine);
        } else {
            offLine = deviceStatus.getOnline() == OnlineStatus.OFFLINE;
            iconRes = getIcon(device, deviceStatus.getValue1(), offLine);
        }
        if (iconRes != 0) {
            holder.img.setImageResource(iconRes);
        }
        if (offLine) {
            holder.offline.setVisibility(View.VISIBLE);
        } else {
            holder.offline.setVisibility(View.GONE);
        }

        if (!StringUtil.isEmpty(device.getDeviceName())) {
            holder.title.setText(device.getDeviceName());
        } else {
            holder.title.setText(R.string.device_name_no);
        }
        convertView.setTag(R.id.device, device);

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 判断网络是否连接
                if (!NetUtil.isNetworkEnable(context)) {
                    ToastUtil.showToast(R.string.network_canot_work, ToastType.NULL, Toast.LENGTH_SHORT);
                    return;
                }

              /*  if (deviceStatus.getOnline() == 0) {
                    ToastUtil.showPopup(context, R.string.offline, Toast.LENGTH_SHORT);
                    return;
                }*/

                Intent intent;
                if (device.getDeviceType() == DeviceType.CLOTHE_SHORSE) {
                    intent = new Intent(context, ClotheShorseActivity.class);
                } else if (device.getDeviceType() == DeviceType.CAMERA) {
                    intent = new Intent(context, YsCameraActivity.class);
                } else {
                    StatService.trackCustomKVEvent(context, context.getString(R.string.MTAClick_MyDevice_COCOIcon), null);
                    intent = new Intent(context, SocketStatusActivity.class);
                }
                intent.putExtra(Constant.DEVICE, device);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    // ViewHolder用于缓存控件
    class ViewHolder {
        public ImageView img;
        public TextView title;
        public TextView offline;
    }

    private int getIcon(Device device, int status, boolean offline) {
        int iconResId = 0;
        int deviceType = device.getDeviceType();
        ProductManage pm = ProductManage.getInstance();
        if (pm.isCOCO(deviceType)) {
            if (offline) {
                iconResId = R.drawable.socket_offline;
            } else {
                if (status == DeviceStatusConstant.ON) {
                    iconResId = R.drawable.socket_on;
                } else {
                    iconResId = R.drawable.socket_off;
                }
            }
        } else if (pm.isCLH(deviceType)) {
            if (offline) {
                iconResId = R.drawable.icon_clothes_hanger_offlie;
            } else {
                if (status == DeviceStatusConstant.ON) {
                    iconResId = R.drawable.icon_clothes_hanger_on;
                } else {
                    iconResId = R.drawable.icon_clothes_hanger_off;
                }
            }
        } else if (pm.isS20(deviceType)) {
            if (pm.isS20(device.getModel())) {
                if (offline) {
                    iconResId = R.drawable.icon_s20_offline;
                } else {
                    if (status == DeviceStatusConstant.ON) {
                        iconResId = R.drawable.icon_s20_on;
                    } else {
                        iconResId = R.drawable.icon_s20_off;
                    }
                }
            } else {
                if (offline) {
                    iconResId = R.drawable.icon_socket_offline;
                } else {
                    if (status == DeviceStatusConstant.ON) {
                        iconResId = R.drawable.icon_socket_on;
                    } else {
                        iconResId = R.drawable.icon_socket_off;
                    }
                }
            }
        } else if (deviceType == DeviceType.CAMERA) {
            iconResId = R.drawable.icon_camera_on;
        } else {
            if (offline) {
                iconResId = R.drawable.socket_offline;
            } else {
                if (status == DeviceStatusConstant.ON) {
                    iconResId = R.drawable.socket_on;
                } else {
                    iconResId = R.drawable.socket_off;
                }
            }
        }
        return iconResId;
    }
}
