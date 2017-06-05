package com.orvibo.homemate.device.bind.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.BindAction;
import com.orvibo.homemate.util.DeviceTool;

import java.util.List;

/**
 * 绑定设备动作或情景动作的列表
 * Created by huangqiyao on 2015/5/4.
 */
public class BindSelectedDeviceActionsAdapter extends BaseAdapter {
    private static final String TAG = BindSelectedDeviceActionsAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private Context mContext;
    private List<BindAction> mBindActions;
    private final String ROOM_NOT_SET;
    private final View.OnClickListener mClickListener;

    public BindSelectedDeviceActionsAdapter(Context context, List<BindAction> bindActions, View.OnClickListener listener) {
        this.mBindActions = bindActions;
        this.mClickListener = listener;
        mInflater = LayoutInflater.from(context);
        ROOM_NOT_SET = context.getString(R.string.device_manage_room_not_set);
        mContext = context;
    }

    public void refresh(List<BindAction> bindActions) {
        this.mBindActions = bindActions;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBindActions == null ? 0 : mBindActions.size();
    }

    @Override
    public Object getItem(int position) {
        return (mBindActions == null || position >= mBindActions.size()) ? null : mBindActions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.scene_action_item,
                    parent, false);
            holder = new ViewHolder();
            holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
            holder.ivDelete = (ImageView) convertView.findViewById(R.id.ivDelete);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.tvAction = (TextView) convertView.findViewById(R.id.tvAction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.ivDelete.setOnClickListener(mClickListener);
        holder.tvTime.setOnClickListener(mClickListener);
        holder.tvAction.setOnClickListener(mClickListener);

        BindAction bindAction = mBindActions.get(position);
        holder.tvLocation.setText(bindAction.getItemName());
        holder.tvTime.setText(bindAction.getDelayTime());
        holder.tvAction.setText(DeviceTool.getActionName(mContext, bindAction.getCommand(),
                bindAction.getValue1(), bindAction.getValue2(), bindAction.getValue3(), bindAction.getValue4(),bindAction.getDeviceId()));
        convertView.setTag(R.id.tag_device, bindAction);
        return convertView;
    }

    private class ViewHolder {
        TextView tvLocation;
        ImageView ivDelete;
        TextView tvTime;
        TextView tvAction;
    }

}
