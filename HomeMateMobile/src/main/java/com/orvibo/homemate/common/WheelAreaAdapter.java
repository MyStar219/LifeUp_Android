package com.orvibo.homemate.common;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.util.PhoneUtil;
import com.orvibo.homemate.view.custom.wheelview.TosGallery;

import java.util.List;

public class WheelAreaAdapter extends BaseAdapter {
    private Context mContext = null;
    private int mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int mHeight = 50;
    private List<String> mContents;

    private final int fontColor;

    public WheelAreaAdapter(Context context, List<String> contents) {
        mContext = context;
        mHeight = (int) PhoneUtil.pixelToDp(context, mHeight);
        mContents = contents;

        fontColor = context.getResources().getColor(R.color.font_black);
    }

    public void setData(List<String> contents) {
        mContents = contents;
        notifyDataSetChanged();
    }


    public String getString(int pos) {
        return mContents.get(pos);
    }

    @Override
    public int getCount() {
        return (null != mContents) ? mContents.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (null != mContents && mContents.size() > position) ? mContents.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = null;
        if (null == convertView) {
            convertView = new TextView(mContext);
            convertView.setLayoutParams(new TosGallery.LayoutParams(mWidth,
                    mHeight));
            textView = (TextView) convertView;
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            // textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            textView.setTextColor(fontColor);
        }

        if (null == textView) {
            textView = (TextView) convertView;
        }
        if (mContents != null && mContents.size() > 0) {
            String str = mContents.get(position);
            if (position < mContents.size()) {
                textView.setText(str);
            }
            convertView.setTag(R.id.tag_wheel, str);
        }
        return convertView;
    }
}
