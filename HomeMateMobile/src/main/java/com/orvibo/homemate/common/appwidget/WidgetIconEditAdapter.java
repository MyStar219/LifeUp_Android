package com.orvibo.homemate.common.appwidget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.ImageName;
import com.orvibo.homemate.data.Constant;

import java.util.List;

/**
 * Created by wuliquan on 2016/6/13.
 */
public class WidgetIconEditAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Resources mRes;
    private TypedArray icons;
    private int mSelectedId = Constant.INVALID_NUM;
    private List<ImageName> mImageNames;
    public WidgetIconEditAdapter(Context context, List<ImageName> imageNames, int selectedId) {
        this.mImageNames = imageNames;
        mRes = context.getResources();
        icons = mRes.obtainTypedArray(R.array.widget_icons);
        mInflater = LayoutInflater.from(context);
        mSelectedId = selectedId;
    }

    public void select(int selectedId) {
        mSelectedId = selectedId;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mImageNames == null ? 0 : mImageNames.size();
    }

    @Override
    public Object getItem(int position) {
        return (mImageNames == null || mImageNames.size() <= position) ? Constant.INVALID_NUM : mImageNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (mImageNames == null || mImageNames.size() <= position) ? Constant.INVALID_NUM : mImageNames.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_widget_icon, parent, false);
            holder.icon = (ImageView) convertView.findViewById(R.id.typeIcon_iv);
            holder.name = (TextView) convertView.findViewById(R.id.typeName_tv);
            holder.selected = (ImageView) convertView.findViewById(R.id.scene_icon_select_mark);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        convertView.setLayoutParams(lp);

        final ImageName imageName = mImageNames.get(position);
        int id = imageName.getId();
        if (id == mSelectedId) {
            holder.selected.setVisibility(View.VISIBLE);
        } else {
            holder.selected.setVisibility(View.INVISIBLE);
        }

        holder.icon.setImageDrawable(icons.getDrawable(imageName.getImgResId()));
        holder.name.setText(imageName.getNameResId());
        return convertView;
    }

    class ViewHolder{
        private ImageView icon;
        private TextView name;
        private ImageView selected;
    }

}
