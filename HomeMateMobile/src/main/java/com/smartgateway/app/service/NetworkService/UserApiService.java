package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.Booking.PaymentDetail;
import com.smartgateway.app.data.model.Detail;
import com.smartgateway.app.data.model.SGWalletDetail;
import com.smartgateway.app.data.model.LaunchInfo;
import com.smartgateway.app.data.model.User.RegistrationCode;
import com.smartgateway.app.data.model.User.User;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Observable;

/**
 * UserApiService
 * Created by Terry on 28/6/16.
 */
public interface UserApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("launch/config")
    Observable<LaunchInfo> config(@Header("Authorization") String token,
                                  @Query("lat") double lat,
                                  @Query("lng") double lng);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("user/login")
    Observable<User> login(@Field("username") String username,
                           @Field("password") String password);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("user/register")
    Observable<RegistrationCode> register(@Field("name") String name,
                                          @Field("mobile") String mobile,
                                          @Field("password") String password);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("user/verify")
    Observable<User> verifyRegister(@Field("registration_code") String registration_code,
                                    @Field("phone_code") String phone_code);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("user/verify")
    Observable<Void> forgotPassword(@Field("registration_code") String registration_code,
                                    @Field("phone_code") String phone_code,
                                    @Field("username") String username,
                                    @Field("password") String password);


    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("user/forgot")
    Observable<RegistrationCode> forgot(@Field("username") String username);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @PUT("user/update")
    Observable<Detail> changePassword(@Header("Authorization") String token,
                                      @Field("current_password") String currentPassword,
                                      @Field("password") String password);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @PUT("user/update")
    Observable<User> updateProfile(@Header("Authorization") String token,
                                     @Field("name") String name,
                                     @Field("email") String email,
                                     @Field("image") String image);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("user/logout")
    Observable<Detail> logout(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @PUT("user/image_upload")
    Observable<Detail> uploadImage(@Header("Authorization") String token,
                                   @Field("image_data") String imageString);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @POST("account/wallet/balance")
    Observable<SGWalletDetail> getSGWallet(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @PUT("account/booking/payment")
    Observable<PaymentDetail> getPaymentStatus(@Header("Authorization") String token,
                                               @Field("booking_id") int id);
}
