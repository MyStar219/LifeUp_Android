package com.orvibo.homemate.room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.room.adapter.RoomTypeAdapter;
import com.orvibo.homemate.util.FloorAndRoomUtil;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.view.custom.NavigationGreenBar;


/**
 * @author Smagret
 * @data 2015/12/14
 */
public class SelectRoomTypeActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = SelectRoomTypeActivity.class
            .getSimpleName();

    private GridView           roomTypeGridView;
    private RoomTypeAdapter    roomTypeAdapter;
    private NavigationGreenBar navigationBar;
    private int                selectRoomType;
    private String             floorId;
    private String             roomId;
    private String             srcActivity;
    private String             roomName;
    private final int SELECT_ROOM_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_room_type_activity);
        init();
    }

    private void init() {
        initView();
        initListener();
        initData();
    }

    private void initView() {
        roomTypeGridView = (GridView) findViewById(R.id.roomTypeGridView);
        navigationBar = (NavigationGreenBar) findViewById(R.id.navigationBar);
        navigationBar.setText(getResources().getString(R.string.add_new_room));
    }

    private void initListener() {
        roomTypeGridView.setOnItemClickListener(this);
    }

    private void initData() {
        selectRoomType = getIntent().getIntExtra("roomType", 0);
        srcActivity = getIntent().getStringExtra("srcActivity");
        floorId = getIntent().getStringExtra("floorId");
        roomTypeAdapter = new RoomTypeAdapter(mContext, selectRoomType);
        roomTypeGridView.setAdapter(roomTypeAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectRoomType = (int) view.getTag(R.id.tag_roomType);
        roomTypeAdapter.selectRoomType(selectRoomType);
        nextActivity();
    }

    @Override
    public void leftTitleClick(View v) {
        backActivity();
    }

    @Override
    public void onBackPressed() {
        backActivity();
    }

    private void nextActivity() {
        if (!StringUtil.isEmpty(srcActivity) && srcActivity.equals(ModifyRoomActivity.class.getSimpleName())) {
            Intent intent = new Intent();
            intent.putExtra("roomType", selectRoomType);
            setResult(RESULT_OK, intent);
            finish();
        } else if (!StringUtil.isEmpty(srcActivity) && srcActivity.equals(SetFloorAndRoomActivity.class.getSimpleName())) {
            Intent intent = new Intent(mContext, AddNewRoomActivity.class);
            intent.putExtra("roomType", selectRoomType);
            intent.putExtra("floorId", floorId);
            intent.putExtra("srcActivity", srcActivity);
            startActivityForResult(intent, SELECT_ROOM_TYPE);

        }else if (!StringUtil.isEmpty(srcActivity) && srcActivity.equals(AddNewRoomActivity.class.getSimpleName())) {
            Intent intent = new Intent(mContext, AddNewRoomActivity.class);
            intent.putExtra("roomType", selectRoomType);
            intent.putExtra("floorId", floorId);
            intent.putExtra("srcActivity", srcActivity);
            intent.putExtra("roomName", getResources().getString(FloorAndRoomUtil.getRoomNameByRoomType(selectRoomType)));
            startActivityForResult(intent, SELECT_ROOM_TYPE);

        }
    }

    private void backActivity() {
        if (!StringUtil.isEmpty(srcActivity) && srcActivity.equals(ModifyRoomActivity.class.getSimpleName())) {
            Intent intent = new Intent();
            intent.putExtra("roomType", selectRoomType);
            setResult(RESULT_OK, intent);
            finish();
        } else if (!StringUtil.isEmpty(srcActivity) && srcActivity.equals(AddNewRoomActivity.class.getSimpleName())) {
            Intent intent = new Intent();
            intent.putExtra("roomId", roomId);
            setResult(RESULT_OK, intent);
            finish();
        } else if (!StringUtil.isEmpty(srcActivity) && srcActivity.equals(SetFloorAndRoomActivity.class.getSimpleName())) {
            finish();
        }
    }

    /**
     * requestCode 请求码，即调用startActivityForResult()传递过去的值
     * <p/>
     * resultCode 结果码，结果码用于标识返回数据来自哪个新Activity
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case SELECT_ROOM_TYPE:
                if (data != null) {
                    if (resultCode == RESULT_CANCELED) {
                        selectRoomType = data.getExtras().getInt("roomType");
                        floorId = data.getExtras().getString("floorId");
                        srcActivity = data.getExtras().getString("srcActivity");
                        roomName = data.getExtras().getString("roomName");
                    } else if (resultCode == RESULT_OK) {
                        roomId = data.getExtras().getString("roomId");
                        Intent intent = new Intent();
                        intent.putExtra("roomId", roomId);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
