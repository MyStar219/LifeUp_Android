package com.orvibo.homemate.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ListView;

import com.orvibo.homemate.bo.Floor;
import com.orvibo.homemate.bo.Room;
import com.orvibo.homemate.room.adapter.FirstSetFloorListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author smagret
 * 
 *         带删除动画的ListView
 */
public class DeleteAnimListView extends ListView {
	private Context mContext;
	static final int ANIMATION_DURATION = 200;

	public DeleteAnimListView(Context context) {
		super(context);
		mContext = context;
	}

	public DeleteAnimListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(attrs);
	}

	public DeleteAnimListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(attrs);
	}

	private void init(AttributeSet attrs) {

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public void deleteCell(
			final ArrayList<HashMap<Floor, List<Room>>> floorsList,
			final FirstSetFloorListAdapter firstSetFloorListAdapter, final ListView listView,final int index) {
		AnimationListener al = new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				floorsList.remove(index);

				firstSetFloorListAdapter.notifyDataSetChanged();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		};

		View view = listView.getChildAt(index);
		collapse(view, al);
	}

	private void collapse(final View v, AnimationListener al) {
		final int initialHeight = v.getMeasuredHeight();

		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				if (interpolatedTime == 1) {
					v.setVisibility(View.GONE);
				} else {
					v.getLayoutParams().height = initialHeight
							- (int) (initialHeight * interpolatedTime);
					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		if (al != null) {
			anim.setAnimationListener(al);
		}
		anim.setDuration(ANIMATION_DURATION);
		v.startAnimation(anim);
	}

}
