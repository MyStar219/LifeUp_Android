package com.smartgateway.app.data.model.User;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;

/**
 * Created by Terry on 29/6/16.
 */
public class ResponseError {

    public ResponseError(String detail) {
        this.detail = detail;
    }

    /**
     * detail : Successfully updated your profile/password
     */

    private String detail;

    @Expose
    private int responseCode;

    @Expose
    Throwable reason;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public static ResponseError handle(Throwable throwable) {
        ResponseError error;
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            try {
                Response response = exception.response();
                error = new Gson().fromJson(response.errorBody().string(),
                        ResponseError.class);
                error.setResponseCode(response.code());
            } catch (Exception e) {
                error = new ResponseError("");
            }
        }
        else error = new ResponseError("");
        return error;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Throwable getReason() {
        return reason;
    }

    public void setReason(Throwable reason) {
        this.reason = reason;
    }
}
