package com.orvibo.homemate.util;

import android.content.Context;

import com.smartgateway.app.R;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.model.UploadLocation;
import com.orvibo.homemate.model.location.LocationCity;
import com.orvibo.homemate.model.location.LocationServiceUtil;
import com.orvibo.homemate.sharedPreferences.LocationCache;
import com.orvibo.homemate.view.popup.ConfirmAndCancelPopup;

/**
 * Created by baoqi on 2016/6/22.
 */
public class ForceLocationTool {
    private static final String TAG = ForceLocationTool.class.getSimpleName();
    private Context mContext;
    private UploadLocation uploadLocation;
    private NoLocationPermissionPopup noLocationPermissionPopup;
    private LocationFailPopup locationFailPopup;
    private RequestLocation mRequestLocation;
    private String userName;
    private String userId;

    private void locationPosition(Context mAppContext, final String userName, final String userId) {
        this.userName=userName;
        this.userId=userId;
        if (noLocationPermissionPopup == null) {
            noLocationPermissionPopup = new NoLocationPermissionPopup();
        }
        if (locationFailPopup == null) {
            locationFailPopup = new LocationFailPopup();
        }
        if (mRequestLocation == null) {
            mRequestLocation = new RequestLocation();
        }
        if (uploadLocation == null) {
            initUploadLoaction();
        }
        LocationCity mLocationCity = new LocationCity(mAppContext) {

            @Override
            public void onLocation(String country, String state, String city, double latitude, double longitude, int result) {
                LogUtil.e(TAG, "onLocation()-country:" + country + ",state:" + state + ",city:" + city + ",latitude:" + latitude + ",longitude:" + longitude
                        + ",result:" + result);

                if (result == 0) {
                    String latitudeString = String.valueOf(latitude);
                    String longitudeString = String.valueOf(longitude);
                    uploadLocation.startUploadLoaction(userName, PhoneUtil.getDeviceID(mContext),
                            longitudeString, latitudeString, country, state, city, DateUtil.getTimeOffset(), DateUtil.getZoneOffset());
                } else if (result == ErrorCode.PERMISSION_POSITION_REFUSE) {
                    if (LocationCache.getTimeLag(mContext, userId)) {
                        noLocationPermissionPopup.showPopup(mContext, mContext.getResources().getString(R.string.location_permission_no_get_tips),
                                mContext.getResources().getString(R.string.location_no_permission_tips),
                                mContext.getResources().getString(R.string.to_set),
                                mContext.getResources().getString(R.string.cancel));
                    }
                } else {
                    if (LocationCache.getTimeLag(mContext, userId)) {
                        locationFailPopup.showPopup(mContext, mContext.getResources().getString(R.string.warm_tips),
                                mContext.getResources().getString(R.string.location_fail_tips),
                                mContext.getResources().getString(R.string.know), null);
                    }
                }
                LocationCache.saveUploadFlag(mContext, true, userId);
            }
        };
        mLocationCity.location();
    }

    private void initUploadLoaction() {
        uploadLocation = new UploadLocation() {
            @Override
            public void onUploadLoactionResult(int errorCode, String errorMessage) {
                LogUtil.e(TAG, "onUploadLoactionResult()-errorCode:" + errorCode + ",errorMessage:" + errorMessage);
                if (errorCode == 0) {
//                    LocationCache.saveUploadFlag(mContext, true, userId);
                }
            }
        };
    }

    private class NoLocationPermissionPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            LocationServiceUtil.gotoLocServiceSettings(mContext);
            dismiss();
        }

        public void cancel() {
            dismiss();
        }
    }

    private class LocationFailPopup extends ConfirmAndCancelPopup {
        /**
         * 点击确定按钮
         */
        public void confirm() {
            dismiss();
        }
    }

    private class RequestLocation extends ConfirmAndCancelPopup {
        @Override
        public void confirm() {
            super.confirm();
           // locationPosition();
            dismiss();

        }

        @Override
        public void cancel() {
            super.cancel();
            dismiss();
        }
    }

}
