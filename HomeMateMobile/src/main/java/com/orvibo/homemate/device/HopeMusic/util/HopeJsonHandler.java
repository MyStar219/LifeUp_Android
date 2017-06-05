package com.orvibo.homemate.device.HopeMusic.util;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.orvibo.homemate.device.HopeMusic.MusicConstant;

import java.util.ArrayList;

import cn.nbhope.smarthomelib.app.enity.DevicePlayState;
import cn.nbhope.smarthomelib.app.enity.Music;
import cn.nbhope.smarthomelib.app.enity.NoticeList;
import cn.nbhope.smarthomelib.app.type.AppCommandType;
import cn.nbhope.smarthomelib.app.type.HopeCommandType;


public class HopeJsonHandler {

    public static final int MSG_HANDLE_INIT_STATE = 0x200;
    public static final int MSG_HANDLE_CHANGE_SOURCE = 0x201;
    public static final int MSG_HANDLE_CHANGE_VOICE_EFFECT = 0x202;

    public static final int MSG_HANDLE_CHANGE_PLAY_MODE = 0x204;
    public static final int MSG_HANDLE_CHANGE_PLAY_LIST = 0x205;
    public static final int MSG_HANDLE_CHANGE_VOICE = 0x207;
    public static final int MSG_SEND_CMD_INIT = 0x209;
    public static final int MSG_SEND_CMD_STOP = 0x210;
    public static final int MSG_HANDLE_ACCEPTSHARE = 0x211;
    public static final int CONTROL_FAIL           = 0x212;
    public static final int DEVICE_DISCONNECT = 0x213;
    public static final int NET_DISCONNECT = 0x214;
    public static final int MUSIC_PROGRESS = 0x215;
    public static final int NEED_RELOGIN   = 0x216;
    public static final int MSG_HANDLE_MUSIC_PAUSE= 0x217;
    public static final int MSG_HANDLE_MUSIC_PLAYEX= 0x218;
    public static final int MSG_HANDLE_MUSIC_PLAY= 0x219;

    public static final String VOICE_SET = "MusicVolumeSet";
    /**
     * 设备列表模式
     */
    public static final String DEVICE_LIST_MODE = "DEVICELIST";

    private String mOwnDeviceId;

    private Handler mHandler;

    private ArrayList<Music> mList = new ArrayList<Music>();

    private static HopeJsonHandler mHopeJsonHandler = null;
    private static Boolean mListMode = false;
    public HopeJsonHandler() {

    }
    public static HopeJsonHandler getInstance() {

        if (mHopeJsonHandler == null) {
            mHopeJsonHandler = new HopeJsonHandler();
        }
        return mHopeJsonHandler;
    }

    public String getmOwnDeviceId() {
        synchronized (this) {
            return mOwnDeviceId;
        }
    }

    public void setmOwnDeviceId(String mOwnDeviceId) {
        synchronized (this) {
            this.mOwnDeviceId = mOwnDeviceId;
            mListMode = DEVICE_LIST_MODE.equals(this.mOwnDeviceId);
        }
    }



    public Handler getHandler() {
        synchronized (this) {
            return mHandler;
        }
    }

    public void setHandler(Handler mHandler) {
        synchronized (this) {
            this.mHandler = mHandler;
        }
    }

