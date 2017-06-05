package ru.johnlife.lifetools.fragment;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import ru.johnlife.lifetools.activity.BaseActivity;
import ru.johnlife.lifetools.service.BaseBackgroundService;

public abstract class BaseDialogFragment extends DialogFragment {
	
	private BaseActivity baseActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof BaseActivity) {
			baseActivity = (BaseActivity)activity;
		} else {
			throw new IllegalArgumentException(
				String.format(
					"Fragment %s can be attached only to superclass of BaseActivity, but trying to attach to %s", 
					getClass().getSimpleName(), 
					activity.getClass().getSimpleName()
				)
			);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		baseActivity = null;  
	}
	
	protected void requestService(BaseBackgroundService.Requester<? extends BaseBackgroundService> requester) {
		Log.d(getClass().getSimpleName(), "Trying to request "+requester);
		if (baseActivity != null) {
			baseActivity.requestService(requester);
		} else {
			Log.e("BaseDialogFragment", "Cannot request activity "+baseActivity);
		}
	}

	protected BaseActivity getBaseActivity() {
		return baseActivity;
	}
	
}