package com.orvibo.homemate.common.appwidget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wuliquan on 2016/6/6.
 */
public class WidgetSettingAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private int maxSelect;
    private Context context;
    private List<Scene> listSceneItem = new ArrayList<>();
    private List<WidgetItem> defaultList = new ArrayList<>();
    private HashMap<Integer,Boolean> status = new HashMap<>();

    public WidgetSettingAdapter(Context context, List<WidgetItem> list, List<Scene> listSceneItem) {
        this.context=context;
        mInflater = LayoutInflater.from(context);
        if (listSceneItem!=null) {
            this.listSceneItem = listSceneItem;

        }
        if(list!=null){
            defaultList.clear();
            defaultList.addAll(list);
        }
        initHashMap();
    }
    private void initHashMap(){
        int size = listSceneItem.size();
        for (int i=0;i<size;i++){
            Scene scene = listSceneItem.get(i);
            status.put(i,false);
            for(WidgetItem widgetItem:defaultList){
                if (widgetItem.getTableId()==scene.getSceneId()){
                    status.put(i,true);
                    break;
                }
            }

        }
    }

    public void setMaxSelect(int Max){
        this.maxSelect = Max;
    }
    @Override
    public int getCount() {
        return listSceneItem.size();
    }

    @Override
    public Object getItem(int position) {
        return listSceneItem==null?null:listSceneItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SenceViewHolder sceneViewHolder;
        if (convertView == null) {
            sceneViewHolder = new SenceViewHolder();
            convertView = mInflater.inflate(R.layout.item_choose_widget, parent, false);
            sceneViewHolder.name = (TextView) convertView.findViewById(R.id.senceName_tv);
            sceneViewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.state_checkBox);

            convertView.setTag(sceneViewHolder);
        } else {
            sceneViewHolder = (SenceViewHolder) convertView.getTag();
        }

            boolean state=status.get(position)==null?false:status.get(position);
            sceneViewHolder.checkBox.setChecked(state);
            sceneViewHolder.name.setText(listSceneItem.get(position).getSceneName());
            sceneViewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(status.get(position)){
                        status.put(position,false);
                    }else{
                        if(!judgeIsOut()) {
                            status.put(position, true);
                        }else{
                            ToastUtil.showToast(context.getString(R.string.out_widget_scene_tip));
                        }
                    }
                    notifyDataSetChanged();
                }
            });

        return convertView;
    }

    //判断是否越界
    private boolean judgeIsOut(){
        int size = status.size();
        int sceneSize = 0;
        for (int i=0;i<size;i++){
            if(status.get(i)){
                sceneSize++;
                if (sceneSize >= maxSelect) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 在这里做空数据填充
     * @return
     */
    public ArrayList<Scene> getSelected(){

        ArrayList<Scene> list = new ArrayList<>();
        for(int i=0;i<listSceneItem.size();i++){
            if(status.containsKey(i)&&status.get(i)){

                list.add(listSceneItem.get(i));
            }
        }
        return list;
    }
    class SenceViewHolder{
        TextView name;
        CheckBox checkBox;
    }
}
