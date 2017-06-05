package com.orvibo.homemate.user;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;


/**
 * @创建者： yuwei
 * @创建时间： 2016/7/22
 * @描述： 该类主要用于将反馈图片上传到服务器
 */
public abstract class UploadFeedBackIcon {
    private static final String TAG = UploadFeedBackIcon.class.getSimpleName();

    public void startUploadFeedBackIcon(String source, String userName, String sessionId,String picType, File file, String md5){
        LogUtil.d(TAG, "startModifyAccountIcon-source:"+source+" userName:"+userName+" md5:" +md5);
        String url = "http://homemate.orvibo.com/uploadFeedbackPic";
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(20 * 1000);// 超时时间10s

        RequestParams params = new RequestParams();
        params.put("Content-Type", "multipart/form-data");
        params.put("source", source);
        params.put("userName", userName);
        params.put("sessionId", sessionId);
        params.put("picType", picType);

        try {
            params.put("data", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        params.put("md5", md5);

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                LogUtil.i(TAG, response);
                if (response.length() == 0) {
                    onUploadFeedBackIconResult("",ErrorCode.FAIL,"");
                    return;
                }
                JSONObject jsonData = null;
                try {
                    jsonData = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String picId = "";
                int errorCode = ErrorCode.FAIL;
                String errorMessage = "";
                if (jsonData != null) {
                    try {
                        errorCode = jsonData.getInt("errorCode");
                        picId = jsonData.getString("picId");
                        errorMessage = jsonData.getString("errorMessage");
                        LogUtil.i(TAG, errorMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                onUploadFeedBackIconResult(picId,errorCode,errorMessage);
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2,
                                  Throwable arg3) {
                LogUtil.i(TAG, "接口访问失败，statusCode为：" + arg0);
                onUploadFeedBackIconResult("", ErrorCode.ERROR_CONNECT_SERVER_FAIL,"");
            }
        });
    }


    /**
     * 回调方法
     * @param picId 上传成功后的图片编码
     * @param errorCode 错误码
     * @param errorMessage 错误描述
     */
    public abstract void onUploadFeedBackIconResult(String picId,int errorCode,String errorMessage );
}
