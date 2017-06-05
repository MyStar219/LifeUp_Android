package ru.johnlife.lifetools.ui.listener;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;

public abstract class AnimationFinishedListener implements AnimatorListener {

	@Override
	public void onAnimationCancel(Animator animation) {}

	@Override
	public void onAnimationRepeat(Animator animation) {}

	@Override
	public void onAnimationStart(Animator animation) {}

}
