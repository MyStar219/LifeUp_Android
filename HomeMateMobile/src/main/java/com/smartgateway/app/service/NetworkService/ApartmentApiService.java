package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.Apartment.Condos;
import com.smartgateway.app.data.model.Credentials;
import com.smartgateway.app.data.model.CredentialsBean;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.Apartment.Apartments;
import com.smartgateway.app.data.model.Apartment.Condo;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Terry on 28/6/16.
 */
public interface ApartmentApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("apartment/condos")
    Observable<Condos> condos(@Query("name") String name,
                              @Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("apartment/condo")
    Observable<Condo> getCondo(@Query("condo_id") int condoID,
                               @Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("apartment/add")
    Observable<CredentialsBean> add(@Field("unit_id") int unitID,
                                    @Field("condo_id") int condoID,
                                    @Header("Authorization") String token);


    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("apartment/remove")
    Observable<Detail> remove(@Field("apartment_id") String apartmentID,
                              @Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("apartment/list")
    Observable<Apartments> list(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("apartment/switch/")
    Observable<CredentialsBean> apartmentSwitch(@Field("apartment_id") int apartmentId,
                                                @Header("Authorization") String token);
}
