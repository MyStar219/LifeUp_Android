package com.orvibo.homemate.common;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.WheelViewFragment;
import com.orvibo.homemate.view.custom.wheelview.WheelBo;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Allen on 2015/4/17.
 */
public class WheelViewActivity extends BaseActivity {
    private static final String TAG = WheelViewActivity.class.getSimpleName();
    //    private TextView confirm_tv;
    private WheelViewFragment wheelViewFragment;
    private List<WheelBo> firstWheelBos;
    private List<WheelBo> secondWheelBos;
    public static final String FIRST_WHEEL_BOS = "first_wheel_bos";
    public static final String SECOND_WHEEL_BOS = "second_wheel_bos";
    private int oldFirstIndex;
    private int oldSecondIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel_view);
        Intent intent = getIntent();
        oldFirstIndex = intent.getIntExtra(IntentKey.WHEELVIEW_SELECTED_FIRST, 0);
        oldSecondIndex = intent.getIntExtra(IntentKey.WHEELVIEW_SELECTED_SECOND, 0);
        LogUtil.d(TAG, "onCreate()-oldFirstIndex:" + oldFirstIndex + ",oldSecondIndex:" + oldSecondIndex);
        findViews();
        init();

        wheelViewFragment.selectFirstIndex(oldFirstIndex);
        wheelViewFragment.selectSecondIndex(oldSecondIndex);
    }

    private void findViews() {
        FragmentManager fragmentManager = getFragmentManager();
        wheelViewFragment = (WheelViewFragment) fragmentManager.findFragmentById(R.id.wheelViewFragment);
        //confirm_tv = (TextView) findViewById(R.id.confirm_tv);
    }

    private void init() {
        Serializable serializable1 = getIntent().getSerializableExtra(FIRST_WHEEL_BOS);
        if (serializable1 != null) {
            firstWheelBos = (List<WheelBo>) serializable1;
            // wheelViewFragment.setFirstWheelText(firstWheelBos);
        }
        Serializable serializable2 = getIntent().getSerializableExtra(SECOND_WHEEL_BOS);
        if (serializable2 != null) {
            secondWheelBos = (List<WheelBo>) serializable2;
            // wheelViewFragment.setSecondWheelText(secondWheelBos);
        }
        wheelViewFragment.setWheelText(firstWheelBos, secondWheelBos);
        setTitle(getString(R.string.bind_select_delay_time_title));
        // confirm_tv.setOnClickListener(this);

        LogUtil.d(TAG, "init()-firstWheelBos:" + firstWheelBos + ",secondWheelBos:" + secondWheelBos);
    }

    public void save(View v) {
        returnResult();
        leftTitleClick(v);
    }

    public void leftTitleClick(View v) {
        super.leftTitleClick(v);
    }

    public void cancel(View v) {
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        int curFirstIndex = wheelViewFragment.getFirstWheelSelectIndex();
        int curSecondIndex = wheelViewFragment.getSecondWheelSelectIndex();
        if (curFirstIndex != oldFirstIndex || curSecondIndex != oldSecondIndex) {
            new ConfirmAndCancelPopup() {
                @Override
                public void confirm() {
                    super.confirm();
                    dismiss();
                    returnResult();
                    leftTitleClick(null);
                }

                @Override
                public void cancel() {
                    super.cancel();
                    leftTitleClick(null);
                }
            }.showPopup(mContext, R.string.save_content, R.string.save, R.string.not_save);
        } else {
            super.onBackPressed();
        }
    }

    private void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(IntentKey.WHEELVIEW_SELECTED_FIRST, wheelViewFragment.getFirstWheelSelectIndex());
        intent.putExtra(IntentKey.WHEELVIEW_SELECTED_SECOND, wheelViewFragment.getSecondWheelSelectIndex());
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.confirm_tv: {
                returnResult();
                break;
            }
        }
    }
}
