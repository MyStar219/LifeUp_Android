package com.orvibo.homemate.device.control.infrareddevice;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.DeviceIr;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.view.custom.IrButton;

import java.util.List;

/**
 * Created by Allen on 2015/4/3.
 * Modified by smagret on 2015/05/07
 */
public class SelfRemoteButtonAdapter extends BaseAdapter implements IrButton.OnControlResultListener {
    private static final String TAG = SelfRemoteButtonAdapter.class.getSimpleName();
    private List<DeviceIr> deviceIrs;
    private Activity activity;
    private OnSetSelfRemoteButtonNameListener onSetSelfRemoteButtonNameListener;
    private OnActionSelectResultListener onActionSelectResultListener;
    /**
     * 1 学习; 2 控制; 3编辑; 4动作
     */
    private int type = 0;
    public final static int TYPE_LEARN = 1;
    public final static int TYPE_CONTROL = 2;
    public final static int TYPE_EDIT = 3;
    public final static int TYPE_ACTION = 4;

    private String selectedOrder;

    /**
     * @param activity
     * @param deviceIrs
     * @param type      1 学习; 2 控制; 3编辑; 4动作
     */
    public SelfRemoteButtonAdapter(Activity activity, List<DeviceIr> deviceIrs, int type) {
        this.deviceIrs = deviceIrs;
        this.activity = activity;
        this.type = type;
    }

    public void refresh(List<DeviceIr> deviceIrs) {
        this.deviceIrs = deviceIrs;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        if (type == TYPE_LEARN) {
            return deviceIrs == null ? 0 : deviceIrs.size() + 1;
        } else {
            return deviceIrs == null ? 0 : deviceIrs.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return deviceIrs == null ? null : deviceIrs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void refreshSelectBg(String command) {
        selectedOrder = command;
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (type == TYPE_LEARN) {
            final IrButton irButton;
            //由于inflate不同布局，所以不能复用
//            if (convertView == null) {
            if (position == getCount() - 1) {
                convertView = View.inflate(parent.getContext(), R.layout.self_remote_button_add, null);
            } else {
                convertView = View.inflate(parent.getContext(), R.layout.self_remote_button_item, null);
            }
            irButton = (IrButton) convertView;
//                convertView.setTag(irButton);
//            } else {
//                irButton = (IrButton) convertView.getTag();
//            }
            if (position < getCount() - 1) {
                final DeviceIr deviceIr = deviceIrs.get(position);
                irButton.setText(deviceIr.getKeyName());
                irButton.setKeyName(deviceIr.getKeyName());
                irButton.setOrder(deviceIr.getCommand());
                irButton.initStatus(activity, deviceIr.getUid(), UserCache.getCurrentUserName(activity), deviceIr.getDeviceId());
                irButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        irButton.onlongClick();
                        //取消短按事件
                        return true;
                    }
                });

                irButton.setOnControlResultListener(this);
                irButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        irButton.onClick();
                    }
                });

            } else if (position == getCount() - 1) {
                irButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSetSelfRemoteButtonNameListener.onSetSelfRemoteButtonName();
                    }
                });
            }
        } else if (type == TYPE_CONTROL) {
            final IrButton irButton;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.self_remote_button_item, null);
                irButton = (IrButton) convertView;
                convertView.setTag(irButton);
            } else {
                irButton = (IrButton) convertView.getTag();
            }
            final DeviceIr deviceIr = deviceIrs.get(position);
            irButton.setText(deviceIr.getKeyName());
            irButton.setKeyName(deviceIr.getKeyName());
            irButton.setOrder(deviceIr.getCommand());
            irButton.initStatus(activity, deviceIr.getUid(), UserCache.getCurrentUserName(activity), deviceIr.getDeviceId());
            irButton.setOnControlResultListener(this);
            irButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    irButton.onClick();
                }
            });
        } else if (type == TYPE_EDIT) {
            final IrButton irButton;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.self_remote_button_item, null);
                irButton = (IrButton) convertView;
                convertView.setTag(irButton);
            } else {
                irButton = (IrButton) convertView.getTag();
            }
            final DeviceIr deviceIr = deviceIrs.get(position);
            irButton.setText(deviceIr.getKeyName());
            irButton.setKeyName(deviceIr.getKeyName());
            irButton.setOrder(deviceIr.getCommand());
            irButton.initStatus(activity, deviceIr.getUid(), UserCache.getCurrentUserName(activity), deviceIr.getDeviceId());
            irButton.setFocusable(false);
            irButton.setClickable(false);
        } else if (type == TYPE_ACTION) {
            final IrButton irButton;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.self_remote_button_item, null);
                irButton = (IrButton) convertView;
                convertView.setTag(irButton);
            } else {
                irButton = (IrButton) convertView.getTag();
            }
            final DeviceIr deviceIr = deviceIrs.get(position);
            irButton.setText(deviceIr.getKeyName());
            irButton.setKeyName(deviceIr.getKeyName());
            irButton.setOrder(deviceIr.getCommand());
            irButton.initStatus(activity, deviceIr.getUid(), UserCache.getCurrentUserName(activity), deviceIr.getDeviceId());

            irButton.setOnCheckedResultListener(new IrButton.OnCheckedResultListener() {
                @Override
                public void onCheckedResult(String keyName, String order, boolean is_learned) {
                    onActionSelectResultListener.onActionSelectResult(keyName, order, is_learned);
                }
            });
            irButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return true;
                }
            });
            irButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedOrder = irButton.getOrder();
                    notifyDataSetChanged();
                }
            });

            if (irButton.getOrder().equals(selectedOrder)) {
                irButton.onChecked();
            } else {
                irButton.onUnChecked();
            }

        }
        return convertView;
    }

    @Override
    public void onControlResult(int result) {
        if (result == ErrorCode.SUCCESS) {
        }
    }

    /**
     * @param onSetSelfRemoteButtonNameListener
     */
    public void setOnToActivityListener(OnSetSelfRemoteButtonNameListener onSetSelfRemoteButtonNameListener) {
        this.onSetSelfRemoteButtonNameListener = onSetSelfRemoteButtonNameListener;
    }

    public interface OnSetSelfRemoteButtonNameListener {

        public void onSetSelfRemoteButtonName();
    }

    /**
     * @param onActionSelectResultListener
     */
    public void setOnActionSelectResultListener(OnActionSelectResultListener onActionSelectResultListener) {
        this.onActionSelectResultListener = onActionSelectResultListener;
    }

    public interface OnActionSelectResultListener {

        public void onActionSelectResult(String keyName, String order, boolean is_learned);
    }

}