    //释放资源
    public void release(){
        mHandler= null;
        mHopeJsonHandler = null;
        mList.clear();
        mOwnDeviceId=null;
    }
    public void responseDevice(String msg) {
        System.out.println(" msg    " + msg);
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(msg);
        JsonObject rootJson = root.getAsJsonObject();
        JsonElement cmdElement = rootJson.get("Cmd");
        String cmd = cmdElement.getAsString();
        JsonElement dataElement = rootJson.get("Data");
        Message message;
        if (dataElement != null) {
                //时时接收
                JsonObject deviceObj = dataElement.getAsJsonObject();
                JsonElement idElement = deviceObj.get("DeviceId");
                if (idElement == null)
                    return;
                String deviceID = idElement.getAsString();
                if (deviceID.equalsIgnoreCase(mOwnDeviceId) || mListMode) {
                    synchronized (this) {
                        if (mHandler != null) {
                            if (cmd.equals(AppCommandType.APPCOMMAND_TYPE_INITSTATE)) {
                                // {"Cmd":"InitState","Data":{"DeviceId":15,"Index":5,"State":2,"Mode":2,"MaxVol":15,"CurrentVol":2,"Source":1,"Effect":0}}
                                message = mHandler.obtainMessage(MSG_HANDLE_INIT_STATE);
                                Gson gson = new Gson();
                                DevicePlayState devicePlayState = gson.fromJson(dataElement, DevicePlayState.class);
                                message.obj = devicePlayState;
                                mHandler.sendMessage(message);
                            }
                            else if(cmd.equals(MusicConstant.MUSIC_PROGRESS)){
                                message = mHandler.obtainMessage(MUSIC_PROGRESS);
                                Gson gson = new Gson();
                                DevicePlayState devicePlayState = gson.fromJson(dataElement, DevicePlayState.class);
                                message.obj = devicePlayState;
                                mHandler.sendMessage(message);
                            }
                            else if (cmd.equalsIgnoreCase(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAYEX)){
                                message = mHandler.obtainMessage(MSG_HANDLE_MUSIC_PLAYEX);
                                Gson gson = new Gson();
                                DevicePlayState devicePlayState = gson.fromJson(dataElement, DevicePlayState.class);
                                message.obj = devicePlayState;
                                mHandler.sendMessage(message);
                            }
                            else if( cmd.equalsIgnoreCase(HopeCommandType.HOPECOMMAND_TYPE_MUSICPLAY)){
                                message = mHandler.obtainMessage(MSG_HANDLE_MUSIC_PLAY);
                                Gson gson = new Gson();
                                DevicePlayState devicePlayState = gson.fromJson(dataElement, DevicePlayState.class);
                                message.obj = devicePlayState;
                                mHandler.sendMessage(message);
                            }
                            else if(cmd.equalsIgnoreCase(HopeCommandType.HOPECOMMAND_TYPE_MUSICPAUSE)) {
                                message = mHandler.obtainMessage(MSG_HANDLE_MUSIC_PAUSE);
                                message.obj = rootJson;
                                mHandler.sendMessage(message);
                            }
                            else if (cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICLIST)) {
                                JsonArray musicList = deviceObj.get("MusicList").getAsJsonArray();
                                Gson gson = new Gson();
                                mList.clear();
                                mList = gson.fromJson(musicList, new TypeToken<ArrayList<Music>>() {
                                }.getType());
                                message = mHandler.obtainMessage(MSG_HANDLE_CHANGE_PLAY_LIST);
                                message.obj = mList;
                                mHandler.sendMessage(message);
                            }
                            else if (cmd.equals(VOICE_SET)) {
                                message = mHandler.obtainMessage(MSG_HANDLE_CHANGE_VOICE);
                                String volume = deviceObj.get("Volume").getAsString();
                                message.obj = volume;
                                mHandler.sendMessage(message);
                            }
                            else if (cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICCHANGEDSOURCE)) {
                                message = mHandler.obtainMessage(MSG_HANDLE_CHANGE_SOURCE);
                                String source = deviceObj.get("SourceType").getAsString();
                                message.obj = source;
                                mHandler.sendMessage(message);
                            }
                            else if (cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSIC_SOUND_EFFECT)) {
                                message = mHandler.obtainMessage(MSG_HANDLE_CHANGE_VOICE_EFFECT);
                                String effect = deviceObj.get("Effect").getAsString();
                                message.obj = effect;
                                mHandler.sendMessage(message);
                            }
                            else if (cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_MUSICCHANGEMODE)) {
                                message = mHandler.obtainMessage(MSG_HANDLE_CHANGE_PLAY_MODE);
                                String mode = deviceObj.get("Mode").getAsString();
                                message.obj = mode;
                                mHandler.sendMessage(message);
                            }
                            else if (cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_DEVICE_DISCONNECT)) {
                                message = mHandler.obtainMessage(DEVICE_DISCONNECT);
                                mHandler.sendMessage(message);
                            }

                        }
                    }
                }
        }

    }
}
