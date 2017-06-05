package ru.johnlife.lifetools.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.os.Bundle;

public class SyncableApplication extends Application {
    private static final int MINUTE = 60;
    private static final int INTERVAL = 20*MINUTE;
	// The authority for the sync adapter's content provider
    public static final String AUTHORITY = "<package>.sync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "<account.type>";
    // The account name
    public static final String ACCOUNT = "oDesk sync"; 
	private Account account;

	@Override
	public void onCreate() {
		super.onCreate();
        account = new Account(ACCOUNT, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        accountManager.addAccountExplicitly(account, null, null);
        ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), getInterval());
	}

    protected int getInterval() {
        return INTERVAL;
    }

}
