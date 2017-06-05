package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.Maintenance.Category;
import com.smartgateway.app.data.model.Maintenance.Maintenance;
import com.smartgateway.app.data.model.Maintenance.MaintenanceList;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Terry on 4/7/16.
 */
public interface MaintenanceApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("maintenance/list")
    Observable<MaintenanceList> list(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("maintenance/category")
    Observable<Category> category(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("maintenance/register")
    Observable<Detail> register(@Header("Authorization") String token,
                                @Field("item") String item,
                                @Field("category_id") String categoryId,
                                @Field("description") String desc,
                                @Field("image") String imageByte);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("maintenance/detail")
    Observable<Maintenance> detail(@Header("Authorization") String token,
                                   @Query("id") String id);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("maintenance/rate")
    Observable<Detail> rate(@Header("Authorization") String token,
                            @Field("id") int id,
                            @Field("rate") String rate,
                            @Field("comment") String comment);
}
