package com.orvibo.homemate.device.allone2;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kookong.app.data.api.IrData;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;
import com.orvibo.homemate.view.popup.SelectAlloneTVPopup;

/**
 * Created by snow on 2016/4/11.
 * 电视，机顶盒红外匹配基类
 */
public class BaseAlloneControlFragment extends BaseControlFragment {

    protected IrKeyButton irKeyButton0;
    protected IrKeyButton irKeyButton1;
    protected IrKeyButton irKeyButton2;
    protected IrKeyButton irKeyButton3;
    protected IrKeyButton irKeyButton4;
    protected IrKeyButton irKeyButton5;
    protected IrKeyButton irKeyButton6;
    protected IrKeyButton irKeyButton7;
    protected IrKeyButton irKeyButton8;
    protected IrKeyButton irKeyButton9;
    protected IrKeyButton irKeyButtonDotDash;//数字键盘中的- -

    protected IrKeyButton irKeyButtonPower;//电源
    protected IrKeyButton irKeyButtonMenu;//菜单

    protected IrKeyButton irKeyButtonVolumeAdd;//音量+
    protected IrKeyButton irKeyButtonVolumeMinus;//音量—

    protected IrKeyButton irKeyButtonMore;//更多
    protected IrKeyButton irKeyButtonDigit;//数字键盘
    protected IrKeyButton irKeyButtonUp;//方向上
    protected IrKeyButton irKeyButtonDown;//方向下
    protected IrKeyButton irKeyButtonLeft;//方向左
    protected IrKeyButton irKeyButtonRight;//方向右
    protected IrKeyButton irKeyButtonOk;//方向OK

    protected LinearLayout digitGridLayout;
    protected RelativeLayout bottomRelativeLayout;
    protected FrameLayout topFrameLayout;

    protected GridView moreKeyGridView;
    protected MorekeyGridViewAdapter morekeyGridViewAdapter;

    protected boolean digitViewIsShowed = false;
    protected boolean digitViewIsAnim = false;
    protected boolean expandViewIsShowed = false;
    protected boolean expandViewIsAnim = false;


    protected SelectAlloneTVPopup selectAlloneTVPopup;


