package com.orvibo.homemate.device.sg;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.orvibo.homemate.util.MyLogger;
import com.smartgateway.app.R;

/**
 * Created by Allen on 2015/6/9.
 */
public class ProgressDialogFragment extends DialogFragment implements View.OnClickListener {
    private TextView contentTextView;

    private String content;
    private OnCancelClickListener listener;
    private boolean isContentVisible = true;
    private AnimationDrawable animationDrawable;
    //    private AnimationDrawable animationDrawable2;
    private AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
    private AlphaAnimation alphaAnimation2 = new AlphaAnimation(1, 0);
    private AlphaAnimation alphaAnimation3 = new AlphaAnimation(1, 0);
    private ImageView animImageView;
//    private ImageView pointImageView;

    public static ProgressDialogFragment newInstance(boolean isCanBack) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isCanBack", isCanBack);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ProgressDialogFragment newInstance() {
        return newInstance(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.progress_dialog_fragment, null);
        Dialog dialog = new Dialog(getActivity(), R.style.MyProgressDialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        dialog.getWindow().setLayout(dm.widthPixels, dialog.getWindow().getAttributes().height);

        //弹出框返回按键不消失逻辑
        if (null != getArguments()) {
            boolean isCanBack = getArguments().getBoolean("isCanBack", true);
            MyLogger.jLog().d(isCanBack);
            if (!isCanBack) {
                dialog.setCancelable(false);
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK)
                            return true;
                        return false;
                    }
                });
            }
        }
        findViews(view);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (listener != null) {
            listener.onCancelClick(null);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void findViews(View view) {

        try {
            contentTextView = (TextView) view.findViewById(R.id.contentTextView);
            animImageView = (ImageView) view.findViewById(R.id.animImageView);
//        pointImageView = (ImageView) view.findViewById(R.id.pointImageView);
            animationDrawable = (AnimationDrawable) animImageView.getDrawable();
//        animationDrawable2 = (AnimationDrawable) pointImageView.getDrawable();
            animationDrawable.start();
//        animationDrawable2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        if (isAdded() && !isDetached() && contentTextView != null) {
            if (!TextUtils.isEmpty(content)) {
                contentTextView.setText(content);
            }
            if (!isContentVisible) {
                contentTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        //dismiss();
        if (listener != null) {
            listener.onCancelClick(v);
        }
    }

    public void setContent(String content) {
        this.content = content;
        if (contentTextView != null) {
            contentTextView.setText(content);
        }
    }

//    public void setContentGone() {
//        isContentVisible = false;
//    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.listener = listener;
    }

    public interface OnCancelClickListener {
        /**
         * @param view 可能为null
         */
        void onCancelClick(View view);
    }
}
