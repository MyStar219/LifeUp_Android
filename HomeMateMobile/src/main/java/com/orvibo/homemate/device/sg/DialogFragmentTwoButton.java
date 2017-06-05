package com.orvibo.homemate.device.sg;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.orvibo.homemate.view.custom.EditTextWithCompound;
import com.smartgateway.app.R;

/**
 * Created by Allen on 2015/6/11.
 */
public class DialogFragmentTwoButton extends DialogFragment implements View.OnClickListener {
    private TextView titleTextView, contentTextView;
    private int leftTextColor, rightTextColor;
    private Button leftButton, rightButton;
    private String title, content, leftButtonText, rightButtonText, editText;
    private OnTwoButtonClickListener listener;
    private DialogInterface.OnCancelListener onCancelListener;
    private EditTextWithCompound editTextWithCompound;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_fragment_two_button, null);
        Dialog dialog = new Dialog(getActivity(), R.style.MyDialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        window.setLayout(displayMetrics.widthPixels * 4 / 5, WindowManager.LayoutParams.WRAP_CONTENT);
        findViews(view);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void findViews(View view) {
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        contentTextView = (TextView) view.findViewById(R.id.contentTextView);
        leftButton = (Button) view.findViewById(R.id.leftButton);
        rightButton = (Button) view.findViewById(R.id.rightButton);
        editTextWithCompound = (EditTextWithCompound) view.findViewById(R.id.editText);
        editTextWithCompound.setMaxLength(16);
    }

    private void init() {
        if (!TextUtils.isEmpty(title)) {
            titleTextView.setText(title);
            titleTextView.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(content)) {
            contentTextView.setText(content);
            contentTextView.setVisibility(View.VISIBLE);
        }
        if (leftTextColor != 0) {
            leftButton.setTextColor(leftTextColor);
        }
        if (!TextUtils.isEmpty(leftButtonText)) {
            leftButton.setText(leftButtonText);
            leftButton.setVisibility(View.VISIBLE);
        } else {
            rightButton.setBackgroundResource(R.drawable.dialog_down);
        }
        if (rightTextColor != 0) {
            rightButton.setTextColor(rightTextColor);
        }
        if (!TextUtils.isEmpty(rightButtonText)) {
            rightButton.setText(rightButtonText);
        }
        if (editText != null) {
            editTextWithCompound.setVisibility(View.VISIBLE);
        }

        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftButton: {
                dismiss();
                if (listener != null) {
                    listener.onLeftButtonClick(v);
                }
                break;
            }
            case R.id.rightButton: {
                dismiss();
                if (listener != null) {
                    listener.onRightButtonClick(v);
                }
                break;
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public void setTitle(String title) {
        this.title = title;
        if (titleTextView != null) {
            titleTextView.setText(title);
        }
    }

    public void hideContent() {
        if (contentTextView != null) {
            contentTextView.setVisibility(View.GONE);
        }
    }

    public void setContent(String content) {
        this.content = content;
        if (contentTextView != null) {
            contentTextView.setText(content);
            contentTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setEditText(String editText) {
        this.editText = editText;
        if (editTextWithCompound != null) {
            contentTextView.setText(editText);
            editTextWithCompound.setVisibility(View.VISIBLE);
        }
    }

    public String getEditTextWithCompound() {
        return editTextWithCompound.getText().toString();
    }

    public void setLeftTextColor(int leftTextColor) {
        this.leftTextColor = leftTextColor;
        if (leftButton != null) {
            leftButton.setTextColor(leftTextColor);
        }
    }

    public void setLeftButtonText(String leftButtonText) {
        this.leftButtonText = leftButtonText;
        if (leftButton != null) {
            leftButton.setText(leftButtonText);
        }
    }

    /**
     * @param rightTextColor 不能直接使用r.color.xxx，要用getResource().getColor(resId);
     */
    public void setRightTextColor(int rightTextColor) {
        this.rightTextColor = rightTextColor;
        if (rightButton != null) {
            rightButton.setTextColor(rightTextColor);
        }
    }

    public void setRightButtonText(String rightButtonText) {
        this.rightButtonText = rightButtonText;
        if (rightButton != null) {
            rightButton.setText(rightButtonText);
        }
    }

    public void setOnTwoButtonClickListener(OnTwoButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnTwoButtonClickListener {
        void onLeftButtonClick(View view);

        void onRightButtonClick(View view);
    }

}
