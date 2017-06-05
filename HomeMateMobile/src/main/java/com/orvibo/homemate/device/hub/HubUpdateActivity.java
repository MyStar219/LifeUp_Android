package com.orvibo.homemate.device.hub;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.HubEventType;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.event.gateway.HubUpgradeEvent;
import com.orvibo.homemate.model.gateway.HubConstant;
import com.orvibo.homemate.model.gateway.upgrade.MulCheckHubUpgrade;
import com.orvibo.homemate.model.gateway.upgrade.OnMulCheckHubUpgradeListener;
import com.orvibo.homemate.util.MyLogger;

import de.greenrobot.event.EventBus;

/**
 * Created by huangqiyao on 2016/7/20 21:54.
 * 主机升级页面
 *
 * @version v1.10
 */
public class HubUpdateActivity extends Activity implements OnMulCheckHubUpgradeListener
//        , OnLogin365Listener
{

    private MulCheckHubUpgrade mMulCheckHubUpgrade;
    private ImageView iv_hub_upgrade_icon;
//    private LoginX mLoginX;

    private static final int WHAT_LOGIN = 1;

    /**
     * 服务器返回主机固件升级结束后app再等待几秒，尽量避免主机固件升级结束但搜索不到或者无法跟主机通信。
     */
    private static final int TIME_LOGIN = 5 * 1000;

    /**
     * true正在升级主机固件
     */
    public static boolean isHubUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isHubUpdating = true;
        setContentView(R.layout.activity_hub_update);
        Intent intent = getIntent();
        if (!intent.hasExtra(IntentKey.HUB_UPGRADE_EVENT)) {
            MyLogger.kLog().e("checkHubUpgradeEvent is null");
            finish();
            return;
        }
        HubUpgradeEvent checkHubUpgradeEvent = (HubUpgradeEvent) intent.getSerializableExtra(IntentKey.HUB_UPGRADE_EVENT);
        MyLogger.kLog().d(checkHubUpgradeEvent);
        iv_hub_upgrade_icon = (ImageView) findViewById(R.id.iv_hub_upgrade_icon);
        startAnim();
        EventBus.getDefault().register(this);
        mMulCheckHubUpgrade = new MulCheckHubUpgrade(getApplicationContext());
        mMulCheckHubUpgrade.setOnMulCheckHubUpgradeListener(this);
        mMulCheckHubUpgrade.checkUpgrade(checkHubUpgradeEvent.getUid());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isHubUpdating = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isHubUpdating = false;
    }

    //    private Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case WHAT_LOGIN:
//                    if (!isFinishing()) {
//                        //主机已升级固件结束，app尝试读主机数据
//                        mLoginX = LoginX.getInstance(getApplicationContext());
//                        mLoginX.setOnLogin365Listener(HubUpdateActivity.this);
//                        mLoginX.autoLogin();
//                    }
//                    break;
//            }
//        }
//    };

    @Override
    public void onHubUpgrading(String uid) {
        MyLogger.kLog().w("uid:" + uid + " upgrading...");
    }

    @Override
    public void onHubUpgradeFinish(String uid) {
        MyLogger.kLog().i("uid:" + uid + " upgrade finish.Start to login");
        if (mMulCheckHubUpgrade != null) {
            mMulCheckHubUpgrade.stopCheckUpgrade();
        }
//        mHandler.removeCallbacksAndMessages(null);
//        mHandler.sendEmptyMessageDelayed(WHAT_LOGIN, TIME_LOGIN);
//        //主机已升级固件结束，app尝试读主机数据
//        mLoginX = LoginX.getInstance(getApplicationContext());
//        mLoginX.setOnLogin365Listener(this);
//        mLoginX.autoLogin();
        stopAnim();
        finish();
    }

    @Override
    public void onError(int errorCode) {
        MyLogger.kLog().e("Fail to check hub upgrade.");
        stopAnim();
        finish();
    }

//    @Override
//    public void onLoginFinish(List<String> gateways, List<String> cocos, int result, int serverLoginResult) {
//        stopAnim();
//        finish();
//    }

    /**
     * 启动升级动画
     */
    private void startAnim() {
        if (iv_hub_upgrade_icon != null) {
            Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            iv_hub_upgrade_icon.startAnimation(operatingAnim);
        }
    }

    /**
     * 停止升级动画
     */
    private void stopAnim() {
        if (iv_hub_upgrade_icon != null) {
            iv_hub_upgrade_icon.clearAnimation();
        }
    }

    public final void onEventMainThread(HubEvent event) {
        final int type = event.getType();
        if (type == HubEventType.UPDATE) {
            //升级结束
            if (event.getUpdateStatus() == HubConstant.Upgrade.FINISH) {
                finish();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按返回不响应
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAnim();
        if (mMulCheckHubUpgrade != null) {
            mMulCheckHubUpgrade.stopCheckUpgrade();
        }
//        if (mLoginX != null) {
//            mLoginX.removeListener(this);
//        }
//        mHandler.removeCallbacksAndMessages(null);
    }
}
