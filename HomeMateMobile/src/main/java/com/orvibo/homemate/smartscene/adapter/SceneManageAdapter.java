package com.orvibo.homemate.smartscene.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.util.SceneTool;

import java.util.List;

/**
 * Created by Allen on 2015/4/3.
 */
public class SceneManageAdapter extends BaseAdapter {
    private static final String TAG = SceneManageAdapter.class.getSimpleName();

    private List<Scene> scenes;
    private final Drawable rightArrowDrawable;

    public SceneManageAdapter(Context context, List<Scene> scenes) {
        this.scenes = scenes;
        rightArrowDrawable = context.getResources().getDrawable(R.drawable.device_item_arrow_right);
    }

    public void refresh(List<Scene> scenes) {
        this.scenes = scenes;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return scenes == null ? 0 : scenes.size();
    }

    @Override
    public Object getItem(int position) {
        return scenes == null ? null : scenes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.scene_manage_item, null);
            holder = new ViewHolder();
            holder.tvSceneName = (TextView) convertView.findViewById(R.id.tvSceneName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Scene scene = scenes.get(position);
        Drawable drawable = parent.getResources().getDrawable(SceneTool.getSceneIconResId(scene.getPic()));
        holder.tvSceneName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, rightArrowDrawable, null);
        holder.tvSceneName.setText(scene.getSceneName());
        convertView.setTag(R.id.tag_scene, scene);
        return convertView;
    }

    class ViewHolder {
        TextView tvSceneName;
    }
}
