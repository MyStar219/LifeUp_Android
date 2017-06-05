package com.orvibo.homemate.view.popup;

import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.SceneBind;
import com.orvibo.homemate.common.BaseActivity;
import com.orvibo.homemate.smartscene.adapter.SceneBindFailAdapter;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.PhoneUtil;

import java.util.List;

/**
 * 情景绑定失败信息界面
 * Created by huangqiyao on 2015/6/1.
 */
public abstract class SceneBindActionFailPopup extends CommonPopup implements View.OnClickListener {
    private static final String TAG = SceneBindActionFailPopup.class.getSimpleName();

    /**
     * 最多尝试3次
     */
    private static final int MAX_COUNT = 3;
    private int mHasTryCount = 0;
    private BaseActivity mActivity;
    private Resources mRes;
    private List<SceneBind> mBindFailSceneBinds;
    private SceneBindFailAdapter mSceneBindAdapter;

    //view
    private TextView tip_tv;
    private ListView bind_lv;
    private TextView reason_tv;
    private LinearLayout confirmAndCancel_ll;
    private TextView ok_tv;

    public SceneBindActionFailPopup(BaseActivity activity) {
        mActivity = activity;
        mRes = activity.getResources();
    }

    public void setFailSceneBinds(List<SceneBind> bindFailSceneBinds, int totalCount) {
        if (bindFailSceneBinds == null) {
            LogUtil.e(TAG, "setFailSceneBinds()-bindFailSceneBinds is null.");
            return;
        }
        if (mActivity.isFinishingOrDestroyed()) {
            LogUtil.w(TAG, "setFailSceneBinds()- activity is finishing or has been destroyed.");
            return;
        }
        mBindFailSceneBinds = bindFailSceneBinds;
        if (isShowing()) {
            mHasTryCount += 1;
            mSceneBindAdapter.refreshList(mBindFailSceneBinds);
            showRetryView(totalCount - mBindFailSceneBinds.size(), mBindFailSceneBinds.size());
            if (mHasTryCount >= MAX_COUNT) {
                showTry3CountView(mBindFailSceneBinds.size());
            }
        } else {
            mHasTryCount = 1;
            show(mBindFailSceneBinds, totalCount - bindFailSceneBinds.size());
        }
    }

    private void show(List<SceneBind> bindFailSceneBinds, int successCount) {
        View contentView = LayoutInflater.from(mActivity).inflate(
                R.layout.popup_bind_fail, null);
        tip_tv = (TextView) contentView.findViewById(R.id.tip_tv);
        bind_lv = (ListView) contentView.findViewById(R.id.bind_lv);
        reason_tv = (TextView) contentView.findViewById(R.id.reason_tv);
        confirmAndCancel_ll = (LinearLayout) contentView.findViewById(R.id.confirmAndCancel_ll);
        contentView.findViewById(R.id.cancel_tv).setOnClickListener(this);
        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        ok_tv = (TextView) contentView.findViewById(R.id.ok_tv);
        ok_tv.setOnClickListener(this);

        mSceneBindAdapter = new SceneBindFailAdapter(mActivity, bindFailSceneBinds);
        bind_lv.setAdapter(mSceneBindAdapter);
        showRetryView(successCount, bindFailSceneBinds.size());
        mPopup = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mPopup.setFocusable(true);
        mPopup.setTouchable(true);
        //mPopup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tran));
        int popupHeight = mPopup.getHeight();
        int sHeight = PhoneUtil.getScreenPixels(mActivity)[1];
        LogUtil.d(TAG, "showPopup()-width:" + mPopup.getWidth() + ",height:" + popupHeight);
        mPopup.showAtLocation(contentView, Gravity.CENTER, 0, 0);
    }

    private void showRetryView(int successCount, int failCount) {
        setTip(R.string.scene_bind_fail_tip, successCount, failCount);

        reason_tv.setVisibility(View.GONE);
        confirmAndCancel_ll.setVisibility(View.VISIBLE);
        ok_tv.setVisibility(View.GONE);
    }

    /**
     * 重试结束界面
     *
     * @param failCount
     */
    private void showTry3CountView(int failCount) {
        setTip(R.string.scene_bind_fail_3count_tip, mHasTryCount, failCount);
        reason_tv.setVisibility(View.VISIBLE);
        confirmAndCancel_ll.setVisibility(View.GONE);
        ok_tv.setVisibility(View.VISIBLE);
    }

    private void setTip(int res, int p1, int p2) {
        String tip = mRes.getString(res);
        tip = String.format(tip, p1, p2);
        tip_tv.setText(tip);
    }

    @Override
    public void onClick(View v) {
        final int vId = v.getId();
        if (vId == R.id.confirm_tv) {
            onRetry();
        } else if (vId == R.id.cancel_tv) {
            onCancel();
            dismiss();
        } else if (vId == R.id.ok_tv) {
            onCancel();
            dismiss();
        }
    }

    /**
     * 重试
     */
    protected abstract void onRetry();

    /**
     * 取消。把未绑定成功的sceneBind从情景绑定列表移除。
     */
    protected abstract void onCancel();
}
