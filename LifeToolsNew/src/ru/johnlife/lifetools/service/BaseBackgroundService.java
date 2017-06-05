package ru.johnlife.lifetools.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import ru.johnlife.lifetools.ClassConstantsProvider;
import ru.johnlife.lifetools.reporter.UpmobileExceptionReporter;

public abstract class BaseBackgroundService extends Service {
	protected static final Void[] NO_PARAMS = (Void[])null;
	// -- ServiceBinder
	public class ServiceBinder extends Binder {
		public BaseBackgroundService getService() {
			return service;
		}
	}

	// -- Requester
	public interface Requester<T extends BaseBackgroundService> {
		public void requestService(T service);
	}
	
	private ServiceBinder binder = new ServiceBinder();
	private String sessionID = null;
	private final BaseBackgroundService service = this;
	
	public BaseBackgroundService() {
		super();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(UpmobileExceptionReporter.getInstance(this));
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String sessionID = prefs.getString("SessionID", null);
		if (null != sessionID) {
			this.sessionID = sessionID;
		}
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public String getSessionId() {
		return sessionID;
	}

	public boolean isLoggedIn() {
		return sessionID != null;
	}

	protected void forceLogin() {
		getClassConstants().forceLogin(service);
	}
	
	protected abstract ClassConstantsProvider getClassConstants();

	public void logout() {
		storeSession(null);
		forceLogin();
	}

	public void storeSession(String session) {
		sessionID = session;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = prefs.edit();
		for (String key : prefs.getAll().keySet()) {
			editor.remove(key);
		}
		editor.putString("SessionID", sessionID);
		editor.apply();
	}

	protected void executeTask(AsyncTask task) {
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, NO_PARAMS);
	}


}