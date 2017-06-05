package ru.johnlife.lifetools;

import ru.johnlife.lifetools.service.BaseBackgroundService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public abstract class ClassConstantsProvider {
	public abstract Class<? extends Activity> getLoginActivityClass();
	public abstract Class<? extends BaseBackgroundService> getBackgroundServiceClass();

	public void forceLogin(Context context) {
		Intent i = new Intent(context, getLoginActivityClass());
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(i);
	}

	public Intent getBackgroundServiceIntent(Context context) {
		return new Intent(context, getBackgroundServiceClass());
	}

}
