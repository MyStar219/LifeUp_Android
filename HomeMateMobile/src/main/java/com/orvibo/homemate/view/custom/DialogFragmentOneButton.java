package com.orvibo.homemate.view.custom;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.smartgateway.app.R;

/**
 * Created by Allen on 2015/5/29.
 */
public class DialogFragmentOneButton extends DialogFragment implements View.OnClickListener{
    private TextView titleTextView, contentTextView;
    private Button gotItButton;
    private String title, content, buttonText;
    private int textColor;
    private OnButtonClickListener listener;
    private DialogInterface.OnCancelListener onCancelListener;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_fragment_one_button , null);
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
        gotItButton = (Button) view.findViewById(R.id.gotItButton);
    }

    private void init() {
        if (!TextUtils.isEmpty(title)) {
            titleTextView.setText(title);
        }
        if (!TextUtils.isEmpty(content)) {
            contentTextView.setText(content);
            contentTextView.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(buttonText)) {
            gotItButton.setText(buttonText);
        }
        if (textColor != 0) {
            gotItButton.setTextColor(textColor);
        }
        gotItButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        dismiss();
        if (listener != null) {
            listener.onButtonClick(v);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener!= null) {
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

    public void setContent(String content) {
        this.content = content;
        if (contentTextView != null) {
            contentTextView.setText(content);
            contentTextView.setVisibility(View.VISIBLE);
        }
    }

    public void setButtonText(String text) {
        this.buttonText = text;
        if (gotItButton != null) {
            gotItButton.setText(buttonText);
        }
    }

    public void setButtonTextColor(int color) {
        this.textColor = color;
        if (gotItButton != null) {
            gotItButton.setTextColor(color);
        }
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnButtonClickListener {
        void onButtonClick(View view);
    }

}
