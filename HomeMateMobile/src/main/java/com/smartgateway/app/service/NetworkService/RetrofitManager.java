package com.smartgateway.app.service.NetworkService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitManager
 * Created by Terry on 28/6/16.
 */
public class RetrofitManager {
    public static final String BASE_URL = "https://api.lifeup.com.sg/v1/";

    private static UserApiService userApiService = null;
    private static ApartmentApiService apartmentApiService = null;
    private static BookingApiService bookingApiService = null;
    private static FacilityApiService facilityApiService = null;
    private static FeedbackApiService feedbackApiService = null;
    private static MaintenanceApiService maintenanceApiService = null;
    private static AnnouncementApiService announcementApiService = null;
    private static FamilyApiService familyApiService = null;

    private static OkHttpClient client = null;
    private static Retrofit retrofit = null;

    private RetrofitManager() {
        initRetrofit();
    }

    private static void initRetrofit() {
        initOkHttpClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static void initOkHttpClient() {
        //Http log interceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        if (client == null) {
            synchronized (RetrofitManager.class) {
                if (client == null) {
                    client = new OkHttpClient.Builder()
                            .addInterceptor(interceptor)
                            .addInterceptor(new SmartGatewayOkhttpInterceptor())
                            .build();

                }
            }
        }
    }

    public static UserApiService getUserApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        userApiService = retrofit.create(UserApiService.class);
        return userApiService;
    }

    public static ApartmentApiService getApartmentApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        apartmentApiService = retrofit.create(ApartmentApiService.class);
        return apartmentApiService;
    }

    public static FacilityApiService getFacilityApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        facilityApiService = retrofit.create(FacilityApiService.class);
        return facilityApiService;
    }

    public static BookingApiService getBookingApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        bookingApiService = retrofit.create(BookingApiService.class);
        return bookingApiService;
    }

    public static FeedbackApiService getFeedbackApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        feedbackApiService = retrofit.create(FeedbackApiService.class);
        return feedbackApiService;
    }

    public static MaintenanceApiService getMaintenanceApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        maintenanceApiService = retrofit.create(MaintenanceApiService.class);
        return maintenanceApiService;
    }

    public static AnnouncementApiService getAnnouncementApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        announcementApiService = retrofit.create(AnnouncementApiService.class);
        return announcementApiService;
    }

    public static FamilyApiService getFamilyApiService() {
        if (retrofit == null) {
            initRetrofit();
        }
        familyApiService = retrofit.create(FamilyApiService.class);
        return familyApiService;
    }
}
