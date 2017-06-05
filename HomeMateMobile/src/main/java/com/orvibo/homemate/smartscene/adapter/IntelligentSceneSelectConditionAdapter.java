package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.data.IntelligentSceneConditionType;
import com.orvibo.homemate.util.DeviceTool;
import com.orvibo.homemate.util.IntelligentSceneTool;

import java.util.List;

/**
 * Created by Smagret on 2015/10/17.
 */
public class IntelligentSceneSelectConditionAdapter extends BaseAdapter {
    private Resources mRes;
    private List<Integer> mConditions;
    private volatile int curConditionType = IntelligentSceneConditionType.OTHER;

    public IntelligentSceneSelectConditionAdapter(Context context, List<Integer> conditions, int conditionType) {
        mConditions = conditions;
        mRes = context.getResources();
        curConditionType = conditionType;
    }

    /**
     * 设置当前选中类型，item相应显示对应的颜色
     *
     * @param conditionType
     */
    public final void setSelectedConditionType(int conditionType) {
        curConditionType = conditionType;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mConditions == null ? 0 : mConditions.size();
    }

    @Override
    public Object getItem(int position) {
        return mConditions == null ? null : mConditions.get(position);
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
            convertView = View.inflate(parent.getContext(), R.layout.intelligent_scene_condition_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.typeIcon_iv);
            holder.name = (TextView) convertView.findViewById(R.id.typeName_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int conditionType = mConditions.get(position);
        holder.name.setText(IntelligentSceneTool.getConditionTypeNameResId(conditionType));
        if (conditionType == IntelligentSceneConditionType.CLICK_ALLONE) {
            //小方图标
            Device device = new Device();
            device.setDeviceType(DeviceType.ALLONE);
            Drawable drawable = DeviceTool.getDeviceDrawable(device, true);
            holder.icon.setImageDrawable(drawable);
        } else {
            final int conditionResid = IntelligentSceneTool.getConditionIconResId(conditionType, true);
            holder.icon.setImageResource(conditionResid);
        }

        convertView.setTag(R.id.tag_conditionType, conditionType);
        if (curConditionType == conditionType) {
            holder.name.setTextColor(mRes.getColor(R.color.scene_green));
        } else {
            holder.name.setTextColor(mRes.getColor(R.color.font_black));
        }
        return convertView;
    }

    class ViewHolder {
        private ImageView icon;
        private TextView name;
    }
}
