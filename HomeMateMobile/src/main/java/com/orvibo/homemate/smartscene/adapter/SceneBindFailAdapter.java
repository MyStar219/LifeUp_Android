package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.SceneBind;
import com.orvibo.homemate.util.DeviceTool;

import java.util.List;

/**
 * Created by huangqiyao on 2015/6/1.
 */
public class SceneBindFailAdapter extends BaseAdapter {
    private Context mContext;
    private final LayoutInflater mInflater;
    private final Resources mRes;
    private List<SceneBind> sceneBinds;
//    private Set<String>

    public SceneBindFailAdapter(Context context, List<SceneBind> sceneBinds) {
        mContext = context;
        this.sceneBinds = sceneBinds;
        mInflater = LayoutInflater.from(context);
        mRes = context.getResources();
    }

    public void refreshList(List<SceneBind> sceneBinds) {
        this.sceneBinds = sceneBinds;
        notifyDataSetChanged();
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
            convertView = mInflater.inflate(R.layout.item_bind_fail, parent, false);
            holder.roomName_tv = (TextView) convertView.findViewById(R.id.roomName_tv);
            holder.deviceName_tv = (TextView) convertView.findViewById(R.id.deviceName_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SceneBind sceneBind = sceneBinds.get(position);
        String[] data = DeviceTool.getBindItemName(mContext, sceneBind.getUid(), sceneBind.getDeviceId());
        String name = data[0] + "" + data[1] + " " + data[2];
        if (name == null) {
            name = "";
        }
        holder.roomName_tv.setText(name);
        holder.deviceName_tv.setVisibility(View.GONE);
        // String action = DeviceTool.getActionName(mRes, sceneBind.getCommand(), sceneBind.getValue1(), sceneBind.getValue2(), sceneBind.getValue3(), sceneBind.getValue4());
        // holder.deviceName_tv.setText(name);
        return convertView;
    }

    private class ViewHolder {
        private TextView roomName_tv;
        private TextView deviceName_tv;
    }
}
