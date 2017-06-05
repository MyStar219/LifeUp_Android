package com.orvibo.homemate.device.allone2.irlearn;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Action;
import com.orvibo.homemate.bo.KKDevice;
import com.orvibo.homemate.bo.KKIr;
import com.orvibo.homemate.util.AlloneDataUtil;
import com.orvibo.homemate.view.custom.IrKeyButton;

/**
 * Created by snow on 2016/4/11.
 * 红外学习fragment基类
 */
public class AlloneBaseLearnFragment extends BaseLearnFragment implements View.OnClickListener, RemoteLearnActivity.OnIrLearnRefreshListener {

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

    protected IrKeyButton irKeyButtonMenu;
    protected IrKeyButton irKeyButtonBack;

    protected IrKeyButton irKeyButtonVolumeAdd;
    protected IrKeyButton irKeyButtonVolumeMinus;
    protected IrKeyButton irKeyButtonMute;
    protected IrKeyButton irKeyButtonMore;
    protected IrKeyButton irKeyButtonDigit;
    protected IrKeyButton irKeyButtonUp;
    protected IrKeyButton irKeyButtonDown;
    protected IrKeyButton irKeyButtonLeft;
    protected IrKeyButton irKeyButtonRight;
    protected IrKeyButton irKeyButtonOk;
    protected IrKeyButton irKeyButtonDotDash;
    protected IrKeyButton irKeyButtonPower;
    protected LinearLayout digitGridLayout;
    protected RelativeLayout bottomRelativeLayout;
    protected FrameLayout topFrameLayout;
    //private LinearLayout ll_morekey;
    protected GridView moreKeyGridView;
    protected TextView tv_morekey_tips;

    protected boolean digitViewIsShowed = false;
    protected boolean digitViewIsAnim = false;
    protected boolean expandViewIsShowed = false;
    protected boolean expandViewIsAnim = false;

    protected MoreGridViewAdapter morekeyGridViewAdapter;


    public static int ANIMATION_DURATION = 500;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(view);
        super.onViewCreated(view, savedInstanceState);
        initData();
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
        irKeyButtonBack = (IrKeyButton) view.findViewById(R.id.irKeyButtonBack);
        irKeyButtonVolumeAdd = (IrKeyButton) view.findViewById(R.id.irKeyButtonVolumeAdd);
        irKeyButtonVolumeMinus = (IrKeyButton) view.findViewById(R.id.irKeyButtonVolumeMinus);
        irKeyButtonMute = (IrKeyButton) view.findViewById(R.id.irKeyButtonMute);
        irKeyButtonMore = (IrKeyButton) view.findViewById(R.id.irKeyButtonMore);
        irKeyButtonDigit = (IrKeyButton) view.findViewById(R.id.irKeyButtonDigit);
        irKeyButtonUp = (IrKeyButton) view.findViewById(R.id.irKeyButtonUp);
        irKeyButtonDown = (IrKeyButton) view.findViewById(R.id.irKeyButtonDown);
        irKeyButtonLeft = (IrKeyButton) view.findViewById(R.id.irKeyButtonLeft);
        irKeyButtonRight = (IrKeyButton) view.findViewById(R.id.irKeyButtonRight);
        irKeyButtonOk = (IrKeyButton) view.findViewById(R.id.irKeyButtonOk);
        irKeyButtonDotDash = (IrKeyButton) view.findViewById(R.id.irKeyButtonDotDash);
        irKeyButtonPower = (IrKeyButton) view.findViewById(R.id.irKeyButtonPower);
        digitGridLayout = (LinearLayout) view.findViewById(R.id.digitLinearLayout);
        bottomRelativeLayout = (RelativeLayout) view.findViewById(R.id.bottomRelativeLayout);
        topFrameLayout = (FrameLayout) view.findViewById(R.id.topFrameLayout);
        //ll_morekey = (LinearLayout) view.findViewById(R.id.ll_morekey);
        moreKeyGridView = (GridView) view.findViewById(R.id.moreKeyGridView);
        tv_morekey_tips = (TextView) view.findViewById(R.id.tv_morekey_tips);

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
        mainIrKeyButtons.add(irKeyButtonBack);
        mainIrKeyButtons.add(irKeyButtonVolumeAdd);
        mainIrKeyButtons.add(irKeyButtonVolumeMinus);
        mainIrKeyButtons.add(irKeyButtonMute);
        mainIrKeyButtons.add(irKeyButtonUp);
        mainIrKeyButtons.add(irKeyButtonDown);
        mainIrKeyButtons.add(irKeyButtonLeft);
        mainIrKeyButtons.add(irKeyButtonRight);
        mainIrKeyButtons.add(irKeyButtonOk);
        mainIrKeyButtons.add(irKeyButtonDotDash);
        mainIrKeyButtons.add(irKeyButtonPower);

