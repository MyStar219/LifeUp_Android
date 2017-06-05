package com.orvibo.homemate.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.LoginType;
import com.orvibo.homemate.device.manage.DeviceManageActivity;
import com.orvibo.homemate.model.login.Logout;
import com.orvibo.homemate.room.AllHomeActivity;
import com.orvibo.homemate.util.LogUtil;

/**
 * 此类已不再使用 by huangqiyao at 2016/4/9
 * @deprecated 旧主机管理交互，已不再使用。
 */
public class SetActivity extends BaseActivity {
    private static final String TAG = SetActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        findViewById(R.id.set_device_ll).setOnClickListener(this);
        findViewById(R.id.set_scene_ll).setOnClickListener(this);
        findViewById(R.id.set_room_ll).setOnClickListener(this);
//        findViewById(R.id.set_jieneng_ll).setOnClickListener(this);
//        findViewById(R.id.set_addGateway_ll).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.set_device_ll) {
            startActivity(new Intent(this, DeviceManageActivity.class));
        } else if (vId == R.id.set_scene_ll) {
            //TODO 屏蔽
//            startActivity(new Intent(this, SceneManageActivity.class));
        } else if (vId == R.id.set_room_ll) {
            startActivity(new Intent(this, AllHomeActivity.class));
        }
//        else if (vId == R.id.set_jieneng_ll) {
//            //startActivity(new Intent(this, DeviceManageActivity.class));
//            ToastUtil.showToast( R.string.NOT_COMPLETE);
//        } else if (vId == R.id.set_addGateway_ll) {
////            BindGatewayCache.saveFromAddNewGateway(mContext);
////            Intent intent = new Intent(this, BindingActivity.class);
////            intent.putExtra(IntentKey.FROM_ADD_GATEWAY, true);
////            startActivity(intent);
//        }
    }

    @Override
    public void onBackPressed() {
        logout();
        super.onBackPressed();
    }

    @Override
    public void leftTitleClick(View v) {
        logout();
        super.leftTitleClick(v);
    }

    private void logout() {
        LogUtil.d(TAG, "logout()");
        Context context = mContext.getApplicationContext();
        new Logout(mContext).logoutVicenter(currentMainUid, LoginType.ADMIN_LOGIN);
    }
}
