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
import com.orvibo.homemate.view.custom.wheelview.WheelBo;

import java.util.List;

public class WheelAdapter extends BaseAdapter {
    private Context mContext = null;
    private int mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
    private int mHeight = 50;
    private List<WheelBo> mContents;

    private final int fontColor;

    public WheelAdapter(Context context, List<WheelBo> contents) {
        mContext = context;
        mHeight = (int) PhoneUtil.pixelToDp(context, mHeight);
        mContents = contents;

        fontColor = context.getResources().getColor(R.color.font_black);
    }

    public void setData(List<WheelBo> contents) {
        mContents = contents;
        notifyDataSetChanged();
    }


    public String getString(int pos) {
        return mContents.get(pos).getName();
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
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            // textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            textView.setTextColor(fontColor);
        }

        if (null == textView) {
            textView = (TextView) convertView;
        }
        //TODO  java.lang.IndexOutOfBoundsException: Invalid index 0, size is 0
        /*at java.util.ArrayList.throwIndexOutOfBoundsException(ArrayList.java:251)
        at java.util.ArrayList.get(ArrayList.java:304)
        at com.orvibo.homemate.common.WheelAdapter.getView(WheelAdapter.java:76)
        at com.orvibo.homemate.view.custom.wheelview.TosAbsSpinner.onMeasure(TosAbsSpinner.java:217)
        at android.view.View.measure(View.java:15297)
        at android.widget.LinearLayout.measureHorizontal(LinearLayout.java:1020)
        at android.widget.LinearLayout.onMeasure(LinearLayout.java:576)
        at android.view.View.measure(View.java:15297)
        at android.widget.RelativeLayout.measureChildHorizontal(RelativeLayout.java:617)
        at android.widget.RelativeLayout.onMeasure(RelativeLayout.java:399)
        at android.view.View.measure(View.java:15297)
        at android.widget.LinearLayout.measureVertical(LinearLayout.java:833)
        at android.widget.LinearLayout.onMeasure(LinearLayout.java:574)
        at android.view.View.measure(View.java:15297)
        at android.widget.LinearLayout.measureVertical(LinearLayout.java:833)
        at android.widget.LinearLayout.onMeasure(LinearLayout.java:574)
        at android.view.View.measure(View.java:15297)
        at android.view.ViewGroup.measureChildWithMargins(ViewGroup.java:4950)
        at android.widget.FrameLayout.onMeasure(FrameLayout.java:310)
        at android.view.View.measure(View.java:15297)
        at android.view.ViewRootImpl.performMeasure(ViewRootImpl.java:1922)
        at android.view.ViewRootImpl.measureHierarchy(ViewRootImpl.java:1172)
        at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:1347)
        at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:1070)
        at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:4296)
        at android.view.Choreographer$CallbackRecord.run(Choreographer.java:725)
        at android.view.Choreographer.doCallbacks(Choreographer.java:555)
        at android.view.Choreographer.doFrame(Choreographer.java:525)
        at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:711)
        at android.os.Handler.handleCallback(Handler.java:615)
        at android.os.Handler.dispatchMessage(Handler.java:92)
        at android.Looper.loop(Looper.java:137)
        at android.app.ActivityThread.main(ActivityThread.java:4914)
        at java.lang.reflect.Method.invokeNative(Native Method)
        at java.lang.reflect.Method.invoke(Method.java:511)
        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:808)
        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:575)
        at dalvik.system.NativeStart.main(Native Method)*/
        if (mContents != null && mContents.size() > 0) {
            WheelBo wheelBo = mContents.get(position);
            if (position < mContents.size()) {
                textView.setText(wheelBo.getName());
            }
            convertView.setTag(R.id.tag_wheel, wheelBo);
        }
        return convertView;
    }
}
