package ru.johnlife.lifetools.auth;

import android.animation.Animator;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.johnlife.lifetools.ui.AnimationUtils;
import ru.johnlife.lifetools.ui.listener.AnimationFinishedListener;

public abstract class AbstractLoginPaneController {
    private ViewGroup mainCard;
    private int currentStep = 0;
    private View currentStepView = null;
    private boolean active = false;

    public AbstractLoginPaneController(View mainCard) {
        this.mainCard = (ViewGroup) mainCard;
    }

    public AnimationFinishedListener show(int step) {
        active = true;
        mainCard.setVisibility(View.INVISIBLE);
        currentStep = step;
        nextStep(step);
        return new AnimationFinishedListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                AnimationUtils.showUp(mainCard, null);
            }
        };
    }

    protected abstract LoginStepDescriptor[] getSteps();

    protected View initializeStep(int currentStep) {
        LoginStepDescriptor[] steps = getSteps();
        if (currentStep < 0 || currentStep >= steps.length) throw new IndexOutOfBoundsException();
        View view = inflate(steps[currentStep].layout);
        SparseArray<View.OnClickListener> listeners = steps[currentStep].listeners;
        if (null != listeners) {
            for (int i=0; i<listeners.size(); i++) {
                view.findViewById(listeners.keyAt(i)).setOnClickListener(listeners.valueAt(i));
            }
        }
        return view;
    }

    AnimationFinishedListener nextStepRunnable = new AnimationFinishedListener(){
        @Override
        public void onAnimationEnd(Animator animation) {
            mainCard.removeAllViews();
            currentStepView = initializeStep(currentStep);
            mainCard.addView(currentStepView);
        }
    };

    protected void nextStep() {
        nextStep(currentStep+1);
    }

    protected void nextStep(int step) {
        currentStep = step;
        if (null == currentStepView) {
            nextStepRunnable.onAnimationEnd(null);
        } else {
            AnimationUtils.hideUp(currentStepView, nextStepRunnable);
        }
    }

    protected final void done() {
        active = false;
        AnimationUtils.hideDown(mainCard, new AnimationFinishedListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainCard.setVisibility(View.GONE);
                onDone();
            }
        });

    }

    protected abstract void onDone();

    protected View inflate(int layout) {
        return LayoutInflater.from(mainCard.getContext()).inflate(layout, mainCard, false);
    }

    protected View findViewById(int id) {
        return mainCard.findViewById(id);
    }

    protected Context getContext() {
        return mainCard.getContext();
    }

    public boolean isActive() {
        return active;
    }

    public void goBack() {
        int backStep  = getSteps()[currentStep].back;
        if (backStep == LoginStepDescriptor.DONE) {
            done();
        } else if (backStep == LoginStepDescriptor.NO_BACK) {
            abort();
        } else {
            nextStep(backStep);
        }
    }

    protected void abort() {
        done();
    }

    public void reset() {
        currentStep = 0;
    }

    protected static class LoginStepDescriptor {
        public static class Listeners extends SparseArray<View.OnClickListener> {}
        public static final int NO_BACK = -666;
        public static final int DONE = -1;
        private int layout;
        private Listeners listeners;
        private int back;

        public LoginStepDescriptor(int layout, Listeners listeners, int stepBack) {
            this.layout = layout;
            this.listeners = listeners;
            this.back = stepBack;
        }
    }
}
