package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.Family.FamilyList;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Terry on 7/7/16.
 */
public interface FamilyApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("family/list")
    Observable<FamilyList> familyList(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("family/remove")
    Observable<Detail> removePending(@Header("Authorization") String token,
                                     @Field("pending_family_id") int id);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("family/remove")
    Observable<Detail> remove(@Header("Authorization") String token,
                              @Field("family_id") int id);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("family/invite")
    Observable<Detail> register(@Header("Authorization") String token,
                                @Field("mobile") String mobile,
                                @Field("relationship") String relationship);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("family/activate")
    Observable<Detail> activate(@Header("Authorization") String token,
                                @Field("phone_code") int phoneCode);
}
