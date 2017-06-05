package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.AnnouncementData;
import com.smartgateway.app.data.model.Announcement.AnnouncementList;
import com.smartgateway.app.data.model.Maintenance.MaintenanceList;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import rx.Observable;

/**
 * Created by Terry on 4/7/16.
 */
public interface AnnouncementApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("announcement/list")
    Observable<AnnouncementList> list(@Header("Authorization") String token);
}
