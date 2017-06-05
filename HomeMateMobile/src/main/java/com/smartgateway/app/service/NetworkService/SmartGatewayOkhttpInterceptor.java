package com.smartgateway.app.service.NetworkService;

import android.util.Log;

import com.google.gson.Gson;
import com.smartgateway.app.data.model.User.ResponseError;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ru.johnlife.lifetools.event.DetailEvent;
import ru.johnlife.lifetools.event.LogoutEvent;
import ru.johnlife.lifetools.util.RxBus;

public class SmartGatewayOkhttpInterceptor implements Interceptor {

    public static final int INSUFFICIENT_FUNDS_ERROR_CODE = 428;
    public static final int INVALID_TOKEN_ERROR_CODE = 401;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        tryHandleLogout(response);
        response = tryHandleDetailsMessage(response);
        return response;
    }

    private Response tryHandleDetailsMessage(Response response) throws IOException {
        if (response.code() != INSUFFICIENT_FUNDS_ERROR_CODE &&
            response.code() != INVALID_TOKEN_ERROR_CODE) {
            ResponseBody responseBody = response.body();
            String responseString = responseBody.string().replace("\"alert\":","\"detail\":");

            if (responseString.contains("\"detail\":")) {
                Log.d("okhttp interceptor", "handle details message, show message dialog");
                ResponseError error = new Gson().fromJson(responseString, ResponseError.class);
                RxBus.getDefaultInstance().post(new DetailEvent(error.getDetail(), response.code()));
            }
            else if (responseString.length() == 0)
                RxBus.getDefaultInstance().post(new DetailEvent("", response.code()));

            // we need to construct new body, because ResponseBody can be consumed only once
            // see response body doc to reference
            ResponseBody body = ResponseBody.create(responseBody.contentType(), responseString);

            return response.newBuilder().body(body).build();
        }
        return response;
    }

    private void tryHandleLogout(Response response) {
        if (response.code() == INVALID_TOKEN_ERROR_CODE) {
            Log.d("okhttp interceptor", "401 code returned, send logout event");
            RxBus.getDefaultInstance().post(new LogoutEvent());
        }
    }

}
