package com.orvibo.homemate.smartscene.manager.selecthumiturepopup;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.common.WheelAdapter;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.view.custom.wheelview.TosGallery;
import com.orvibo.homemate.view.custom.wheelview.WheelBo;
import com.orvibo.homemate.view.custom.wheelview.WheelView;
import com.orvibo.homemate.view.popup.CommonPopup;

import java.util.List;

/**
 * 联动选择温湿度传感器弹出的选择温度/湿度界面
 * Created by huangqiyao on 2016/3/10.
 */
public class SelectHumiturePopup extends CommonPopup implements View.OnClickListener {
    private static final String TAG = SelectHumiturePopup.class.getSimpleName();
    private Context mContext;

    private LinearLayout wheel_ll;
    private WheelView mLeft_wv;
    private WheelView mRight_wv;

    private WheelAdapter mRightAdapter;
    private TextView title_tv;
    private List<WheelBo> mLeftBos;
    private List<WheelBo> mRightBos;

    public SelectHumiturePopup(Context context) {
        mContext = context;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dismiss();
        }
    };

    public void dismissPopupDelay() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(0, 200);
    }

    public void show(int selectedLeftPos, int selectedRightPos, List<WheelBo> leftBos, List<WheelBo> rightBos) {
        View contentView = LayoutInflater.from(mContext).inflate(
                R.layout.popup_set_room, null);
        wheel_ll = (LinearLayout) contentView.findViewById(R.id.wheel_ll);
        inAnim();
        mLeftBos = leftBos;
        mRightBos = rightBos;
        TextView cancel_tv = (TextView) contentView.findViewById(R.id.cancel_tv);
        cancel_tv.setVisibility(View.INVISIBLE);
        title_tv = (TextView) contentView.findViewById(R.id.title_tv);

        contentView.findViewById(R.id.confirm_tv).setOnClickListener(this);
        contentView.findViewById(R.id.v1).setOnClickListener(this);

        mLeft_wv = (WheelView) contentView.findViewById(R.id.floor_wv);
        mLeft_wv.setScrollCycle(false);
        mLeft_wv.setOnEndFlingListener(mLeftSelectListener);
        mLeft_wv.setAdapter(new WheelAdapter(mContext, mLeftBos));
        mLeft_wv.setSelection(selectedLeftPos);

        mRight_wv = (WheelView) contentView.findViewById(R.id.room_wv);
        mRight_wv.setScrollCycle(false);
        mRight_wv.setOnEndFlingListener(mRightSelectListener);
        mRightAdapter = new WheelAdapter(mContext, mRightBos);
        mRight_wv.setAdapter(mRightAdapter);
        mRight_wv.setSelection(selectedRightPos);

        show(mContext, contentView, true);
    }

    /**
     * 先show再设置ｔｉｔｌｅ
     * @param resId
     */
    public void setTitle(int resId) {
        try {
            title_tv.setText(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_in));
    }

    public void outAnim() {
        wheel_ll.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top_out));
    }

    private TosGallery.OnEndFlingListener mLeftSelectListener = new TosGallery.OnEndFlingListener() {
        @Override
        public void onEndFling(TosGallery v) {
            WheelBo wheelBo = (WheelBo) v.getSelectedItem();
            LogUtil.d(TAG, "onEndFling(left)-wheelBo:" + wheelBo);
            mLeft_wv.setSelection(v.getSelectedItemPosition());
        }
    };

    private TosGallery.OnEndFlingListener mRightSelectListener = new TosGallery.OnEndFlingListener() {
        @Override
        public void onEndFling(TosGallery v) {
            //选择房间
            WheelBo wheelBo = (WheelBo) v.getSelectedItem();
            LogUtil.d(TAG, "onEndFling(right)-wheelBo:" + wheelBo);
            mRight_wv.setSelection(v.getSelectedItemPosition());
        }
    };

    @Override
    public final void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_tv:
                outAnim();
                try {
                    int leftPos = mLeft_wv.getSelectedItemPosition();
                    WheelBo leftBo = mLeftBos.get(leftPos);
                    int rightPos = mRight_wv.getSelectedItemPosition();
                    WheelBo righBo = mRightBos.get(rightPos);
                    onSelect(leftPos, leftBo, rightPos, righBo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dismissPopupDelay();
                break;
            case R.id.cancel_tv:
                outAnim();
                dismissPopupDelay();
                break;
            case R.id.v1:
                dismissPopupDelay();
                break;
        }
    }

    /**
     * @param selectedLeftPos
     * @param leftBo
     * @param selectedRightPos
     * @param rightBo
     */
    public void onSelect(int selectedLeftPos, WheelBo leftBo, int selectedRightPos, WheelBo rightBo) {
    }

}
