package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.Booking.Booking;
import com.smartgateway.app.data.model.Booking.BookingDetail;
import com.smartgateway.app.data.model.Booking.BookingList;
import com.smartgateway.app.data.model.Detail;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Terry on 1/7/16.
 */
public interface BookingApiService {

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("booking/book")
    Observable<Booking> book(@Field("session_ids") String sessionIDs,
                             @Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @POST("booking/confirm")
    Observable<Detail> confirm(@Field("session_ids") String sessionIDs,
                               @Field("payment_type") int paymentType,
                               @Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("booking/list")
    Observable<BookingList> list(@Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @GET("booking/detail")
    Observable<BookingDetail> detail(@Query("booking_id") int bookingId,
                                     @Header("Authorization") String token);

    @Headers("x-client-identifier: d128aacd7c300c67")
    @FormUrlEncoded
    @PUT("booking/cancel")
    Observable<Detail> cancel(@Field("booking_id") String bookingId,
                              @Header("Authorization") String token);

}
