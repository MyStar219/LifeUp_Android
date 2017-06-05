package ru.johnlife.lifetools.sync;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;

/**
 * USAGE: 
 * 	/AndroidManifest.xml	
 
    <uses-permission android:name="android.permission.READ_SYNC_SETTINGS" />
	<uses-permission android:name="android.permission.WRITE_SYNC_SETTINGS" />
	<uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />

 	    <service android:name="ru.johnlife.lifetools.sync.FakeAuthenticatorService" >
	        <intent-filter>
	            <action android:name="android.accounts.AccountAuthenticator" />
	        </intent-filter>
	
	        <meta-data
	            android:name="android.accounts.AccountAuthenticator"
	            android:resource="@xml/authenticator" />
	    </service>
		<service
		    android:name="<T extends ru.johnlife.lifetools.sync.BaseSyncService>"
		    android:exported="true"
		    android:process=":sync" >
		    <intent-filter>
		        <action android:name="android.content.SyncAdapter" />
		    </intent-filter>
		
		    <meta-data
		        android:name="android.content.SyncAdapter"
		        android:resource="@xml/syncadapter" />
		</service>
   		<provider
            android:name="ru.johnlife.lifetools.sync.FakeProvider"
            android:authorities="<package>.sync.provider"
            android:exported="false"
            android:syncable="true"
            android:label="Whatever" />


 * /odesk/res/xml/authenticator.xml
		<account-authenticator
	        xmlns:android="http://schemas.android.com/apk/res/android"
	        android:accountType="<account.type>"
	        android:icon="@drawable/ic_launcher"
	        android:smallIcon="@drawable/ic_launcher"
	        android:label="@string/app_name"/>

 * /odesk/res/xml/syncadapter.xml
		<sync-adapter
	        xmlns:android="http://schemas.android.com/apk/res/android"
	        android:contentAuthority="<package>.sync.provider"
	        android:accountType="<account.type>"
	        android:userVisible="true"
	        android:supportsUploading="false"
	        android:allowParallelSyncs="false"
	        android:isAlwaysSyncable="true"/>
        
*/
public abstract class BaseSyncService extends Service {
	
	protected static abstract class BaseSyncAdapter extends AbstractThreadedSyncAdapter {

		public BaseSyncAdapter(Context context, boolean autoInitialize) {
			this(context, autoInitialize, false);
		}

		public BaseSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
			super(context, autoInitialize, allowParallelSyncs);
		}

		@Override
		public abstract void onPerformSync(
			Account account, 
			Bundle extras, 
			String authority, 
			ContentProviderClient provider, 
			SyncResult syncResult
		);

	}

    private static BaseSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = createSyncAdapter(getApplicationContext());
            }
        }
    }

	protected abstract BaseSyncAdapter createSyncAdapter(Context context);

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
