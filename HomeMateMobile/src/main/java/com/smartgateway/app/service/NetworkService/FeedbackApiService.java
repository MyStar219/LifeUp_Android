package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.FeedBack.FeedbackCategory;
import com.smartgateway.app.data.model.FeedBack.FeedbackDetail;
import com.smartgateway.app.data.model.FeedBack.FeedbackList;

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
public interface FeedbackApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("feedback/list")
    Observable<FeedbackList> list(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("feedback/category")
    Observable<FeedbackCategory> category(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("feedback/register")
    Observable<Detail> register(@Header("Authorization") String token,
                                @Field("item") String item,
                                @Field("category_id") String categoryId,
                                @Field("description") String desc,
                                @Field("image") String imageByte);
    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("feedback/detail")
    Observable<FeedbackDetail> detail(@Header("Authorization") String token,
                                      @Query("id") int id);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("feedback/rate")
    Observable<Detail> rate(@Header("Authorization") String token,
                            @Field("id") int id,
                            @Field("rate") String rate,
                            @Field("comment") String comment);
}
