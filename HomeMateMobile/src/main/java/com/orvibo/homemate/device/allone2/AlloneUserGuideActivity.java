package com.orvibo.homemate.device.allone2;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.view.custom.NavigationGreenBar;

/**
 * Created by baoqi on 2016/4/5.
 * update by snown on 2016/5/20
 * 使用教程和反馈问题通用一个界面
 */
public class AlloneUserGuideActivity extends BaseActivity {
    private android.widget.LinearLayout guideView;
    private android.widget.LinearLayout feedbackView;
    public static final String VIEW_TYPE = "view_type";
    private int viewType;
    private com.orvibo.homemate.view.custom.NavigationGreenBar title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allone_userguide);

        viewType = getIntent().getIntExtra(VIEW_TYPE, 0);
        this.feedbackView = (LinearLayout) findViewById(R.id.feedbackView);
        this.guideView = (LinearLayout) findViewById(R.id.guideView);
        this.title = (NavigationGreenBar) findViewById(R.id.title);
        /**
         * 类型为1时，问题反馈view出现
         */
        if (viewType == 1) {
            feedbackView.setVisibility(View.VISIBLE);
            guideView.setVisibility(View.GONE);
            title.setText(getString(R.string.allone_feedback));
        }
    }
}
