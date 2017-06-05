package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.LinkageOutput;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.view.custom.ActionView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Smagret on 2015/10/16.
 */
public class LinkageBindAdapter extends BaseAdapter {
    private static final String TAG = LinkageBindAdapter.class.getSimpleName();
    private Context mContext;
    private final LayoutInflater mInflater;
    private final Resources mRes;
    private List<LinkageOutput> linkageOutputs;
    private final DeviceDao mDeviceDao;
    private View.OnClickListener mClickListener;
    private ConcurrentHashMap<String, String> mTitles;
    private boolean isCommonLinkage = false;
    private boolean isSecurity = true;

    public LinkageBindAdapter(Context context, List<LinkageOutput> linkageOutputs, View.OnClickListener clickListener) {
        this.mContext = context;
        this.mClickListener = clickListener;
        mInflater = LayoutInflater.from(context);
        mRes = context.getResources();
        mDeviceDao = new DeviceDao();
        mTitles = new ConcurrentHashMap<String, String>();
        this.linkageOutputs = removeDeletedDevices(linkageOutputs);
    }

    public void refreshList(List<LinkageOutput> linkageOutputs) {
        this.linkageOutputs = removeDeletedDevices(linkageOutputs);
        notifyDataSetChanged();
    }

    /**
     * 移除设备被删除，但sceneBid没有删除的linkageOutput。
     *
     * @param linkageOutputs
     * @return
     */
    private List<LinkageOutput> removeDeletedDevices(List<LinkageOutput> linkageOutputs) {
        ArrayList<LinkageOutput> tempLinkageOutputs = new ArrayList<LinkageOutput>();
//        List<LinkageOutput> binds = new ArrayList<LinkageOutput>();
        for (LinkageOutput linkageOutput : linkageOutputs) {
            final String deviceId = linkageOutput.getDeviceId();
            if (mDeviceDao.selDevice(deviceId) != null) {
                tempLinkageOutputs.add(linkageOutput);
                LogUtil.d(TAG, "removeDeletedDevices()-" + linkageOutput);
            } else {
                LogUtil.w(TAG, "removeDeletedDevices()-" + linkageOutput + " has been deleted.");
            }
        }
        return tempLinkageOutputs;
    }

    @Override
    public int getCount() {
        return linkageOutputs == null ? 0 : linkageOutputs.size();
    }

    @Override
    public Object getItem(int position) {
        return linkageOutputs == null || linkageOutputs.size() < position ? null : linkageOutputs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 布防和撤防时的第一个item需要特别处理,不可编辑

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.security_action_item, parent, false);
            holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
            holder.ivDelete = (ImageView) convertView.findViewById(R.id.ivDelete);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.av_bindaction = (ActionView) convertView.findViewById(R.id.av_bindaction);
            holder.linearAction = (LinearLayout) convertView.findViewById(R.id.linearAction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        LinkageOutput linkageOutput = linkageOutputs.get(position);
        final String uid = linkageOutput.getUid();
        final String deviceId = linkageOutput.getDeviceId();

        //所在楼层房间、设备名称
        holder.tvLocation.setText(getTitleName(uid, deviceId));

        holder.ivDelete.setOnClickListener(mClickListener);
        holder.ivDelete.setTag(linkageOutput);

        //延时时间
        int delayTime = linkageOutput.getDelayTime() / 10;//单位s
        holder.tvTime.setText(TimeUtil.getDelayTimeTip2(mContext, delayTime));
        holder.tvTime.setOnClickListener(mClickListener);
        holder.tvTime.setTag(linkageOutput);

        /**
         * 动作
         */
//        String action = DeviceTool.getActionName(mContext, linkageOutput.getCommand(), linkageOutput.getValue1(),
//                linkageOutput.getValue2(), linkageOutput.getValue3(), linkageOutput.getValue4(), linkageOutput.getDeviceId());
        String action = DeviceTool.getActionName(mContext, linkageOutput);
        holder.av_bindaction.setAction(linkageOutput);
//        if (action.equals(mRes.getString(R.string.action_stop))) {
//            action = mRes.getString(R.string.action_on);
//        }
//        if (DeviceOrder.COLOR_CONTROL.equals(linkageOutput.getCommand())) {
//            int[] rgb = ColorUtil.hsl2DeviceRgb(linkageOutput.getValue4(), linkageOutput.getValue3(), linkageOutput.getValue2());
//            holder.colorView.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
//            holder.color_ll.setVisibility(View.VISIBLE);
////            holder.tvAction.setText(mContext.getString(R.string.action_color_text));
//            holder.tvAction.setText("");
//        } else if (DeviceOrder.COLOR_TEMPERATURE.equals(linkageOutput.getCommand())) {
//            int colorTemperture = ColorUtil.colorToColorTemperture(linkageOutput.getValue3());
//            double[] rgb = ColorUtil.colorTemperatureToRGB(colorTemperture);
//            int red = (int) rgb[0];
//            int green = (int) rgb[1];
//            int blue = (int) rgb[2];
//            holder.colorView.setBackgroundColor(Color.rgb(red, green, blue));
//            holder.color_ll.setVisibility(View.VISIBLE);
////            holder.tvAction.setText(mContext.getString(R.string.action_color_text));
//            holder.tvAction.setText("");
//        } else {
//            holder.color_ll.setVisibility(View.GONE);
//            Device device = mDeviceDao.selDevice(uid, deviceId);
//            if (device != null && device.getDeviceType() == DeviceType.CURTAIN_PERCENT && !DeviceOrder.STOP.equals(linkageOutput.getCommand()) && linkageOutput.getValue1() != 0 && linkageOutput.getValue1() != 100) {
//                String frequentlyModeString = FrequentlyModeUtil.getFrequentlyModeString(uid, deviceId, linkageOutput.getValue1());
//                if (!StringUtil.isEmpty(frequentlyModeString)) {
//                    holder.tvAction.setText(frequentlyModeString);
//                } else {
//                    holder.tvAction.setText(action);
//                }
//            } else {
//                holder.tvAction.setText(action);
//            }
//        }

        holder.linearAction.setOnClickListener(mClickListener);
        holder.linearAction.setTag(linkageOutput);
        return convertView;
    }


    public String getItemTitle(String uid, String deviceId) {
        String title = mTitles.get(getTitleKey(uid, deviceId));
        if (title == null) {
            title = "";
        }
        return title;
    }

    private String getTitleName(String uid, String deviceId) {
        String name = null;
        final String key = getTitleKey(uid, deviceId);
        if (mTitles.containsKey(key)) {
            name = mTitles.get(key);
        } else {
            String[] data = DeviceTool.getBindItemName(mContext, deviceId);
            name = data[0] + "" + data[1] + " " + data[2];
        }
        if (name == null) {
            name = "";
        }
        return name;
    }

    private String getTitleKey(String uid, String deviceId) {
        return uid + "_" + deviceId;
    }

    private class ViewHolder {
        private TextView tvLocation;
        private ImageView ivDelete;
        private TextView tvTime;
        private LinearLayout linearAction;
        private ActionView av_bindaction;
    }
}
