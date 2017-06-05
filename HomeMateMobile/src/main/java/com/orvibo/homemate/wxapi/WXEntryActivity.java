package com.orvibo.homemate.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.orvibo.homemate.common.MainActivity;
import com.orvibo.homemate.data.LoginStatus;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.user.LoginActivity;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.umeng.socialize.weixin.view.WXCallbackActivity;


//import com.tencent.mm.sdk.modelbase.BaseReq;
//import com.tencent.mm.sdk.modelbase.BaseResp;
//import com.tencent.mm.sdk.openapi.IWXAPI;
//import com.tencent.mm.sdk.openapi.WXAPIFactory;


/**
 * Created by allen on 2015/10/30.
 */
public class WXEntryActivity extends WXCallbackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /* @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);

         setIntent(intent);
         api.handleIntent(intent, this);
     }*/
    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
//        Toast.makeText(this, req.getType()+"", Toast.LENGTH_LONG).show();
        Intent intent;
        String appUserName = UserCache.getCurrentUserName(this);
        int loginStatus = UserCache.getLoginStatus(this, appUserName);
        if (loginStatus == LoginStatus.SUCCESS || loginStatus == LoginStatus.FAIL) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
//        switch (req.getType()) {
//            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                goToGetMsg();
//                break;
//            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//                goToShowMsg((ShowMessageFromWX.Req) req);
//                break;
//            default:
//                break;
//        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
//        String result;
//
//        switch (resp.errCode) {
//            case BaseResp.ErrCode.ERR_OK:
//                result = "success";
//                break;
//            case BaseResp.ErrCode.ERR_USER_CANCEL:
//                result = "cancel";
//                break;
//            case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                result = "denied";
//                break;
//            default:
//                result = "unknown";
//                break;
//        }
//
//        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        try {
            super.onResp(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }



//    private void goToGetMsg() {
//        Intent intent = new Intent(this, GetFromWXActivity.class);
//        intent.putExtras(getIntent());
//        startActivity(intent);
//        finish();
//    }
//
//    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
//        WXMediaMessage wxMsg = showReq.message;
//        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
//
//        StringBuffer msg = new StringBuffer(); // 组织一个待显示的消息内容
//        msg.append("description: ");
//        msg.append(wxMsg.description);
//        msg.append("\n");
//        msg.append("extInfo: ");
//        msg.append(obj.extInfo);
//        msg.append("\n");
//        msg.append("filePath: ");
//        msg.append(obj.filePath);
//
//        Intent intent = new Intent(this, ShowFromWXActivity.class);
//        intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
//        intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
//        intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
//        startActivity(intent);
//        finish();
//    }
}
