package com.orvibo.homemate.common;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartgateway.app.R;
import com.smartgateway.app.Utils.DialogUtil;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * SmartCommunityFragment
 * Created by MDev on 12/14/16.
 */

public class FacialRecognitionFragment extends BaseAbstractFragment {

    private AlertDialog dialog;

    @Override
    protected String getTitle(Resources res) {
        return getString(R.string.facila_recognition);
    }

    @Override
    protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
        return createToolbarFrom(R.layout.toolbar_small);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facial_recognition, null, true);

        view.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                dialog = DialogUtil.showErrorAlert(getContext(),
                        getString(R.string.coming_soon_facial_recognition), null);
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        cancelDialog();
        super.onDestroyView();
    }

    // cancel to prevent memory leak
    public void cancelDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}