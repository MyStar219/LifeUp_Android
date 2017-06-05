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

/**
 * 年月日，时分选择adapter
 * Created by snown on 2016/5/28.
 */
public class WheelLockAdapter extends BaseAdapter {
    private Context mContext = null;
    private int mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int mHeight = 50;
    private List<Integer> times;//时间
    private String suffix;//时间后缀

    private final int fontColor;

    public WheelLockAdapter(Context context, List<Integer> times, String suffix) {
        mContext = context;
        mHeight = (int) PhoneUtil.pixelToDp(context, mHeight);
        this.times = times;
        this.suffix = suffix;

        fontColor = context.getResources().getColor(R.color.font_black);
    }

    public void updateData(List<Integer> times, String suffix) {
        this.times = times;
        this.suffix = suffix;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return (null != times) ? times.size() : 0;
    }

    @Override
    public Integer getItem(int position) {
        return times.get(position);
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
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textView.setTextColor(fontColor);
        }

        if (null == textView) {
            textView = (TextView) convertView;
        }
        if (times != null && times.size() > 0) {
            int time = times.get(position);
            if (position < times.size()) {
                if (suffix == null)
                    textView.setText(time + "");
                else
                    textView.setText(time + suffix);
            }
            convertView.setTag(R.id.tag_wheel, time);
        }
        return convertView;
    }
}
