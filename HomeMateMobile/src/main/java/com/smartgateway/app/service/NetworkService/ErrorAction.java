package com.smartgateway.app.service.NetworkService;

import com.smartgateway.app.data.model.User.ResponseError;

import rx.functions.Action1;

/**
 * Created by Terry on 7/7/16.
 */
public abstract class ErrorAction implements Action1<Throwable> {

    @Override
    public void call(Throwable throwable) {
        ResponseError error = ResponseError.handle(throwable);
        error.setReason(throwable);
        call(error);
    }

    public abstract void call(ResponseError error);
}
