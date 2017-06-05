package com.orvibo.homemate.device.bind;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.dao.DeviceDao;
import com.orvibo.homemate.dao.SceneDao;
import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.sharedPreferences.SmartSceneCache;
import com.orvibo.homemate.smartscene.adapter.SceneAdapter;
import com.orvibo.homemate.util.LogUtil;

import java.util.List;

/**
 * 绑定选择情景
 *
 * @intent {@link IntentKey#SCENE}传递已选择的情景对象
 * @result {@link IntentKey#SCENE}返回已选择的情景对象
 * Created by huangqiyao on 2015/5/6.
 */
public class SelectSceneActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = SelectSceneActivity.class.getSimpleName();
    private SceneAdapter mSceneAdapter;
    private Scene mSelectedScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_scene);
        mSelectedScene = (Scene) getIntent().getSerializableExtra(IntentKey.SCENE);

        TextView back_titlebar_tv = (TextView) findViewById(R.id.back_titlebar_tv);
        back_titlebar_tv.setText(R.string.scene);

        GridView scenes_gv = (GridView) findViewById(R.id.gridView);
        scenes_gv.setOnItemClickListener(this);

        DeviceDao mDeviceDao = new DeviceDao();
        //根据是否需要显示灯光全开全关情景，来返回智能遥控器可绑定的情景
        List<Scene> scenes = new SceneDao().selScenes(currentMainUid,isShowAllRightScene(mDeviceDao));
        mSceneAdapter = new SceneAdapter(this, scenes, mSelectedScene == null ? Constant.INVALID_NUM : mSelectedScene.getSceneId());
        scenes_gv.setAdapter(mSceneAdapter);
    }

    /**
     * 查询是否需要显示灯光全开全关的情景
     * @param mDeviceDao
     * @return
     */
    private boolean isShowAllRightScene(DeviceDao mDeviceDao){
        int lampCount = mDeviceDao.selZigbeeLampCount(currentMainUid);
        boolean shown = false;
        //判断默认情景的显示状态
        boolean showed = SmartSceneCache.isDefaultSceneShowed(currentMainUid);
        //默认情景没有显示并且灯光数超过3个时：
        if (!showed && lampCount >= 3) {
            shown = true;
            SmartSceneCache.saveDefaultSceneShow(currentMainUid, shown);
        } else {
            shown = showed;
        }
        return shown;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Scene scene = (Scene) view.getTag(R.id.tag_scene);
        LogUtil.d(TAG, "onItemClick()-mSelectedScene:" + mSelectedScene + ",scene:" + scene);
        if (mSelectedScene != null && mSelectedScene.getSceneId() == scene.getSceneId()) {
            mSelectedScene = null;
        } else {
            mSelectedScene = scene;
        }
        mSceneAdapter.selectScene(mSelectedScene == null ? Constant.INVALID_NUM : mSelectedScene.getSceneId());
        //选中返回
        returnData();
        leftTitleClick(null);
    }

    @Override
    public void onBackPressed() {
        returnData();
        super.onBackPressed();
    }

    @Override
    public void leftTitleClick(View v) {
        returnData();
        super.leftTitleClick(v);
    }

    private void returnData() {
        LogUtil.d(TAG, "returnData()-mSelectedScene:" + mSelectedScene);
        Intent intent = new Intent();
        intent.putExtra(IntentKey.SCENE, mSelectedScene);
        setResult(RESULT_OK, intent);
    }
}
