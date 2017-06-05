package com.orvibo.homemate.common;

import android.content.Intent;
import android.text.TextUtils;

import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.data.IntentKey;
import com.orvibo.homemate.device.hub.HubUpdateActivity;
import com.orvibo.homemate.event.gateway.HubUpgradeEvent;
import com.orvibo.homemate.model.gateway.HubConstant;
import com.orvibo.homemate.model.gateway.upgrade.CheckHubUpgrade;
import com.orvibo.homemate.sharedPreferences.UserCache;

/**
 * Created by huangqiyao on 2016/8/12 12:20.
 * BaseActivity的逻辑交互放到此类
 *
 * @version v1.10
 */
public class BasePresenter {
    private BaseActivity mActivity;

    public BasePresenter(BaseActivity activity) {
        mActivity = activity;
    }

    /**
     * 检查主机固件升级
     */
    public void onCheckHubUpgrade() {
        String hubUid = UserCache.getCurrentMainUid(mActivity);
        if (!TextUtils.isEmpty(hubUid)) {
            new CheckHubUpgrade(mActivity.getApplicationContext()) {
                @Override
                protected void onHubUpgradeStatus(String uid, int result, int upgradeStatus) {
                    super.onHubUpgradeStatus(uid, result, upgradeStatus);
                    stopCheckUpgrade();
                    if (mActivity == null || mActivity.isFinishingOrDestroyed()) {
                        return;
                    }
                    if (result == ErrorCode.SUCCESS) {
                        if (upgradeStatus == HubConstant.Upgrade.UPGRADING) {
                            HubUpgradeEvent checkHubUpgradeEvent = new HubUpgradeEvent(uid, Constant.INVALID_NUM, result, upgradeStatus);
                            Intent intent = new Intent(mActivity, HubUpdateActivity.class);
                            intent.putExtra(IntentKey.HUB_UPGRADE_EVENT, checkHubUpgradeEvent);
                            mActivity.startActivity(intent);
//                            mActivity.overridePendingTransition(R.anim.top_to_bottom_in_slow, R.anim.top_to_bottom_out_slow);
                        }
                    }
                }
            }.checkUpgrade(hubUid);
        }

    }
}
