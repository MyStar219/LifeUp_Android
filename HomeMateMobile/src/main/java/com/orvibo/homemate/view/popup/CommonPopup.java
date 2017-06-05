package com.orvibo.homemate.view.popup;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.smartgateway.app.R;

/**
 * Created by huangqiyao on 2015/4/9.
 */
public class CommonPopup {
    protected Context mCommonPopupContext;
    protected PopupWindow mPopup;
    private OnPopupDismissListener onPopupDismissListener;

    /**
     * @param view
     * @param focusableOrTouchable true 接收焦点和触摸事件
     */
    protected void show(View view, boolean focusableOrTouchable) {
        dismiss();
        try {
            mPopup = new PopupWindow(view,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            mPopup.setFocusable(focusableOrTouchable);
            mPopup.setTouchable(focusableOrTouchable);
            mPopup.showAtLocation(view, Gravity.CENTER, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                onPopupDismiss();
                if(onPopupDismissListener!=null){
                    onPopupDismissListener.onPopupDismiss();
                }
            }
        });
    }

    protected void show(Context context, View view, boolean focusableOrTouchable) {
        dismiss();
        mPopup = new PopupWindow(view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mPopup.setFocusable(focusableOrTouchable);
        mPopup.setTouchable(focusableOrTouchable);
        mPopup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tran));
        mPopup.showAtLocation(view, Gravity.CENTER, 0, 0);

        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                onPopupDismiss();
            }
        });
    }

    protected void show(Context context, View view, boolean focusableOrTouchable, boolean outsideTouchable) {
        dismiss();
        mPopup = new PopupWindow(view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mPopup.setFocusable(focusableOrTouchable);
        mPopup.setTouchable(focusableOrTouchable);
        mPopup.setOutsideTouchable(outsideTouchable);
        mPopup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tran));
        mPopup.showAtLocation(view, Gravity.CENTER, 0, 0);

        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                onPopupDismiss();
            }
        });
    }

    protected void show(Context context, View view, View topView, boolean focusableOrTouchable, boolean outsideTouchable) {
        dismiss();
        mPopup = new PopupWindow(view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mPopup.setFocusable(focusableOrTouchable);
        mPopup.setTouchable(focusableOrTouchable);
        mPopup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tran));
        mPopup.showAsDropDown(topView);
        mPopup.setOutsideTouchable(outsideTouchable);
        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                onPopupDismiss();
            }
        });
    }

    protected void show(Context context, View view, View topView, boolean focusableOrTouchable) {
        dismiss();
        mPopup = new PopupWindow(view,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mPopup.setFocusable(false);
        mPopup.setTouchable(focusableOrTouchable);
        mPopup.setOutsideTouchable(false);
//        mPopup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tran));
        mPopup.showAsDropDown(topView);
        mPopup.setAnimationStyle(R.anim.popup_out);
        mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                onPopupDismiss();
            }
        });
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismiss();
        }
    };

    public void dismissPopupDelay() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(0, 200);
    }

    protected void onPopupDismiss() {

    }

    public void dismiss() {
        try {
            if (isShowing()) {
                mPopup.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isShowing() {
        return mPopup != null && mPopup.isShowing();
    }

    public interface OnPopupDismissListener{
        public void onPopupDismiss();
    }

    public void setOnPopupDismissListener(OnPopupDismissListener onPopupDismissListener) {
        this.onPopupDismissListener = onPopupDismissListener;
    }
}
