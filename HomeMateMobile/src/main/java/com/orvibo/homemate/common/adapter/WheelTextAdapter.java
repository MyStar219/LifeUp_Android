package com.orvibo.homemate.common.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.view.custom.wheelview.TosGallery;


public class WheelTextAdapter extends BaseAdapter {

	public static final int TYPE_HOUR = 0;
	public static final int TYPE_MINUTE = 1;
	public static final int TYPE_SECOND = 2;
	public static final int TYPE_CDHOUR = 3;
    public static final int TYPE_FORMAT = 4;
    public static final int TYPE_FMHOUR = 5;

	String[] mData;
	int mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
	int mHeight = 50;
	Context mContext = null;
	private int type;

	public WheelTextAdapter(Context context, int type) {
		mContext = context;
		this.type = type;
		mHeight = (int) pixelToDp(context, mHeight);
		if (type == TYPE_HOUR) {
			mData = new String[24];
		} else if (type == TYPE_MINUTE) {
			mData = new String[60];
		} else if (type == TYPE_CDHOUR) {
			mData = new String[17];
		} else if (type == TYPE_SECOND){
			mData = new String[60];
		} else if (type == TYPE_FMHOUR) {
            mData = new String[12];
        } else {
            mData = new String[2];
        }
        if (type == TYPE_FORMAT) {
            mData[0] = mContext.getString(R.string.timing_morning);
            mData[1] = mContext.getString(R.string.timing_afternoon);
        } else {
            for (int x = 0; x < mData.length; x++) {
                if (x < 10) {
                    if (x == 0 && type == TYPE_FMHOUR) {
                        mData[0] = "" + 12;
                    } else {
                        mData[x] = "0" + x;
                    }
                }
                else
                    mData[x] = "" + x;
            }
        }
	}

	public String getString(int pos) {
		return mData[pos];
	}

	public void setItemSize(int width, int height) {
		mWidth = width;
		mHeight = (int) pixelToDp(mContext, height);
	}

	@Override
	public int getCount() {
		return (null != mData) ? mData.length : 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
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
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			textView.setTextColor(Color.BLACK);
		}

		if (null == textView) {
			textView = (TextView) convertView;
		}

		if (position < mData.length)
			textView.setText(mData[position]);
		return convertView;
	}

    private float pixelToDp(Context context, float val) {
        float density = context.getResources().getDisplayMetrics().density;
        return val * density;
    }
}
