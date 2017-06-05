package com.orvibo.homemate.smartscene.adapter;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.view.custom.SceneAnimDialog;

/**
 * Created by snown on 2015/10/17.
 * 模拟场景+动画展示
 */
public class IntelligentSceneSimulationAdapter extends BaseAdapter {
    private Context mContext;

    private FragmentManager fragmentManager;
    private AbsListView.LayoutParams lp;
    public IntelligentSceneSimulationAdapter(Context context, FragmentManager fragmentManager) {
        this.mContext = context;
        this.fragmentManager = fragmentManager;
        int h = AbsListView.LayoutParams.WRAP_CONTENT;
        if (context != null) {
            h = PhoneUtil.getScreenPixels((Activity) context)[1] / 5;
        }
        lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, h);
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ActionHolder actionHolder;
        if (null == convertView) {
            actionHolder = new ActionHolder();
            if (position == 0) {
                //情景模拟
                convertView = View.inflate(parent.getContext(), R.layout.intelligent_scene_simulation_list_item, null);
                actionHolder.activate = (TextView) convertView.findViewById(R.id.activateImageView);
                actionHolder.activate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SceneAnimDialog.newInstance(R.layout.scene_anim_dialog, R.raw.scene_home, (float) 0.4, mContext.getString(R.string.anim_scene_home_tip), null).show(fragmentManager, null);
                    }
                });
            } else {
                //联动模拟
                convertView = View.inflate(parent.getContext(), R.layout.intelligent_linkage_simulation_list_item, null);
                actionHolder.ckeck = (CheckBox) convertView.findViewById(R.id.check);
                actionHolder.ckeck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        actionHolder.ckeck.setChecked(isChecked);
                        if (isChecked) {
                            SceneAnimDialog.newInstance(R.layout.scene_linkage_anim_dialog, R.raw.scene_link_start, (float) 1, mContext.getString(R.string.anim_scene_open_tip), mContext.getString(R.string.anim_scene_open_tip1)).show(fragmentManager, null);
                        } else {
                            SceneAnimDialog.newInstance(R.layout.scene_linkage_anim_dialog, R.raw.scene_link_stop, (float) 1, mContext.getString(R.string.anim_scene_close_tip), mContext.getString(R.string.anim_scene_close_tip1)).show(fragmentManager, null);
                        }
                    }
                });
            }
        } else {
            actionHolder = (ActionHolder) convertView.getTag();
        }
        convertView.setLayoutParams(lp);
        return convertView;
    }


    class ActionHolder {
        private TextView activate;
        private CheckBox ckeck;
    }

}
