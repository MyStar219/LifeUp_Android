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
import com.orvibo.homemate.bo.SceneBind;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.TimeUtil;
import com.orvibo.homemate.view.custom.ActionView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangqiyao on 2015/6/1.
 */
public class SceneBindAdapter extends BaseAdapter {
    private static final String TAG = SceneBindAdapter.class.getSimpleName();
    private Context mContext;
    private final LayoutInflater mInflater;
    private final Resources mRes;
    private List<SceneBind> sceneBinds;
    private final DeviceDao mDeviceDao;
    private View.OnClickListener mClickListener;
    private ConcurrentHashMap<String, String> mTitles;

    public SceneBindAdapter(Context context, List<SceneBind> sceneBinds, View.OnClickListener clickListener) {
        this.mContext = context;
        this.mClickListener = clickListener;
        mInflater = LayoutInflater.from(context);
        mRes = context.getResources();
        mDeviceDao = new DeviceDao();
        mTitles = new ConcurrentHashMap<String, String>();
        this.sceneBinds = removeDeletedDevices(sceneBinds);
    }

    public void refreshList(List<SceneBind> sceneBinds) {
        this.sceneBinds = removeDeletedDevices(sceneBinds);
        notifyDataSetChanged();
    }

    /**
     * 移除设备被删除，但sceneBid没有删除的sceneBind。
     *
     * @param sceneBinds
     * @return
     */
    private List<SceneBind> removeDeletedDevices(List<SceneBind> sceneBinds) {
        ArrayList<SceneBind> tempSceneBinds = new ArrayList<SceneBind>();
//        List<SceneBind> binds = new ArrayList<SceneBind>();
        for (SceneBind sceneBind : sceneBinds) {
            final String uid = sceneBind.getUid();
            final String deviceId = sceneBind.getDeviceId();
            if (mDeviceDao.selDevice(uid, deviceId) != null) {
                tempSceneBinds.add(sceneBind);
                LogUtil.d(TAG, "removeDeletedDevices()-" + sceneBind);
            } else {
                LogUtil.w(TAG, "removeDeletedDevices()-" + sceneBind + " has been deleted.");
            }
        }
        return tempSceneBinds;
    }

    @Override
    public int getCount() {
        return sceneBinds == null ? 0 : sceneBinds.size();
    }

    @Override
    public Object getItem(int position) {
        return sceneBinds == null || sceneBinds.size() < position ? null : sceneBinds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.security_action_item, parent, false);
            holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
            holder.ivDelete = (ImageView) convertView.findViewById(R.id.ivDelete);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.av_bindaction = (ActionView) convertView.findViewById(R.id.av_bindaction);
            //  holder.colorView = (ImageView) convertView.findViewById(R.id.colorView);
            holder.linearAction = (LinearLayout) convertView.findViewById(R.id.linearAction);
            //holder.color_ll = (LinearLayout) convertView.findViewById(R.id.color_ll);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SceneBind sceneBind = sceneBinds.get(position);
        final String uid = sceneBind.getUid();
        final String deviceId = sceneBind.getDeviceId();

        //所在楼层房间、设备名称
        holder.tvLocation.setText(getTitleName(uid, deviceId));

        holder.ivDelete.setOnClickListener(mClickListener);
        holder.ivDelete.setTag(sceneBind);

        //延时时间
        holder.tvTime.setText(TimeUtil.getDelayTimeTip(mContext, sceneBind.getDelayTime() / 10));
//        holder.tvTime.setText(String.format(mRes.getString(R.string.scene_action_time), sceneBind.getDelayTime() / 10));
        holder.tvTime.setOnClickListener(mClickListener);
        holder.tvTime.setTag(sceneBind);

        /**
         * 动作
         */
        String action = DeviceTool.getActionName(mContext, sceneBind.getCommand(), sceneBind.getValue1(),
                sceneBind.getValue2(), sceneBind.getValue3(), sceneBind.getValue4(), sceneBind.getDeviceId());

//        if (action.equals(mRes.getString(R.string.action_stop))) {
//            action = mRes.getString(R.string.action_on);
//        }
        holder.av_bindaction.setAction(sceneBind);

//        if (DeviceOrder.COLOR_CONTROL.equals(sceneBind.getCommand())) {
//            int[] rgb = ColorUtil.hsl2DeviceRgb(sceneBind.getValue4(), sceneBind.getValue3(), sceneBind.getValue2());
//            holder.color_ll.setVisibility(View.VISIBLE);
//            holder.colorView.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
////            holder.tvAction.setText(mContext.getString(R.string.action_color_text));
//            holder.tvAction.setText("");
//        } else if (DeviceOrder.COLOR_TEMPERATURE.equals(sceneBind.getCommand())) {
//            int value2 = sceneBind.getValue2();
//            if (value2 == 0) {
//                holder.color_ll.setVisibility(View.GONE);
//                holder.tvAction.setText(R.string.scene_action_off);
//            } else {
//                int colorTemperture = ColorUtil.colorToColorTemperture(sceneBind.getValue3());
//                double[] rgb = ColorUtil.colorTemperatureToRGB(colorTemperture);
//                int red = (int) rgb[0];
//                int green = (int) rgb[1];
//                int blue = (int) rgb[2];
//                holder.colorView.setBackgroundColor(Color.rgb(red, green, blue));
//                holder.color_ll.setVisibility(View.VISIBLE);
////                holder.tvAction.setText(mContext.getString(R.string.action_color_text));
//                holder.tvAction.setText("");
//            }
//        } else {
//            holder.color_ll.setVisibility(View.GONE);
//            Device device = mDeviceDao.selDevice(sceneBind.getUid(), sceneBind.getDeviceId());
//            if (device.getDeviceType() == DeviceType.CURTAIN_PERCENT && !DeviceOrder.STOP.equals(sceneBind.getCommand()) && sceneBind.getValue1() != 0 && sceneBind.getValue1() != 100) {
//                String frequentlyModeString = FrequentlyModeUtil.getFrequentlyModeString(sceneBind.getUid(), sceneBind.getDeviceId(), sceneBind.getValue1());
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
        holder.linearAction.setTag(sceneBind);
//        holder.tvAction.setText(action);
        // holder.tvAction.setOnClickListener(mClickListener);
//        holder.tvAction.setTag(sceneBind);
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
            String[] data = DeviceTool.getBindItemName(mContext, uid, deviceId);
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
        // private TextView tvAction;
        // private ImageView colorView;
        private LinearLayout linearAction;
        // private LinearLayout color_ll;
        private ActionView av_bindaction;
    }
}