        /**
         * 防止数字键盘穿透点击
         */
        digitGridLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }


    private void initData() {
        if (expandViewIsShowed && !isStartLearn) {
            if ((irKeys == null || irKeys.size() <= 0)) {
                tv_morekey_tips.setVisibility(View.VISIBLE);
                moreKeyGridView.setVisibility(View.GONE);
            } else {
                tv_morekey_tips.setVisibility(View.GONE);
                moreKeyGridView.setVisibility(View.VISIBLE);
            }
            moreKeyGridView.setVisibility(View.VISIBLE);
        }
        if (morekeyGridViewAdapter == null) {
            morekeyGridViewAdapter = new MoreGridViewAdapter(getActivity(), irKeys, isStartLearn, keyClickListener, mOnIrKeyLongClickListener, device);
            morekeyGridViewAdapter.setAction(action);
            morekeyGridViewAdapter.setAction(isAction);
            moreKeyGridView.setAdapter(morekeyGridViewAdapter);
        } else {
            morekeyGridViewAdapter.setAction(action);
            morekeyGridViewAdapter.setAction(isAction);
            morekeyGridViewAdapter.updateData(irKeys, isStartLearn);
        }
    }

    protected void initListener() {
        for (final IrKeyButton irKeyButton : mainIrKeyButtons) {
            int fid = irKeyButton.getFid();
            if (keyHashMap != null && keyHashMap.containsKey(fid) && !TextUtils.isEmpty(keyHashMap.get(fid).getPluse())) {
                irKeyButton.setLearned(true);
                irKeyButton.setKkIr(keyHashMap.get(fid));
                if (isAction && action != null && action.getValue2() == fid) {
                    irKeyButton.setSelected(true);
                } else {
                    irKeyButton.setSelected(false);
                }
            } else {
                irKeyButton.setLearned(false);
                KKIr kkIr = new KKIr();
                kkIr.setFid(fid);
                kkIr.setfKey(irKeyButton.getText().toString());
                kkIr.setfName(irKeyButton.getText().toString());
                irKeyButton.setKkIr(kkIr);
            }
            irKeyButton.setStartLearn(isStartLearn);
            irKeyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    keyClickListener.OnClick(irKeyButton);
                }
            });
        }
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
        irKeyButtonDigit.setText("123");
        irKeyButtonDigit.getBackground().setAlpha(255);
        digitViewIsShowed = false;
    }

    private void showExpandView() {
        int toYDelta;
        //正常使用状态并且没有添加更多按键
        if ((!isStartLearn) && (irKeys == null || irKeys.size() <= 0)) {
            toYDelta = bottomRelativeLayout.getTop() - tv_morekey_tips.getTop();
        } else {
            toYDelta = bottomRelativeLayout.getTop() - moreKeyGridView.getTop();
        }
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
        //正常使用状态并且没有添加更多按键
        if ((!isStartLearn) && (irKeys == null || irKeys.size() <= 0)) {
            tv_morekey_tips.startAnimation(animation);
            tv_morekey_tips.setVisibility(View.VISIBLE);
        } else {
            moreKeyGridView.startAnimation(animation);
            moreKeyGridView.setVisibility(View.VISIBLE);
        }
        irKeyButtonMore.setBackgroundResource(R.drawable.icon_allone_pulldown_selector);
        expandViewIsShowed = true;

    }

    private void hideExpandView() {
        int toYDelta;
        //正常使用状态并且没有添加更多按键
        if ((!isStartLearn) && (irKeys == null || irKeys.size() <= 0)) {
            toYDelta = bottomRelativeLayout.getTop() - tv_morekey_tips.getTop();
        } else {
            toYDelta = bottomRelativeLayout.getTop() - moreKeyGridView.getTop();
        }
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
                if ((!isStartLearn) && (irKeys == null || irKeys.size() <= 0)) {
                    tv_morekey_tips.setVisibility(View.GONE);
                } else {
                    moreKeyGridView.setVisibility(View.GONE);
                }
                expandViewIsAnim = false;
            }
        });
        if ((!isStartLearn) && (irKeys == null || irKeys.size() <= 0)) {
            tv_morekey_tips.startAnimation(animation);
        } else {
            moreKeyGridView.startAnimation(animation);
        }
        irKeyButtonMore.setBackgroundResource(R.drawable.icon_allone2_more_selector);
        expandViewIsShowed = false;
    }


    @Override
    public void onPause() {
        super.onPause();
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


    @Override
    public void onRefresh(KKDevice irData, boolean isStartLearn) {
        super.onRefresh(irData, isStartLearn);
        initData();
        initListener();
    }

    @Override
    public void onRefresh(Action action) {
        this.action = action;
        initData();
        initListener();
    }

    /**
     * 根据action设置数字键盘或者更多按钮是否自动弹出
     */
    private void setSelectedState() {
        if (isAction && action != null) {//判断数字键盘或者更多按钮是否自动弹出
            if (AlloneDataUtil.isInNumber(action.getValue2())) {
                digitViewIsShowed = true;
            } else if (!AlloneDataUtil.isInMainPage(action.getValue2(), device.getDeviceType()) && action.getValue2() != 0) {
                expandViewIsShowed = true;
            }
        }
    }

}
