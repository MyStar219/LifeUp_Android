package com.orvibo.homemate.smartscene.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.util.LibSceneTool;
import com.orvibo.homemate.util.SceneTool;

import java.util.List;

/**
 * Created by Allen on 2015/4/3.
 */
public class SceneAdapter extends BaseAdapter {
    private static final String TAG = SceneAdapter.class.getSimpleName();
    private Resources mRes;
    private List<Scene> scenes;
    private int mSelectedSceneId;
    private Activity mAcitvity;

    public SceneAdapter(Activity activity, List<Scene> scenes) {
        this.scenes = LibSceneTool.sortScenes(scenes);
        mAcitvity = activity;
        mRes = activity.getResources();
    }

    public SceneAdapter(Activity activity, List<Scene> scenes, int selectedSceneId) {
//        this.scenes = scenes;
        this.scenes = LibSceneTool.sortScenes(scenes);
        mAcitvity = activity;
        mRes = activity.getResources();
        mSelectedSceneId = selectedSceneId;
    }

    public void selectScene(int sceneId) {
        mSelectedSceneId = sceneId;
        notifyDataSetChanged();
    }

    public void refresh(List<Scene> scenes) {
        this.scenes = LibSceneTool.sortScenes(scenes);
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

        Scene scene = scenes.get(position);
        if (mSelectedSceneId == scene.getSceneId()) {
            holder.selected.setVisibility(View.VISIBLE);
        } else {
            holder.selected.setVisibility(View.INVISIBLE);
        }

        holder.name.setText(scene.getSceneName());
        final int picResid = SceneTool.getSceneIconResId(scene.getPic());
        holder.icon.setImageResource(picResid);
        convertView.setTag(R.id.tag_scene, scene);
        return convertView;
    }

    class ViewHolder {
        private ImageView icon;
        private TextView name;
        private ImageView selected;
    }
}
