package ru.johnlife.lifetools.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/*
 * USAGE: 
 * 	/AndroidManifest.xml	
       <service android:name="ru.johnlife.lifetools.sync.BaseAuthenticatorService" >
            <intent-filter>
                <action android:name="android.accounts.AccountAuthenticator" />
            </intent-filter>

            <meta-data
                android:name="android.accounts.AccountAuthenticator"
                android:resource="@xml/authenticator" />
        </service>

 * /odesk/res/xml/authenticator.xml
		<account-authenticator
	        xmlns:android="http://schemas.android.com/apk/res/android"
	        android:accountType="<account.type>"
	        android:icon="@drawable/ic_launcher"
	        android:smallIcon="@drawable/ic_launcher"
	        android:label="@string/app_name"/>
 */

public class FakeAuthenticatorService extends Service {
	protected static class FakeAuthenticator extends AbstractAccountAuthenticator {

		public FakeAuthenticator(Context context) {
			super(context);
		}

	    // Editing properties is not supported
	    @Override
	    public Bundle editProperties(
	            AccountAuthenticatorResponse r, String s) {
	        throw new UnsupportedOperationException();
	    }
	    
	    // Don't add additional accounts
	    @Override
	    public Bundle addAccount(
	            AccountAuthenticatorResponse r,
	            String s,
	            String s2,
	            String[] strings,
	            Bundle bundle) throws NetworkErrorException {
	        return null;
	    }
	    // Ignore attempts to confirm credentials
	    @Override
	    public Bundle confirmCredentials(
	            AccountAuthenticatorResponse r,
	            Account account,
	            Bundle bundle) throws NetworkErrorException {
	        return null;
	    }
	    // Getting an authentication token is not supported
	    @Override
	    public Bundle getAuthToken(
	            AccountAuthenticatorResponse r,
	            Account account,
	            String s,
	            Bundle bundle) throws NetworkErrorException {
	        throw new UnsupportedOperationException();
	    }
	    // Getting a label for the auth token is not supported
	    @Override
	    public String getAuthTokenLabel(String s) {
	        throw new UnsupportedOperationException();
	    }
	    // Updating user credentials is not supported
	    @Override
	    public Bundle updateCredentials(
	            AccountAuthenticatorResponse r,
	            Account account,
	            String s, Bundle bundle) throws NetworkErrorException {
	        throw new UnsupportedOperationException();
	    }
	    // Checking features for the account is not supported
	    @Override
	    public Bundle hasFeatures(
	        AccountAuthenticatorResponse r,
	        Account account, String[] strings) throws NetworkErrorException {
	        throw new UnsupportedOperationException();
	    }


	}

	
    // Instance field that stores the authenticator object
    private FakeAuthenticator mAuthenticator;
    
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new FakeAuthenticator(this);
    }
    
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
