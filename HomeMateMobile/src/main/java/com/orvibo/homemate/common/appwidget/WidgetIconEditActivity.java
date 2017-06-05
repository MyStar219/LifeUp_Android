package com.orvibo.homemate.common.appwidget;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.ImageName;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.common.appwidget.db.WidgetDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliquan on 2016/6/12.
 * 主要用来设置桌面情景模式图标
 */
public class WidgetIconEditActivity extends BaseActivity implements GridView.OnItemClickListener{
    private List<ImageName> imageNameLists = new ArrayList<ImageName>();
    private WidgetIconEditAdapter adapter;
    private WidgetDao widgetDao;
    private String widgetId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_icon_edit);

        int srcId = getIntent().getIntExtra("srcId",0);
        widgetDao = new WidgetDao(WidgetIconEditActivity.this);
        initImageNameLists();
        GridView gridView = (GridView) findViewById(R.id.gridview);
        adapter = new WidgetIconEditAdapter(WidgetIconEditActivity.this,imageNameLists,srcId);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }
    private void initImageNameLists() {

        ImageName imageName0 = new ImageName(0, 0, R.string.widget_name_default);
        ImageName imageName1 = new ImageName(1, 1, R.string.widget_name_go_home);
        ImageName imageName2 = new ImageName(2, 2, R.string.widget_name_leave_home);
        ImageName imageName3 = new ImageName(3, 3, R.string.widget_name_eatting);
        ImageName imageName4 = new ImageName(4, 4, R.string.widget_name_party);
        ImageName imageName5 = new ImageName(5, 5, R.string.widget_name_get_up);
        ImageName imageName6 = new ImageName(6, 6, R.string.widget_name_sleep);
        ImageName imageName7 = new ImageName(7, 7, R.string.widget_name_video);

        imageNameLists.add(imageName0);
        imageNameLists.add(imageName1);
        imageNameLists.add(imageName2);
        imageNameLists.add(imageName3);
        imageNameLists.add(imageName4);
        imageNameLists.add(imageName5);
        imageNameLists.add(imageName6);
        imageNameLists.add(imageName7);




    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.select(position);
        widgetDao.updateIcon((position)+"",getIntent().getStringExtra("widgetId"));
    }
}
