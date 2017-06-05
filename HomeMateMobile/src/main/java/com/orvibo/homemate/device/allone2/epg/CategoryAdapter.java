package com.orvibo.homemate.device.allone2.epg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuwei on 2016/4/13.
 */
public class CategoryAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<CategoryModel> categoryList;
    private int selectPostion = 0;

    public void setCategoryList(List<CategoryModel> categoryList) {
        if (categoryList == null)
            this.categoryList = new ArrayList<>();
        else
            this.categoryList = categoryList;
    }

    public CategoryAdapter(Context context, List<CategoryModel> categoryList) {
        mInflater = LayoutInflater.from(context);
        setCategoryList(categoryList);
    }

    public void changeData(List<CategoryModel> categoryList, int selectPostion) {
        this.selectPostion = selectPostion;
        setCategoryList(categoryList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
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
            convertView = mInflater.inflate(R.layout.program_guides_gridview_item, null);
            holder.iv_item_bg = (ImageView) convertView.findViewById(R.id.iv_item_bg);
            holder.tv_program_guide_name = (TextView) convertView.findViewById(R.id.tv_program_guide_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CategoryModel categoryModel = categoryList.get(position);
        holder.tv_program_guide_name.setText(categoryModel.getCategory());
        if (position == selectPostion)
            holder.iv_item_bg.setSelected(true);
        else
            holder.iv_item_bg.setSelected(false);
        return convertView;
    }

    class ViewHolder {
        ImageView iv_item_bg;
        TextView tv_program_guide_name;
    }
}
