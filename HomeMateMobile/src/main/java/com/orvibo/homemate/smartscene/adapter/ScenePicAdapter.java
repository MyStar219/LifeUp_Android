package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.SceneTool;

/**
 * Created by huangqiyao on 2015/4/27.
 */
public class ScenePicAdapter extends BaseAdapter {
    private Resources mRes;
    //private List<Scene> scenes;
    private int mSelectedPic;

    public ScenePicAdapter(Context context, int selectedPic) {
        mSelectedPic = selectedPic;
        // scenes = new ArrayList<>(9);
        mRes = context.getResources();
    }

    public void selectPic(int selectedPic) {
        mSelectedPic = selectedPic;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return 7;
//        return scenes == null ? 0 : scenes.size();
    }

    @Override
    public Object getItem(int position) {
//        return scenes == null ? null : scenes.get(position);
        return position;
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
            convertView = View.inflate(parent.getContext(), R.layout.scene_pick_item, null);
            holder.icon = (ImageView) convertView.findViewById(R.id.typeIcon_iv);
            holder.name = (TextView) convertView.findViewById(R.id.typeName_tv);
            holder.selected = (ImageView) convertView.findViewById(R.id.scene_icon_select_mark);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final int pic = position +3;
        if (pic == mSelectedPic) {
            holder.selected.setVisibility(View.VISIBLE);
        } else {
            holder.selected.setVisibility(View.INVISIBLE);
        }
        holder.name.setText(SceneTool.getSceneTypeNameResId(pic));
        final int picResid = SceneTool.getSceneIconResId(pic);
        holder.icon.setImageResource(picResid);
//        holder.tvSceneName.setCompoundDrawablesWithIntrinsicBounds(0, picResid, 0, 0);
//        convertView.setBackgroundResource(pic == mSelectedPic ? R.drawable.gridview_item_p : R.drawable.gridview_selector);
//        convertView.setTag(R.id.tag_scene, scene);
        return convertView;
    }

    class ViewHolder {
        private ImageView icon;
        private TextView name;
        private ImageView selected;
    }
}
