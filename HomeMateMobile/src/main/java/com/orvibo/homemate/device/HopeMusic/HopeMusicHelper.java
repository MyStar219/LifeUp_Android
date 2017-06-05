package com.orvibo.homemate.device.HopeMusic;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Account;
import com.orvibo.homemate.dao.AccountDao;
import com.orvibo.homemate.data.ErrorCode;
import com.orvibo.homemate.device.HopeMusic.socket.HopeSocket;
import com.orvibo.homemate.device.HopeMusic.socket.Request;
import com.orvibo.homemate.model.DeviceBind;
import com.orvibo.homemate.model.DeviceUnbind;
import com.orvibo.homemate.sharedPreferences.UserCache;
import com.orvibo.homemate.util.StringUtil;
import com.orvibo.homemate.util.ToastUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.nbhope.smarthomelib.app.api.AccountService;
import cn.nbhope.smarthomelib.app.api.DeviceInfoService;
import cn.nbhope.smarthomelib.app.api.impl.AccountServiceImpl;
import cn.nbhope.smarthomelib.app.api.impl.DeviceInfoServiceImpl;
import cn.nbhope.smarthomelib.app.type.AppCommandType;
import cn.nbhope.smarthomelib.app.uitls.AppUItls;
import cn.nbhope.smarthomelib.app.uitls.CONST;
import cn.nbhope.smarthomelib.app.uitls.HttpRequest;
import cn.nbhope.smarthomelib.app.uitls.ResponseHandler;

/**
 * Created by wuliquan on 2016/6/17.
 */
public class HopeMusicHelper {
    private String TAG = HopeMusicHelper.class.getSimpleName();
    private Context context;
    private HopeSocket socketThread;

    private LoginHopeServerListener loginHopeServerListener;
    private BindHopeMusicListener bindHopeMusicListener;
    private AddHopeMusciListener addHopeMusciListener;
    private static String APPKEY="238D9B9F618947BA8F75945511068E2A";
    private static String SECRET="489E7BC73B6448059FF4808F02D2F265";

    public DeviceInfoService deviceInfoService = new DeviceInfoServiceImpl();
    public AccountService accountService = new AccountServiceImpl();

    public HopeMusicHelper(Context context) {
        this.context = context;
    }

    public void setLoginHopeServerListener(LoginHopeServerListener listener){
        this.loginHopeServerListener = listener;
    }
    public void setBindHopeMusicListener(BindHopeMusicListener listener){
        this.bindHopeMusicListener=listener;
    }

    public void setAddHopeMusciListener(AddHopeMusciListener listener){
        this.addHopeMusciListener=listener;
    }

    public void initState(String deviceId,String token){
        String cmd = deviceInfoService.initState(deviceId,token);
        commondSend(cmd);
    }


    //释放资源
    public void release(){
        loginHopeServerListener=null;
        bindHopeMusicListener=null;
        addHopeMusciListener=null;
        stopSocket();
    }
    /***
     * 发送命令
     *
     * @param sendstr
     */
    public void commondSend(String sendstr) {
        if(sendstr==null){
            return;
        }
        ByteBuffer buffer = AppUItls.sendPacket(sendstr);
        Request request = new Request(buffer,"normal",System.currentTimeMillis());
        if(socketThread!=null) {
            socketThread.addRequest(request);
        }
    }

    public void startSocket(Context context,String mToken) {
        if(socketThread==null) {
            socketThread = new HopeSocket(context, mToken);
            socketThread.connect();
        }else{
            socketThread.setmToken(mToken);
            socketThread.disconnect(false);

        }
    }

    public void setSocketThreadHandler(Handler handler){
        if(socketThread!=null){
            socketThread.setOutHandler(handler);
        }
    }

    public void setConnectServerListener(HopeSocket.ConnectServerListener listener){
        if(socketThread!=null&&listener!=null){
            socketThread.setmConnectListener(listener);
        }
    }
    public void stopSocket() {
        if (socketThread != null) {
            socketThread.disconnect(true);
            socketThread = null;
        }
    }


