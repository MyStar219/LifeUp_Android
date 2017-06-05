package com.orvibo.homemate.common.appwidget;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.SceneDao;
import com.orvibo.homemate.sharedPreferences.UserCache;

import java.util.ArrayList;
import java.util.List;


public class WidgetSceneEditActivity extends BaseActivity {
    private ListView listview;
    private SceneDao sceneDao;

    private WidgetSettingAdapter adapter;

    private ImageView sceneEmptyImageView;
    private TextView  sceneEmptyTextView;

    private static int MAX_SELECT=5;
    private List<Scene> scenes;
    private List<WidgetItem> defaultScenes = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_set_scene);
        List<WidgetItem> sceneList = (List<WidgetItem>) getIntent().getSerializableExtra("scenes");
        if(sceneList!=null){
            defaultScenes.clear();
            defaultScenes.addAll(sceneList);
        }
        sceneDao = new SceneDao();

        String mainUid = UserCache.getCurrentMainUid(WidgetSceneEditActivity.this);

        listview = (ListView) findViewById(R.id.listview);
        scenes = sceneDao.selScenes(mainUid, false);

        sceneEmptyImageView = (ImageView) findViewById(R.id.deviceEmptyImageView);
        sceneEmptyTextView  = (TextView) findViewById(R.id.deviceEmptyTextView);


        adapter = new WidgetSettingAdapter(WidgetSceneEditActivity.this,defaultScenes, scenes);
        adapter.setMaxSelect(MAX_SELECT);
        listview.setAdapter(adapter);

        initEmptyView();
    }

    private void initEmptyView() {
        if  (scenes == null || scenes.size() == 0) {
            //如果帐号下没有情景
            listview.setVisibility(View.GONE);
            sceneEmptyTextView.setText(getResources().getString(R.string.no_widget_scene_tip));
            sceneEmptyImageView.setBackgroundResource(R.drawable.bg_no_device);
        }else{
            listview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void leftTitleClick(View v) {
        returnResult();
        super.leftTitleClick(v);
    }

    @Override
    public void onBackPressed() {
        returnResult();
        super.onBackPressed();
    }

    private void returnResult() {
        adapter.getSelected();
        Intent data = new Intent();
        data.putExtra("checkedScenes", adapter.getSelected());
        setResult(RESULT_OK, data);
    }
    }