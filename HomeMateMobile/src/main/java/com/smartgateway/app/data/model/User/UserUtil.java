package com.smartgateway.app.data.model.User;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.smartgateway.app.R;
import com.smartgateway.app.Utils.Constants;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.LaunchInfo;
import com.smartgateway.app.data.model.Provider;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class UserUtil {

    public static void loadUserPic(ImageView imageView, Context context) {
        if (context == null || imageView == null) {
            Log.e("UserUtil", "can't load user pic image: context or image view is empty");
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        String image = preferences.getString(Constants.USER_PIC_IMAGE, "");

        if (!TextUtils.isEmpty(image)) {
            Log.d("UserUtil", "load image for url: " + image);
            Picasso.with(context).load(image).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.family).into(imageView);
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.family));
        }
    }

    public static void saveUser(Context context, User user) {
        saveUser(context, user, true);
        saveFeedback(context, user.getNotification());
    }

    public static void saveUser(Context context, User user, boolean saveCredentials) {
        if (context == null) {
            Log.e("UserUtil", "can't save user, context is null");
        }
        if (user == null) {
            Log.e("UserUtil", "can't save user, user is null");
        }

        SharedPreferences.Editor editor = getUserEditor(context);
        editor.putString(Constants.USER_NAME, user.getUser().getName());
        editor.putString(Constants.USER_MOBILE, user.getUser().getMobile());
        editor.putString(Constants.USER_TOKEN, user.getUser().getToken());
        editor.putString(Constants.USER_EMAIL, user.getUser().getEmail());
        editor.putString(Constants.USER_HOME, user.getUser().getDefault_condo());
        if (saveCredentials) {
            editor.putString(Constants.USER_CREDENTIALS_JSON, new Gson().toJson(user.getCredentials()));
        }

        String image = user.getUser().getImage_url();
        editor.putString(Constants.USER_PIC_IMAGE, image);
        editor.apply();
        OneSignal.sendTag("user", user.getUser().getMobile());

    }

    public static void saveFeedback(Context context, User.Notification notification) {
        SharedPreferences.Editor editor = getNotificateionEditor(context);

        editor.putBoolean(Constants.NOTIFICAION_FAMILY, notification.getFamily());
        editor.putBoolean(Constants.NOTIFICAION_TRANSFER, notification.getTransfer());
        editor.putBoolean(Constants.NOTIFICAION_MAINTENANCE_FEES, notification.getMaintenance_fees());
        editor.putBoolean(Constants.NOTIFICAION_HISTORY, notification.getFamily());
        editor.putInt(Constants.NOTIFICAION_BOOKING, notification.getBooking());

        editor.putInt(Constants.NOTIFICAION_FEEDBACK, notification.getFeedback());
        editor.putInt(Constants.NOTIFICAION_SYSTEM, notification.getSystem());
        editor.putInt(Constants.NOTIFICAION_MAINTENANCE, notification.getMaintenance());
        editor.putInt(Constants.NOTIFICAION_CONDO, notification.getCondo());

        editor.apply();
    }

    public static User.Notification loadFeedback(Context context) {
        User.Notification notification = new User.Notification();
        SharedPreferences pref = context.getSharedPreferences(Constants.NOTIFICAION_DATA, Context.MODE_PRIVATE);
        notification.setFamily(pref.getBoolean(Constants.NOTIFICAION_FAMILY, notification.getFamily()));
        notification.setTransfer(pref.getBoolean(Constants.NOTIFICAION_TRANSFER, notification.getTransfer()));
        notification.setMaintenance_fees(pref.getBoolean(Constants.NOTIFICAION_MAINTENANCE_FEES, notification.getMaintenance_fees()));
        notification.setHistory(pref.getBoolean(Constants.NOTIFICAION_HISTORY, notification.getFamily()));
        notification.setBooking(pref.getInt(Constants.NOTIFICAION_BOOKING, notification.getBooking()));

        notification.setFeedback(pref.getInt(Constants.NOTIFICAION_FEEDBACK, notification.getFeedback()));
        notification.setSystem(pref.getInt(Constants.NOTIFICAION_SYSTEM, notification.getSystem()));
        notification.setMaintenance(pref.getInt(Constants.NOTIFICAION_MAINTENANCE, notification.getMaintenance()));
        notification.setCondo(pref.getInt(Constants.NOTIFICAION_CONDO, notification.getCondo()));

        return notification;
    }

    public static void saveLaunchInfo(Context context, LaunchInfo launchInfo) {
        SharedPreferences.Editor editor = getUrlsEditor(context);
        editor.putString(Constants.ABOUT_URL, launchInfo.getUrls().getAbout_url());
        editor.putString(Constants.AGREEMENT_URL, launchInfo.getUrls().getAgreement_url());
        editor.putString(Constants.PRIVACY_URL, launchInfo.getUrls().getPrivacy_url());
        editor.putString(Constants.TERMS_URL, launchInfo.getUrls().getTerms_url());
        editor.putString(Constants.FINDCAR_URL, launchInfo.getUrls().getFindcar_url());
        editor.putString(Constants.CARPLATE_URL, launchInfo.getUrls().getCarplate_url());
        editor.putString(Constants.NEEDHELP_URL, launchInfo.getUrls().getNeedhelp_url());
        editor.commit();
        saveCredentials(context, launchInfo.getCredentials());
        Log.d("UrlsStorage", "links saved");
    }

    private static SharedPreferences.Editor getUrlsEditor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.URLS,
                Context.MODE_PRIVATE);
        return preferences.edit();
    }

    private static SharedPreferences.Editor getUserEditor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        return preferences.edit();
    }

    private static SharedPreferences.Editor getNotificateionEditor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DATA,
                Context.MODE_PRIVATE);
        return preferences.edit();
    }

    public static Provider getProviderInfo(Context context, String providerName) {
        Credentials credentials = getCredentials(context);
        if (credentials == null) return null;

        return credentials.getProvider(providerName);
    }

    @Nullable
    public static Credentials getCredentials(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DATA,
                                                                     Context.MODE_PRIVATE);
        String credentialsJson = preferences.getString(Constants.USER_CREDENTIALS_JSON, "");
        Credentials credentials = new Gson().fromJson(credentialsJson, Credentials.class);
        if (credentials == null) {
            Log.e("UserUtils", "credentials is null");
            return null;
        }
        return credentials;
    }

    public static void saveCredentials(Context context, Credentials credentials) {
        if (credentials == null) {
            Log.e("UserUtils", "credentials to save is null");
        }
        SharedPreferences.Editor editor = getUserEditor(context);
        editor.putString(Constants.USER_CREDENTIALS_JSON, new Gson().toJson(credentials));
        editor.commit();
    }

}
