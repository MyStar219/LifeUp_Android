package com.orvibo.homemate.view.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.smartscene.adapter.SceneBindTipAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snown on 2015/5/29.
 */
public class SceneTipDialog extends DialogFragment implements View.OnClickListener {
    private TextView titleTextView;
    private ListView listView;
    private Button gotItButton;
    private OnButtonClickListener listener;
    private DialogInterface.OnCancelListener onCancelListener;
    private static final int MAX_VIEW_LENTH=12;

    public static SceneTipDialog newInstance(String title, List<CharSequence> tips) {
        SceneTipDialog dialog = new SceneTipDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putCharSequenceArrayList("tips", (ArrayList<CharSequence>) tips);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.scene_tip_dialog, null);
        Dialog dialog = new Dialog(getActivity(), R.style.MyDialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();


        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        titleTextView.setText(getArguments().getString("title"));
        listView = (ListView) view.findViewById(R.id.tip_list);
        List<CharSequence> tips = getArguments().getCharSequenceArrayList("tips");
        if (tips.size() > MAX_VIEW_LENTH)
            window.setLayout(displayMetrics.widthPixels * 4 / 5, displayMetrics.heightPixels * 3 / 5);
        else
            window.setLayout(displayMetrics.widthPixels * 4 / 5, WindowManager.LayoutParams.WRAP_CONTENT);
        SceneBindTipAdapter sceneBindTipAdapter = new SceneBindTipAdapter(tips);
        listView.setAdapter(sceneBindTipAdapter);
        gotItButton = (Button) view.findViewById(R.id.gotItButton);
        gotItButton.setOnClickListener(this);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        if (onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    public interface OnButtonClickListener {
        void onButtonClick(View view);
    }

}
