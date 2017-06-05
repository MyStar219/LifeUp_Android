package com.orvibo.homemate.user;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.ThirdAccount;
import com.orvibo.homemate.util.LogUtil;
import com.orvibo.homemate.util.ToastUtil;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 1.第三方授权
 * 2.获取用户信息
 * 3.
 * Created by allen on 2016/4/5.
 */
public class UserThirdAuth {
    private static final String TAG = UserThirdAuth.class.getName();
    private Activity context;
    private UMShareAPI mUMShareAPI;
    private ThirdAccount thirdAccount;
    private OnAuthListener onAuthListener;
    private boolean isGettingInfo = false;
    private boolean isComplete = false;

    public UserThirdAuth(Activity context, UMShareAPI mUMShareAPI) {
        this.context = context;
        this.mUMShareAPI = mUMShareAPI;
        thirdAccount = new ThirdAccount();
    }

    public void authLogin(SHARE_MEDIA platform) {
        LogUtil.d(TAG,"authLogin()-platform:" +platform);
        isGettingInfo = false;
        isComplete = false;
        mUMShareAPI.doOauthVerify(context, platform, new UMAuthListener() {
            @Override
            public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
                LogUtil.d(TAG, "onComplete()-platform:"+platform+".doOauthVerify map:" + data);
                if (mUMShareAPI.isAuthorize(context, platform)) {
                    String token = data.get("access_token");
                    if (!isGettingInfo && !TextUtils.isEmpty(token)) {
                        LogUtil.d(TAG, "oauth:" + mUMShareAPI.isAuthorize(context, platform));
                        isGettingInfo = true;
                        thirdAccount.setToken(token);
                        getUserInfo(platform);
                    }
                }
            }

            @Override
            public void onError(SHARE_MEDIA platform, int action, Throwable t) {
                ToastUtil.showToast(R.string.auth_fail);
                if (onAuthListener != null) {
                    onAuthListener.onError();
                }
            }

            @Override
            public void onCancel(SHARE_MEDIA platform, int action) {
                ToastUtil.showToast(R.string.auth_cancel);
                if (onAuthListener != null) {
                    onAuthListener.onCancel();
                }
            }
        });


    }


    private void  getUserInfo(final SHARE_MEDIA platform) {
        mUMShareAPI.getPlatformInfo(context, platform, new UMAuthListener() {
            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                Log.d(TAG, "getUserInfo map:" + map);
                if (!isComplete && map != null) {
                    String thirdId = null;
                    String thirdUserName = null;
                    String file = null;
                    if (share_media == SHARE_MEDIA.WEIXIN) {
                        thirdId = map.get("openid");
                        thirdUserName = map.get("nickname");
                        file = map.get("headimgurl");
                    } else if (share_media == SHARE_MEDIA.QQ) {
                        thirdId = map.get("openid");
                        thirdUserName = map.get("screen_name");
                        file = map.get("profile_image_url");
                    } else if (share_media == SHARE_MEDIA.SINA) {
                        try {
                            String result = map.get("result");
                            JSONObject resultObject = new JSONObject(result);
                            thirdId = resultObject.getString("idstr");
                            thirdUserName = resultObject.getString("screen_name");
                            file = resultObject.getString("profile_image_url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!TextUtils.isEmpty(thirdId)) {
                        isComplete = true;
                        thirdAccount.setThirdId(thirdId);
                        thirdAccount.setThirdUserName(thirdUserName);
                        thirdAccount.setFile(file);
                        if (onAuthListener != null) {
                            onAuthListener.onComplete(thirdAccount);
                        }
                    }
                }
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                ToastUtil.showToast(R.string.auth_fail);
                if (onAuthListener != null) {
                    onAuthListener.onError();
                }
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                ToastUtil.showToast(R.string.auth_cancel);
                if (onAuthListener != null) {
                    onAuthListener.onCancel();
                }
            }
        });
    }

    public void setOnAuthListener(OnAuthListener onAuthListener) {
        this.onAuthListener = onAuthListener;
    }

    public interface OnAuthListener {
        void onComplete(ThirdAccount thirdAccount);

        void onError();

        void onCancel();
    }

}