    protected static int ANIMATION_DURATION = 500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
        initGridData(false);
        initListener();
        setSelectedState();
    }

    private void initView(View view) {
        irKeyButton0 = (IrKeyButton) view.findViewById(R.id.irKeyButton0);
        irKeyButton1 = (IrKeyButton) view.findViewById(R.id.irKeyButton1);
        irKeyButton2 = (IrKeyButton) view.findViewById(R.id.irKeyButton2);
        irKeyButton3 = (IrKeyButton) view.findViewById(R.id.irKeyButton3);
        irKeyButton4 = (IrKeyButton) view.findViewById(R.id.irKeyButton4);
        irKeyButton5 = (IrKeyButton) view.findViewById(R.id.irKeyButton5);
        irKeyButton6 = (IrKeyButton) view.findViewById(R.id.irKeyButton6);
        irKeyButton7 = (IrKeyButton) view.findViewById(R.id.irKeyButton7);
        irKeyButton8 = (IrKeyButton) view.findViewById(R.id.irKeyButton8);
        irKeyButton9 = (IrKeyButton) view.findViewById(R.id.irKeyButton9);
        irKeyButtonMenu = (IrKeyButton) view.findViewById(R.id.irKeyButtonMenu);

        irKeyButtonVolumeAdd = (IrKeyButton) view.findViewById(R.id.irKeyButtonVolumeAdd);
        irKeyButtonVolumeMinus = (IrKeyButton) view.findViewById(R.id.irKeyButtonVolumeMinus);
        irKeyButtonPower = (IrKeyButton) view.findViewById(R.id.irKeyButtonPower);

        irKeyButtonMore = (IrKeyButton) view.findViewById(R.id.irKeyButtonMore);
        irKeyButtonDigit = (IrKeyButton) view.findViewById(R.id.irKeyButtonDigit);
        irKeyButtonUp = (IrKeyButton) view.findViewById(R.id.irKeyButtonUp);
        irKeyButtonDown = (IrKeyButton) view.findViewById(R.id.irKeyButtonDown);
        irKeyButtonLeft = (IrKeyButton) view.findViewById(R.id.irKeyButtonLeft);
        irKeyButtonRight = (IrKeyButton) view.findViewById(R.id.irKeyButtonRight);
        irKeyButtonOk = (IrKeyButton) view.findViewById(R.id.irKeyButtonOk);
        irKeyButtonDotDash = (IrKeyButton) view.findViewById(R.id.irKeyButtonDotDash);
        digitGridLayout = (LinearLayout) view.findViewById(R.id.digitLinearLayout);
        bottomRelativeLayout = (RelativeLayout) view.findViewById(R.id.bottomRelativeLayout);
        topFrameLayout = (FrameLayout) view.findViewById(R.id.topFrameLayout);
        moreKeyGridView = (GridView) view.findViewById(R.id.moreKeyGridView);

        mainIrKeyButtons.add(irKeyButton0);
        mainIrKeyButtons.add(irKeyButton1);
        mainIrKeyButtons.add(irKeyButton2);
        mainIrKeyButtons.add(irKeyButton3);
        mainIrKeyButtons.add(irKeyButton4);
        mainIrKeyButtons.add(irKeyButton5);
        mainIrKeyButtons.add(irKeyButton6);
        mainIrKeyButtons.add(irKeyButton7);
        mainIrKeyButtons.add(irKeyButton8);
        mainIrKeyButtons.add(irKeyButton9);
        mainIrKeyButtons.add(irKeyButtonMenu);
        mainIrKeyButtons.add(irKeyButtonPower);
        mainIrKeyButtons.add(irKeyButtonVolumeAdd);
        mainIrKeyButtons.add(irKeyButtonVolumeMinus);

        mainIrKeyButtons.add(irKeyButtonUp);
        mainIrKeyButtons.add(irKeyButtonDown);
        mainIrKeyButtons.add(irKeyButtonLeft);
        mainIrKeyButtons.add(irKeyButtonRight);
        mainIrKeyButtons.add(irKeyButtonOk);
        mainIrKeyButtons.add(irKeyButtonDotDash);

        /**
         * 防止数字键盘穿透点击
         */
        digitGridLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //处理数字键盘是否显示
        irKeyButtonDigit.setVisibility(alloneData.isHasNumber() ? View.VISIBLE : View.GONE);
    }

    /**
     * 根据action设置数字键盘或者更多按钮是否自动弹出
     */
    private void setSelectedState() {
        if (isAction && action != null) {//判断数字键盘或者更多按钮是否自动弹出
            if (AlloneDataUtil.isInNumber(action.getValue2())) {
                digitViewIsShowed = true;
            } else if (!AlloneDataUtil.isInMainPage(action.getValue2(), device.getDeviceType())&&action.getValue2()!=0) {
                expandViewIsShowed = true;
            }
        }
    }

    /**
     * 初始或更新更多中的数据
     */
    private void initGridData(boolean isUpdate) {
        if (irKeys == null) {
            irKeyButtonMore.setVisibility(View.GONE);
        } else {
            if (!isUpdate) {
                morekeyGridViewAdapter = new MorekeyGridViewAdapter(getActivity(), irKeys, irData.fre, keyClickListener, false);
                morekeyGridViewAdapter.setAction(isAction);
                morekeyGridViewAdapter.setAction(action);
                moreKeyGridView.setAdapter(morekeyGridViewAdapter);
            } else if (morekeyGridViewAdapter != null) {
                morekeyGridViewAdapter.setAction(isAction);
                morekeyGridViewAdapter.setAction(action);
                morekeyGridViewAdapter.updateData(irKeys, irData.fre);
            }
        }

    }

    @Override
    public void initListener() {
        super.initListener();
        irKeyButtonDigit.setOnClickListener(this);
        irKeyButtonDigit.getBackground().setAlpha(255);
        irKeyButtonMore.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.irKeyButtonDigit:
                if (!digitViewIsAnim) {
                    if (expandViewIsShowed) {
                        hideExpandView();
                        showDigitView();
                    } else {
                        if (digitViewIsShowed) {
                            hideDigitView();
                        } else {
                            showDigitView();
                        }
                    }
                }
                break;

            case R.id.irKeyButtonMore:
                if (!expandViewIsAnim) {
                    if (digitViewIsShowed) {
                        hideDigitView();
                        showExpandView();
                    } else {
                        if (expandViewIsShowed) {
                            hideExpandView();
                        } else {
                            showExpandView();
                        }
                    }
                }
                break;

        }
    }

    private void showDigitView() {
        int toYDelta = bottomRelativeLayout.getTop() - topFrameLayout.getTop();
        final TranslateAnimation animation = new TranslateAnimation(0, 0, toYDelta, 0);
        animation.setDuration(ANIMATION_DURATION);//设置动画持续时间
        animation.setRepeatCount(0);//设置重复次数
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                digitViewIsAnim = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                digitViewIsAnim = false;
            }
        });
        digitGridLayout.startAnimation(animation);
        digitGridLayout.setVisibility(View.VISIBLE);
        irKeyButtonDigit.setBackgroundResource(R.drawable.icon_allone_pulldown_selector);
        irKeyButtonDigit.setText("");
        digitViewIsShowed = true;
    }

    private void hideDigitView() {
        int toYDelta = bottomRelativeLayout.getTop() - topFrameLayout.getTop();
        final TranslateAnimation animation = new TranslateAnimation(0, 0, 0, toYDelta);
        animation.setDuration(ANIMATION_DURATION);//设置动画持续时间
        animation.setRepeatCount(0);//设置重复次数
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                digitViewIsAnim = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                digitGridLayout.setVisibility(View.GONE);
                digitViewIsAnim = false;
            }
        });
        digitGridLayout.startAnimation(animation);
        irKeyButtonDigit.setBackgroundResource(R.drawable.icon_bg_green_selector);
        irKeyButtonDigit.getBackground().setAlpha(255);
        irKeyButtonDigit.setText("123");
        digitViewIsShowed = false;
    }

    private void showExpandView() {
        int toYDelta = bottomRelativeLayout.getTop() - moreKeyGridView.getTop();
        final TranslateAnimation animation = new TranslateAnimation(0, 0, toYDelta, 0);
        animation.setDuration(ANIMATION_DURATION);//设置动画持续时间
        animation.setRepeatCount(0);//设置重复次数
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                expandViewIsAnim = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                expandViewIsAnim = false;
            }
        });
        moreKeyGridView.startAnimation(animation);
        moreKeyGridView.setVisibility(View.VISIBLE);
        irKeyButtonMore.setBackgroundResource(R.drawable.icon_allone_pulldown_selector);
        expandViewIsShowed = true;

    }

    private void hideExpandView() {
        int toYDelta = bottomRelativeLayout.getTop() - moreKeyGridView.getTop();
        final TranslateAnimation animation = new TranslateAnimation(0, 0, 0, toYDelta);
        animation.setDuration(ANIMATION_DURATION);//设置动画持续时间
        animation.setRepeatCount(0);//设置重复次数
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                expandViewIsAnim = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                moreKeyGridView.setVisibility(View.GONE);
                expandViewIsAnim = false;
            }
        });
        moreKeyGridView.startAnimation(animation);
        irKeyButtonMore.setBackgroundResource(R.drawable.icon_allone2_more_selector);
        expandViewIsShowed = false;
    }

    @Override
    public void onRefresh(IrData irData) {
        super.onRefresh(irData);
        initGridData(true);
        initListener();
    }

    @Override
    public void onRefresh(Action action) {
        super.onRefresh(action);
    }

    @Override
    public void onResume() {
        super.onResume();
        //处理定时倒计时按钮更新时的界面显示
        if (isAction) {
            if (digitViewIsShowed && !digitGridLayout.isShown()) {
                showDigitView();
            }
            if (expandViewIsShowed && !moreKeyGridView.isShown()) {
                showExpandView();
            }
        }
    }

}
