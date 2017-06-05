package com.orvibo.homemate.device.bind.adapter;

import android.content.Context;
import android.content.res.Resources;
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
 * Created by huangqiyao on 2015/4/30.
 * <p/>
 */
public class SelectDeviceTypeAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final Resources mRes;
    private int mSelectedId = Constant.INVALID_NUM;
    private List<ImageName> mImageNames;
//    private final AbsListView.LayoutParams lp;

    public SelectDeviceTypeAdapter(Context context, List<ImageName> imageNames, int selectedId) {
        this.mImageNames = imageNames;
        mRes = context.getResources();
        mInflater = LayoutInflater.from(context);
        mSelectedId = selectedId;

//        int pW = PhoneUtil.getScreenPixels((Activity) context)[0];
//        int w = pW * 180 / 750;
//        lp = new AbsListView.LayoutParams(w, w);
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
            convertView = mInflater.inflate(R.layout.item_device_type, parent, false);
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

        if (position == 5) {
            holder.icon.setImageResource(R.drawable.scene_pic_blank);
            holder.name.setText("");
        } else {
            holder.icon.setImageResource(imageName.getImgResId());
            holder.name.setText(imageName.getNameResId());
        }
        return convertView;
    }

    private class ViewHolder {
        private ImageView icon;
        private TextView name;
        private ImageView selected;
    }


}