    public void loginHopeServer() {
        String json = accountService.getServerTime();
        HttpRequest.GetRequest().Post(CONST.HOPE_HTTP_URL, json, new ResponseHandler() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                if(loginHopeServerListener!=null){
                    loginHopeServerListener.loginFail(arg0.getMessage());
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {

                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(arg0.result);
                JsonObject rootJson = root.getAsJsonObject();
                JsonElement cmdElement = rootJson.get("Cmd");
                String cmd = cmdElement.getAsString();
                if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_GETSERVERTIME)) {
                    JsonElement DataElement = rootJson.get("Data");
                    if (DataElement != null) {
                        JsonObject asJsonObject = DataElement.getAsJsonObject();
                        String str_timeJson = asJsonObject.get("Time").getAsString();
                        if (str_timeJson != null) {
                            String userName = UserCache.getCurrentUserName(context);
                            Account account = new AccountDao().selMainAccountdByUserName(userName);
                            if(account!=null) {
                                String phone = account.getPhone();
                                if (!StringUtil.isEmpty(phone)) {
                                    verifyUser(accountService.verifyExternalUser(phone, APPKEY, SECRET, str_timeJson));
                                } else {
                                    if (loginHopeServerListener != null) {
                                        loginHopeServerListener.loginFail(context.getString(R.string.add_hope_music_bind_phone_title));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }


    /**
     * 验证用户
     * 登录用户
     *
     * @param json
     */
    private void verifyUser(final String json) {
        HttpRequest.GetRequest().Post(CONST.HOPE_HTTP_URL, json, new ResponseHandler() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                if(loginHopeServerListener!=null){
                    loginHopeServerListener.loginFail(arg1);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(arg0.result);
                JsonObject rootJson = root.getAsJsonObject();
                Log.e("rootJson",rootJson.toString());
                JsonElement cmdElement = rootJson.get("Cmd");
                String cmd = cmdElement.getAsString();
                if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_VERIFYEXTERNALUSER)) {
                    JsonElement resultElement = rootJson.get("Result");
                    String result = resultElement.getAsString();
                    JsonElement DataElement = rootJson.get("Data");
                    if (result.equals("Success")) {
                        if (DataElement != null) {
                            JsonObject asJsonObject = DataElement.getAsJsonObject();
                            String jsonElement = asJsonObject.get("Message").getAsString();
                            String tokenElement = asJsonObject.get("Token").getAsString();
                            if(loginHopeServerListener!=null){
                                loginHopeServerListener.loginSuccess(tokenElement);
                            }

                        }
                    }
                    if (result.equals("Failed")) {
                        if (DataElement != null) {
                            JsonObject asJsonObject = DataElement.getAsJsonObject();
                            String errorMessage = asJsonObject.get("Message").getAsString();
                            ToastUtil.showToast(errorMessage);
                        }
                    }
                }
            }
        });
    }

    /**
     * 从向往服务器获取是否包含该deviceId
     * @param deviceId
     * @param listener
     */
    public void judgeIsHaveDevice(final String deviceId, final JudgeIsHaveListener listener){
        if(StringUtil.isEmpty(deviceId)||listener==null)
            return;
        String json = deviceInfoService.getDevices(1,10);
        HttpRequest.GetRequest().Post(CONST.HOPE_HTTP_URL, json, new ResponseHandler() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                if(listener!=null){
                    listener.error(arg1);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(arg0.result);
                JsonObject rootJson = root.getAsJsonObject();
                Log.e("rootJson",rootJson.toString());
                JsonElement cmdElement = rootJson.get("Cmd");
                String cmd = cmdElement.getAsString();
                if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_GETDEVICES)) {
                    JsonElement resultElement = rootJson.get("Result");
                    String result = resultElement.getAsString();
                    JsonElement DataElement = rootJson.get("Data");
                    if (result.equals("Success")) {
                        if (DataElement != null) {
                           JsonObject data = DataElement.getAsJsonObject();
                           JsonElement devcieListElement =  data.get("DeviceList");
                            if(devcieListElement!=null){
                                boolean isHave =false;
                                JsonArray jsonArray = devcieListElement.getAsJsonArray();
                                int size = jsonArray.size();
                                for(int i=0;i<size;i++){
                                    JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                                    JsonElement id = jsonObject.get("Id");
                                    if(id.getAsString().equals(deviceId)){
                                        isHave=true;
                                        break;
                                    }
                                    else {
                                        isHave=false;
                                    }
                                }
                                if(listener!=null){
                                    listener.isHave(isHave);
                                }
                            }

                        }
                    }

                    if (result.equals("Failed")) {
                        if (DataElement != null) {
                            JsonObject data = DataElement.getAsJsonObject();
                            JsonElement  messageElement = data.get("Message");
                            try {
                                JsonElement codeElement   = data.get("Code");
                                if(codeElement.getAsString().equals(MusicConstant.ERROR_CODE[5])){
                                    if(listener!=null){
                                        listener.isHave(false);
                                        return;
                                    }
                                }
                            }catch (Exception e){
                                if(messageElement.getAsString().equals(MusicConstant.NO_DATA)){
                                    if(listener!=null){
                                        listener.isHave(false);
                                    }
                                }
                            }
                           if(messageElement.getAsString().equals(MusicConstant.NO_PERMISSION)){
                                if(listener!=null){
                                    listener.error(MusicConstant.ERROR_RELOGIN);
                                }
                            }


                        }
                    }
                }
            }
        });

    }

    public void getHopeIsOnlineList(final GetHopeOnlineListener listener){
        //这里初步设置最大为100
        int size = 100;

        String json = deviceInfoService.getDevices(1,size);
        HttpRequest.GetRequest().Post(CONST.HOPE_HTTP_URL, json, new ResponseHandler() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                if(listener!=null){
                    listener.error(arg1);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(arg0.result);
                JsonObject rootJson = root.getAsJsonObject();
                Log.e("rootJson",rootJson.toString());
                JsonElement cmdElement = rootJson.get("Cmd");
                String cmd = cmdElement.getAsString();
                if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_GETDEVICES)) {
                    JsonElement resultElement = rootJson.get("Result");
                    String result = resultElement.getAsString();
                    JsonElement DataElement = rootJson.get("Data");
                    List<HashMap<String,String>> OnlineList = new ArrayList<>();
                    if (result.equals("Success")) {
                        if (DataElement != null) {
                            JsonObject data = DataElement.getAsJsonObject();
                            JsonElement devcieListElement =  data.get("DeviceList");
                            if(devcieListElement!=null){
                                JsonArray jsonArray = devcieListElement.getAsJsonArray();
                                int size = jsonArray.size();
                                for(int i=0;i<size;i++){
                                    JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                                    JsonElement id = jsonObject.get("Id");
                                    JsonElement uid = jsonObject.get("DeviceGUID");
                                    JsonElement IsOnline = jsonObject.get("IsOnline");
                                    HashMap<String,String> hashMap = new HashMap<String, String>();
                                    hashMap.put("uid",uid.getAsString());
                                    hashMap.put("deviceId",id.getAsString());
                                    hashMap.put("IsOnline",IsOnline.getAsString());
                                    OnlineList.add(hashMap);
                                }
                                if(listener!=null){
                                    listener.callBackOnlineList(OnlineList);
                                }

                            }

                        }
                    }

                    else if (result.equals("Failed")) {
                        if (DataElement != null) {
                            JsonObject data = DataElement.getAsJsonObject();
                            JsonElement  messageElement = data.get("Message");
                            if(messageElement.getAsString().equals(MusicConstant.NO_DATA)){
                                if(listener!=null){
                                    listener.callBackOnlineList(OnlineList);
                                }
                            }else if(messageElement.getAsString().equals(MusicConstant.NO_PERMISSION)){
                                if(listener!=null){
                                    listener.error(MusicConstant.ERROR_RELOGIN);
                                }
                            }


                        }
                    }
                }
            }
        });
    }

    /**
     * 绑定设备
     * @param qrcode
     */
    public void bindDevice(final String qrcode) {
        String json =deviceInfoService.addDevice(qrcode);
        HttpRequest.GetRequest().Post(CONST.HOPE_HTTP_URL, json, new ResponseHandler() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                bindHopeMusicListener.bindDeviceFail(arg0.getMessage());
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                System.out.println("ResponseInfo:" + arg0.result);
                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(arg0.result);
                JsonObject rootJson = root.getAsJsonObject();
                JsonElement cmdElement = rootJson.get("Cmd");
                String cmd = cmdElement.getAsString();
                if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_ADDDEVICE)) {
                    JsonElement resultElement = rootJson.get("Result");
                    String result = resultElement.getAsString();
                    if (result.equals("Success")) {
                        JsonElement dataElement = rootJson.get("Data");
                        if (dataElement != null) {
                            JsonObject asJsonObject = dataElement.getAsJsonObject();
                            String deviceIdElement= asJsonObject.get("DeviceId").getAsString();
                                if (bindHopeMusicListener != null) {
                                    if(deviceIdElement!=null) {
                                    bindHopeMusicListener.bindDeviceSuccess(deviceIdElement);
                                }else {
                                    bindHopeMusicListener.bindDeviceFail(context.getString(R.string.binging_fail));
                                    }
                            }
                        }
                    }
                    if (result.equals("Failed")) {
                        if(bindHopeMusicListener!=null){
                            JsonElement dataElement = rootJson.get("Data");
                            String errorStr="";
                            if (dataElement != null) {
                                JsonObject asJsonObject = dataElement.getAsJsonObject();
                                errorStr = asJsonObject.get("Message").getAsString();
                                boolean selfAdd = asJsonObject.get("SelfAdd").getAsBoolean();
                                if (selfAdd) {
                                    String deviceId = asJsonObject.get("DeviceId").getAsString();
                                    bindHopeMusicListener.bindDeviceSuccess(deviceId);
                                } else {
                                    bindHopeMusicListener.bindDeviceFail(context.getString(R.string.add_ys_device_added));
                                }
                            } else {
                                bindHopeMusicListener.bindDeviceFail(context.getString(R.string.binging_fail));
                            }
                        }

                    }
                }
            }
        });
    }

    /**
     * 删除设备
     * @param deviceId
     */
    public void deleteDevice(final String deviceId,final DeleteHopeMusciListener listener) {
        String json =deviceInfoService.getDeleteDeviceCmdDeviceId(deviceId);
        HttpRequest.GetRequest().Post(CONST.HOPE_HTTP_URL, json, new ResponseHandler() {
            @Override
            public void onFailure(HttpException arg0, String arg1) {
                listener.DeleteDeviceFail(arg0.getMessage());
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {
                System.out.println("ResponseInfo:" + arg0.result);
                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(arg0.result);
                JsonObject rootJson = root.getAsJsonObject();
                JsonElement cmdElement = rootJson.get("Cmd");
                String cmd = cmdElement.getAsString();
                if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_DELETEDEVICE)) {
                    JsonElement resultElement = rootJson.get("Result");
                    String result = resultElement.getAsString();
                    if (result.equals("Success")) {
                        listener.DeleteDeviceSuccess("");
                    }
                    if (result.equals("Failed")) {
                        listener.DeleteDeviceFail("");
                    }
                }
            }
        });
    }

    public void startBind(String uid,final String deviceId) {
        final DeviceBind deviceBind = new DeviceBind() {
            @Override
            public void onBindResult(final String uid, int serial, int result) {
                if (result == ErrorCode.SUCCESS) {
                    if(addHopeMusciListener!=null){
                        if (result== ErrorCode.SUCCESS){
                            addHopeMusciListener.addDeviceSuccess(uid);
                        }
                    }
                }
            }
        };

        DeviceUnbind deviceUnbind = new DeviceUnbind() {
            @Override
            public void onUnbindResult(final String uid, int serial, int result) {
                        deviceBind.bindHopeMusic(context,uid,deviceId);


                }

        };
        deviceUnbind.unBind(context,uid);


    }




    public interface LoginHopeServerListener{
        void loginSuccess(String token);
        void loginFail(String msg);
    }
    public interface BindHopeMusicListener{
        void bindDeviceSuccess(String deviceIdElement);
        void bindDeviceFail(String error);
    }

    public interface AddHopeMusciListener{
        void addDeviceSuccess(String uid);
        void addDeviceFail();
    }
    public interface DeleteHopeMusciListener{
        void DeleteDeviceSuccess(String uid);
        void DeleteDeviceFail(String msg);
    }

    public interface JudgeIsHaveListener{
        void isHave(boolean is);
        void error(String error);
    }

    public interface GetHopeOnlineListener{
        void callBackOnlineList(List<HashMap<String,String>> list);
        void error(String error);
    }


}
