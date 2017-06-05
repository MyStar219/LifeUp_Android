package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.DeviceType;
import com.orvibo.homemate.util.StringUtil;

/**
 * 带有确定和取消按钮的popup
 * Created by huangqiyao on 2015/4/9.
 */
public class ConfirmAndCancelPopup extends CommonPopup implements View.OnClickListener {
    private Context mContext;
    private TextView mCancel_tv;

    public final void showPopup(Context context, int content, int yes, int no) {
        showPopup(context, context.getString(content), context.getString(yes), no == 0 || no == Constant.INVALID_NUM ? null : context.getString(no));
    }

    /**
     * 弹框同一调用这个类，默认显示两个按钮，如果参数no为空，则显示一个按钮
     *
     * @param context
     * @param content
     * @param yes
     * @param no
     */
    public final void showPopup(Context context, String content, String yes, String no) {
        mContext = context;
        showPopup(context, content, null, yes, no);
    }

    public final void showPopup(Context context, String title, String content, String yes, String no) {
        mContext = context;
        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_confirm_cancel, null);
        TextView titleTextView = (TextView) contentView.findViewById(R.id.titleTextView);
        TextView contentTextView = (TextView) contentView.findViewById(R.id.contentTextView);

        if (title != null) {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
        if (content != null) {
            contentTextView.setVisibility(View.VISIBLE);
            contentTextView.setText(content);
        } else {
            contentTextView.setVisibility(View.GONE);
        }

        TextView rightTextView = (TextView) contentView.findViewById(R.id.rightTextView);
        mCancel_tv = (TextView) contentView.findViewById(R.id.leftTextView);
        if (StringUtil.isEmpty(no)) {
            mCancel_tv.setVisibility(View.GONE);
            rightTextView.setVisibility(View.VISIBLE);

            rightTextView.setText(yes);
            rightTextView.setBackgroundResource(R.drawable.dialog_down);
            rightTextView.setOnClickListener(this);
        } else if (StringUtil.isEmpty(yes)) {
            rightTextView.setVisibility(View.GONE);
            mCancel_tv.setVisibility(View.VISIBLE);

            mCancel_tv.setText(no);
            mCancel_tv.setBackgroundResource(R.drawable.dialog_down);
            mCancel_tv.setOnClickListener(this);
        } else {
            mCancel_tv.setVisibility(View.VISIBLE);
            rightTextView.setVisibility(View.VISIBLE);

            rightTextView.setText(yes);
            mCancel_tv.setText(no);
            rightTextView.setOnClickListener(this);
            mCancel_tv.setOnClickListener(this);
        }

        show(contentView, true);
    }

    public final void showPopupWithImage(Context context, String title, String content, String yes, String no) {
        mContext = context;
        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_confirm_cancel, null);
        TextView titleTextView = (TextView) contentView.findViewById(R.id.titleTextView);
        TextView contentTextView = (TextView) contentView.findViewById(R.id.contentTextView);

        if (title != null) {
            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(title);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
//        if (content != null) {
//            contentTextView.setVisibility(View.VISIBLE);
//            contentTextView.setText(content);
//        } else {
//            contentTextView.setVisibility(View.GONE);
//        }
        contentTextView.setVisibility(View.VISIBLE);
        final Html.ImageGetter imageGetter = new Html.ImageGetter() {

            public Drawable getDrawable(String source) {
                Drawable drawable = null;
                int rId = Integer.parseInt(source);
                drawable = mContext.getResources().getDrawable(rId);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            }
        };
        final String sText1 = mContext.getString(R.string.device_set_remote_bind_suiyitie_tips1);
        final String sText2 = "<img src=\"" + R.drawable.bg_icon_add + "\" />";
        final String sText3 = mContext.getString(R.string.device_set_remote_bind_suiyitie_tips2);
        final String sText4 = "<img src=\"" + R.drawable.bg_icon_reduce + "\" />";
        final String sText5 = mContext.getString(R.string.device_set_remote_bind_suiyitie_tips3);
        contentTextView.setText(Html.fromHtml(sText1 + sText2 + sText3 + sText4 + sText5, imageGetter, null));

        TextView rightTextView = (TextView) contentView.findViewById(R.id.rightTextView);
        TextView leftTextView = (TextView) contentView.findViewById(R.id.leftTextView);
        if (StringUtil.isEmpty(no)) {
            leftTextView.setVisibility(View.GONE);
            rightTextView.setVisibility(View.VISIBLE);

            rightTextView.setText(yes);
            rightTextView.setBackgroundResource(R.drawable.dialog_down);
            rightTextView.setOnClickListener(this);
        } else if (StringUtil.isEmpty(yes)) {
            rightTextView.setVisibility(View.GONE);
            leftTextView.setVisibility(View.VISIBLE);

            leftTextView.setText(no);
            leftTextView.setBackgroundResource(R.drawable.dialog_down);
            leftTextView.setOnClickListener(this);
        } else {
            leftTextView.setVisibility(View.VISIBLE);
            rightTextView.setVisibility(View.VISIBLE);

            rightTextView.setText(yes);
            leftTextView.setText(no);
            rightTextView.setOnClickListener(this);
            leftTextView.setOnClickListener(this);
        }

        show(contentView, true);
    }

    /**
     * 显示绑定主机失败后提示重置的界面
     *
     * @param context
     * @param content
     */
    public final void showVicenterResetPopup(Context context, String content, int deviceType) {
        if (content == null) {
            return;
        }
        mContext = context;
        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_bind_reset, null);
        TextView contentTextView = (TextView) contentView.findViewById(R.id.contentTextView);
        contentTextView.setVisibility(View.VISIBLE);
        contentTextView.setText(content);
        TextView rightTextView = (TextView) contentView.findViewById(R.id.rightTextView);
        TextView leftTextView = (TextView) contentView.findViewById(R.id.leftTextView);
        rightTextView.setOnClickListener(this);
        leftTextView.setOnClickListener(this);

        ImageView host300_iv = (ImageView) contentView.findViewById(R.id.host300_iv);
        if (deviceType == DeviceType.VICENTER) {
            host300_iv.setImageResource(R.drawable.bg_zigbee_vicenter_tips);
        } else {
            host300_iv.setImageResource(R.drawable.bg_zigbee_minihub_tips);
        }

        show(contentView, true);
    }

    /**
     * 点击确定按钮
     */
    public void confirm() {

    }

    /**
     * 点击取消按钮
     */
    public void cancel() {

    }

    @Override
    public final void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.leftTextView) {
            dismiss();
            cancel();
        } else if (vId == R.id.rightTextView) {
            confirm();
        }
    }

    /**
     * @param color 16进制颜色
     */
    public void setCancelTextColor(int color) {
        if (mCancel_tv != null) {
            mCancel_tv.setTextColor(color);
        }
    }
}
