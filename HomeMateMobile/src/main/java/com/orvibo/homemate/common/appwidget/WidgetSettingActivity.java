package com.orvibo.homemate.common.appwidget;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.common.appwidget.db.WidgetDao;
import com.orvibo.homemate.data.BindActionType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.bind.BaseSelectDeviceActionsActivity;
import com.orvibo.homemate.device.bind.SelectDeviceActivity;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.view.custom.SwipeItemMenuListView;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenu;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuCreator;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuItem;
import com.orvibo.homemate.view.custom.swipemenulistview.SwipeMenuListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by wuliquan on 2016/6/12.
 */
public class WidgetSettingActivity extends BaseSelectDeviceActionsActivity {
    private static int EDIT_SCENE_FLAG = 0x10;
    private static int BIND_DEVIE_SIZE =5;
    private SwipeMenuCreator creator;
    private SwipeItemMenuListView lvWidgetDevice;

    private TextView add_device;
    private TextView add_scene;
    private WidgetDao widgetDao;
    private WidgetDeviceAndSceneAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_set);
        widgetDao = new WidgetDao(WidgetSettingActivity.this);
        findViews();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void findViews() {
        lvWidgetDevice = (SwipeItemMenuListView) findViewById(R.id.lvWidgetDevice);
        add_device = (TextView)findViewById(R.id.add_device);
        add_device.setOnClickListener(this);
        add_scene=(TextView)findViewById(R.id.add_scene);
        add_scene.setOnClickListener(this);

    }

    private void init() {
        creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(getResources().getColor(R.color.red)));
                deleteItem.setWidth(getResources().getDimensionPixelSize(R.dimen.swipe_button_width));
                deleteItem.setTitle(getString(R.string.delete));
                deleteItem.setTitleSize((int) (getResources().getDimensionPixelSize(R.dimen.text_normal) / getResources().getDisplayMetrics().scaledDensity));
                deleteItem.setTitleColor(getResources().getColor(R.color.font_black));
                menu.addMenuItem(deleteItem);
            }
        };

        lvWidgetDevice.setMenuCreator(creator);
        lvWidgetDevice.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                if(adapter!=null){
                    String widgetId = adapter.getTableId(position);
                    widgetDao.deleteDataById(widgetId);
                }
                refreshList();
            }
        });
    }

    private void initEmptyLayout(){
       lvWidgetDevice.setVisibility(View.GONE);
    }
    @Override
    public void onClick(View v) {
        String userId = UserCache.getCurrentUserId(WidgetSettingActivity.this);
        switch (v.getId()){
            case R.id.add_device:
                Intent intent1 = new Intent(this,
                        SelectDeviceActivity.class);

                List<WidgetItem> deviceList = widgetDao.selItemByTyple(userId,"device");
                intent1.putExtra(IntentKey.BIND_ACTION_TYPE, BindActionType.SET_WIDGET);
                intent1.putExtra(IntentKey.BIND_SIZE,BIND_DEVIE_SIZE);
                intent1.putExtra(IntentKey.DEVICES, (Serializable) Widgets2Devices(deviceList));
                startActivityForResult(intent1, SELECT_DEVICE);
                break;
            case R.id.add_scene:
                Intent intent2 = new Intent(this,
                        WidgetSceneEditActivity.class);
                List<WidgetItem> sceneList = widgetDao.selItemByTyple(userId,"scene");
                intent2.putExtra("scenes",(Serializable) sceneList);
               startActivityForResult(intent2,EDIT_SCENE_FLAG);
                break;
        }
    }

    @Override
    protected void onSelectDevices(List<Device> devices) {
        if(devices!=null){
            widgetDao.deleteDataByType("device");
            String userId = UserCache.getCurrentUserId(WidgetSettingActivity.this);
            widgetDao.insertDevice(userId,devices);
            refreshList();
        }
    }

    /**
     * 备注：采用adapter的刷新机制不能刷新界面，故采用new adapter的方式来刷新
     */
    private void refreshList(){
        String userId = UserCache.getCurrentUserId(WidgetSettingActivity.this);
        EventBus.getDefault().post(new WidgetUpdateEvent(0));
        List<WidgetItem> deviceList = widgetDao.selItemByTyple(userId,"device");
        List<WidgetItem> sceneList = widgetDao.selItemByTyple(userId,"scene");
        lvWidgetDevice.setVisibility(View.VISIBLE);
         adapter= null;
         adapter = new WidgetDeviceAndSceneAdapter(WidgetSettingActivity.this, deviceList, sceneList);
         lvWidgetDevice.setAdapter(adapter);

        if(deviceList==null||deviceList.size()==0){
            add_device.setText(getResources().getString(R.string.add_widget_device));
        }else{
            add_device.setText(getResources().getString(R.string.edit_widget_device));
        }

        if(sceneList==null||sceneList.size()==0){
            add_scene.setText(getResources().getString(R.string.add_widget_scene));
        }
        else{
            add_scene.setText(getResources().getString(R.string.edit_widget_scene));
        }
        if(sceneList==null||sceneList.size()==0){
            if(deviceList==null||deviceList.size()==0){
                initEmptyLayout();
            }
        }
    }

    private List<Device> Widgets2Devices(List<WidgetItem> deviceList){
        ArrayList<Device> devices = new ArrayList<>();
        if(deviceList!=null) {
            for (WidgetItem widgetItem : deviceList) {
                Device device = new Device();
                device.setUid(widgetItem.getUid());
                device.setDeviceName(widgetItem.getWidgetName());
                device.setDeviceId(widgetItem.getDeviceId());
                device.setDeviceType(widgetItem.getDeviceType());
                devices.add(device);
            }
        }
        return devices;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if(requestCode==EDIT_SCENE_FLAG){
                ArrayList<Scene> scenes = null;
                Serializable serializable1 =
                        data.getSerializableExtra("checkedScenes");
                if (serializable1 != null) {
                    scenes = (ArrayList<Scene>)
                            serializable1;
                    if(scenes!=null) {
                        String userId = UserCache.getCurrentUserId(WidgetSettingActivity.this);
                        widgetDao.deleteDataByType("scene");
                        widgetDao.insertScene(userId,scenes);
                        refreshList();
                    }
                }

            }
        }
    }
}
