package com.orvibo.homemate.view.custom;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgateway.app.R;

import java.io.IOException;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;


/**
 * Created by snown on 2015/5/29.
 */
public class SceneAnimDialog extends DialogFragment implements View.OnClickListener, AnimationListener, Animation.AnimationListener {
    private ImageView gifView;
    private GifDrawable gifFromRaw;
    private TextView tipOne, tipTwo;
    private Animation aplaAnim, aplaAnimTwo;
    private Handler handler;
    private String tipOneStr, tipTwoStr;

    /**
     * @param layout    布局
     * @param source    gif动画
     * @param speed     gif动画速度
     * @param firstTip  动画第一个提示
     * @param secendTip 动画第二个提示
     * @return
     */
    public static SceneAnimDialog newInstance(int layout, int source, float speed, String firstTip, String secendTip) {
        SceneAnimDialog dialog = new SceneAnimDialog();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putInt("source", source);
        args.putFloat("speed", speed);
        args.putString("firstTip", firstTip);
        args.putString("secendTip", secendTip);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(getArguments().getInt("layout"), null);
        view.setOnClickListener(this);
        Dialog dialog = new Dialog(getActivity(), R.style.AnimDialog);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        tipOneStr = getArguments().getString("firstTip");
        tipTwoStr = getArguments().getString("secendTip");

        tipOne = (TextView) view.findViewById(R.id.tip_one);
        tipOne.setText(tipOneStr);
        aplaAnim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.scene_apla);
        aplaAnim.setAnimationListener(this);
        if (!TextUtils.isEmpty(tipTwoStr)) {
            tipTwo = (TextView) view.findViewById(R.id.tip_two);
            handler = new Handler(new MyHandlerCallback());
        }
        aplaAnimTwo = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.scene_apla);

        gifView = (ImageView) view.findViewById(R.id.gif_anim);
        try {
            gifFromRaw = new GifDrawable(getResources(), getArguments().getInt("source"));
            gifFromRaw.setSpeed(getArguments().getFloat("speed"));
            gifFromRaw.addAnimationListener(this);
            gifView.setImageDrawable(gifFromRaw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startAnim();
        return dialog;
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public void onDestroy() {
        stopAnim();
        if (gifFromRaw != null) {
            gifFromRaw = null;
        }
        super.onDestroy();
    }

    private void stopAnim() {
        if (tipOne != null) {
            tipOne.clearAnimation();
        }
        if (tipTwo != null) {
            tipTwo.clearAnimation();
            tipTwo.setText("");
        }
        if (gifFromRaw != null)
            gifFromRaw.stop();
        gifView.setVisibility(View.INVISIBLE);
    }

    private void startAnim() {
        if (tipOne != null) {
            tipOne.startAnimation(aplaAnim);
        }
        if (tipTwo != null) {
            tipTwo.startAnimation(aplaAnimTwo);
            handler.sendEmptyMessageDelayed(0, 6000);
        }
        gifView.setVisibility(View.INVISIBLE);
    }

    /**
     * gif动画停止
     */
    @Override
    public void onAnimationCompleted() {
        stopAnim();
        startAnim();
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    /**
     * 第一个文字动画停止
     *
     * @param animation
     */
    @Override
    public void onAnimationEnd(Animation animation) {
        gifView.setVisibility(View.VISIBLE);
        gifView.startAnimation(aplaAnimTwo);
        gifFromRaw.start();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    class MyHandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    if (tipTwo != null) {
                        tipTwo.setText(tipTwoStr);
                        tipTwo.startAnimation(aplaAnimTwo);
                    }
                    break;
            }
            return true;
        }
    }
}
