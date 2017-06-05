package com.orvibo.homemate.smartscene.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.smartscene.adapter.ScenePicAdapter;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.NavigationCocoBar;

/**
 * 选择情景图片
 * Created by huangqiyao on 2015/4/27.
 */
public class SceneSelectPicActivity extends BaseActivity implements AdapterView.OnItemClickListener, NavigationCocoBar.OnLeftClickListener, NavigationCocoBar.OnRightClickListener {
    private static final String TAG = SceneSelectPicActivity.class.getSimpleName();
    private ScenePicAdapter mScenePicAdapter;
    private NavigationCocoBar navigationCocoBar;
    private int mSelectPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_select_pic);
        navigationCocoBar = (NavigationCocoBar) findViewById(R.id.navigationCocoBar);
        navigationCocoBar.setOnRightClickListener(this);
        mSelectPic = getIntent().getIntExtra("pic", Constant.INVALID_NUM);
        LogUtil.d(TAG, "onCreate()-pic:" + mSelectPic);
        GridView scenes_gv = (GridView) findViewById(R.id.gridView);
        scenes_gv.setOnItemClickListener(this);
        mScenePicAdapter = new ScenePicAdapter(mContext, mSelectPic);
        scenes_gv.setAdapter(mScenePicAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mSelectPic = position +3;
        mScenePicAdapter.selectPic(mSelectPic);
//        Intent intent = new Intent();
//        intent.putExtra("pic", pic);
//        setResult(RESULT_OK, intent);
//        leftTitleClick(null);
    }

    @Override
    public void onLeftClick(View v) {
        finish();
    }

    @Override
    public void onRightClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("pic", mSelectPic);
        setResult(RESULT_OK, intent);
        finish();
    }
}
