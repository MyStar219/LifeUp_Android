package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.Facility.Facilities;
import com.smartgateway.app.data.model.Facility.Session;
import com.smartgateway.app.data.model.Facility.Type;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import rx.Observable;

/**
 * Created by Terry on 1/7/16.
 */
public interface FacilityApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("facility/type")
    Observable<Type> type(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("facilities/{facilitytype_id}")
    Observable<Facilities> facilities(@Path("facilitytype_id") int facilityTypeID,
                                      @Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("facility/session/{facility_id}/{dates}")
    Observable<Session> session(@Path("facility_id") int facilityID,
                                @Path("dates") String dates,
                                @Header("Authorization") String token);

}
