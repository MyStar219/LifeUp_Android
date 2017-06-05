package com.orvibo.homemate.user;


import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


/**
 * @创建者： yuwei
 * @创建时间： 2016/7/22
 * @描述： 发送用户反馈内容到服务器
 */
public abstract class SendFeedBackContent {
    private static final String TAG = SendFeedBackContent.class.getSimpleName();

    public void sendFeedBackContent(String source, String userName, String sessionId,String picId, String content,String contactWay,String phoneTime,String phoneZone,String phoneModel,String phoneSystemVesion,String appVesion,String nickname){
        String url = "http://homemate.orvibo.com/uploadFeedbackContent";
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(5 * 1000);// 超时时间5s

        RequestParams params = new RequestParams();
        params.put("Content-Type", "multipart/form-data");
        params.put("source", source);
        params.put("userName", userName);
        params.put("sessionId", sessionId);
        if (!TextUtils.isEmpty(picId)){
            params.put("picId", picId);//添加上传的图片编码
        }
        params.put("content", content);//反馈内容
/*        if (!TextUtils.isEmpty(contactWay)){
            params.put("contactsWay",contactWay);
        }*/
        params.put("contactsWay",TextUtils.isEmpty(contactWay)?"":contactWay);
        params.put("phoneTime", phoneTime);
        params.put("phoneZone", phoneZone);
        params.put("phoneModel", phoneModel);
        params.put("phoneSystemVesion", phoneSystemVesion);
        params.put("appVesion", appVesion);
        params.put("nickname", nickname);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                LogUtil.i(TAG, response);
                if (response.length() == 0) {
                    onUploadFeedBackResult(ErrorCode.FAIL,"");
                    return;
                }
                JSONObject jsonData = null;
                try {
                    jsonData = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int errorCode = ErrorCode.FAIL;
                String errorMessage = "";
                if (jsonData != null) {
                    try {
                        errorCode = jsonData.getInt("errorCode");
                        errorMessage = jsonData.getString("errorMessage");
                        LogUtil.i(TAG, errorMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                onUploadFeedBackResult(errorCode,errorMessage);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                LogUtil.i(TAG, "接口访问失败，statusCode为：" + arg0);
                onUploadFeedBackResult(ErrorCode.ERROR_CONNECT_SERVER_FAIL,"");
            }
        });
    }


    /**
     * 回调方法
     * @param errorCode 错误码
     * @param errorMessage 错误描述
     */
    public abstract void onUploadFeedBackResult(int errorCode,String errorMessage );
}
