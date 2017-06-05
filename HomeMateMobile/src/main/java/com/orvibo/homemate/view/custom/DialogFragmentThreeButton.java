//package com.orvibo.homemate.view.custom;
//
//import android.app.Dialog;
//import android.app.DialogFragment;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.text.TextUtils;
//import android.util.DisplayMetrics;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.smartgateway.app.R;
//
///**
// * Created by Smagret on 2016/09/01.
// */
//public class DialogFragmentThreeButton extends DialogFragment implements View.OnClickListener {
//    private TextView titleTextView, contentTextView;
//    private int upTextColor, middleTextColor, downTextColor;
//    private Button upButton, middleButton, downButton;
//    private String title, content, upButtonText, middleButtonText, downButtonText, editText;
//    private OnThreeButtonClickListener listener;
//    private DialogInterface.OnCancelListener onCancelListener;
//    private EditTextWithCompound editTextWithCompound;
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        View view = View.inflate(getActivity(), R.layout.dialog_fragment_three_button, null);
//        Dialog dialog = new Dialog(getActivity(), R.style.MyDialog);
//        dialog.setContentView(view);
//        dialog.setCanceledOnTouchOutside(false);
//        Window window = dialog.getWindow();
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        window.setLayout(displayMetrics.widthPixels * 4 / 5, WindowManager.LayoutParams.WRAP_CONTENT);
//        findViews(view);
//        return dialog;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        init();
//    }
//
//    private void findViews(View view) {
//        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
//        contentTextView = (TextView) view.findViewById(R.id.contentTextView);
//        upButton = (Button) view.findViewById(R.id.upButton);
//        middleButton = (Button) view.findViewById(R.id.middleButton);
//        downButton = (Button) view.findViewById(R.id.downButton);
//        editTextWithCompound = (EditTextWithCompound) view.findViewById(R.id.editText);
//        editTextWithCompound.setMaxLength(16);
//    }
//
//    private void init() {
//        if (!TextUtils.isEmpty(title)) {
//            titleTextView.setText(title);
//            titleTextView.setVisibility(View.VISIBLE);
//        }
//        if (!TextUtils.isEmpty(content)) {
//            contentTextView.setText(content);
//            contentTextView.setVisibility(View.VISIBLE);
//        }
//        if (upTextColor != 0) {
//            upButton.setTextColor(upTextColor);
//        }
//
//        if (!TextUtils.isEmpty(upButtonText)) {
//            upButton.setText(upButtonText);
//            upButton.setVisibility(View.VISIBLE);
//        }
//        if (middleTextColor != 0) {
//            middleButton.setTextColor(middleTextColor);
//        }
//
//        if (!TextUtils.isEmpty(middleButtonText)) {
//            middleButton.setText(middleButtonText);
//            middleButton.setVisibility(View.VISIBLE);
//        }
//        if (downTextColor != 0) {
//            downButton.setTextColor(downTextColor);
//        }
//
//        if (!TextUtils.isEmpty(downButtonText)) {
//            downButton.setText(downButtonText);
//            downButton.setVisibility(View.VISIBLE);
//        }
//
//        if (editText != null) {
//            editTextWithCompound.setVisibility(View.VISIBLE);
//        }
//
//        upButton.setOnClickListener(this);
//        middleButton.setOnClickListener(this);
//        downButton.setOnClickListener(this);
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.upButton: {
//                dismiss();
//                if (listener != null) {
//                    listener.onUpButtonClick(v);
//                }
//                break;
//            }
//
//            case R.id.middleButton: {
//                dismiss();
//                if (listener != null) {
//                    listener.onMiddleButtonClick(v);
//                }
//                break;
//            }
//            case R.id.downButton: {
//                dismiss();
//                if (listener != null) {
//                    listener.onDownButtonClick(v);
//                }
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void onCancel(DialogInterface dialog) {
//        super.onCancel(dialog);
//        if (onCancelListener != null) {
//            onCancelListener.onCancel(dialog);
//        }
//    }
//
//    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
//        this.onCancelListener = onCancelListener;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//        if (titleTextView != null) {
//            titleTextView.setText(title);
//        }
//    }
//
//    public void hideContent() {
//        if (contentTextView != null) {
//            contentTextView.setVisibility(View.GONE);
//        }
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//        if (contentTextView != null) {
//            contentTextView.setText(content);
//            contentTextView.setVisibility(View.VISIBLE);
//        }
//    }
//
//    public void setEditText(String editText) {
//        this.editText = editText;
//        if (editTextWithCompound != null) {
//            contentTextView.setText(editText);
//            editTextWithCompound.setVisibility(View.VISIBLE);
//        }
//    }
//
//    public String getEditTextWithCompound() {
//        return editTextWithCompound.getText().toString();
//    }
//
//    public void setUpTextColor(int leftTextColor) {
//        this.upTextColor = leftTextColor;
//        if (upButton != null) {
//            upButton.setTextColor(leftTextColor);
//        }
//    }
//
//    public void setUpButtonText(String upButtonText) {
//        this.upButtonText = upButtonText;
//        if (upButton != null) {
//            upButton.setText(upButtonText);
//        }
//    }
//
//    public void setMiddleTextColor(int upTextColor) {
//        this.upTextColor = upTextColor;
//        if (middleButton != null) {
//            middleButton.setTextColor(upTextColor);
//        }
//    }
//
//    public void setMiddleButtonText(String middleButtonText) {
//        this.middleButtonText = middleButtonText;
//        if (middleButton != null) {
//            middleButton.setText(middleButtonText);
//        }
//    }
//
//    /**
//     * @param downTextColor 不能直接使用r.color.xxx，要用getResource().getColor(resId);
//     */
//    public void setDownTextColor(int downTextColor) {
//        this.downTextColor = downTextColor;
//        if (downButton != null) {
//            downButton.setTextColor(downTextColor);
//        }
//    }
//
//    public void setDownButtonText(String downButtonText) {
//        this.downButtonText = downButtonText;
//        if (downButton != null) {
//            downButton.setText(downButtonText);
//        }
//    }
//
//    public void setOnThreeButtonClickListener(OnThreeButtonClickListener listener) {
//        this.listener = listener;
//    }
//
//    public interface OnThreeButtonClickListener {
//        void onUpButtonClick(View view);
//
//        void onMiddleButtonClick(View view);
//
//        void onDownButtonClick(View view);
//    }
//}
