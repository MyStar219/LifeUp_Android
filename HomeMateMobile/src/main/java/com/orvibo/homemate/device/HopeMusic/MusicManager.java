package com.orvibo.homemate.device.HopeMusic;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.orvibo.homemate.device.HopeMusic.Bean.DeviceSongBean;
import com.orvibo.homemate.device.HopeMusic.util.MusicUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.nbhope.smarthomelib.app.comm.Cmd;
import cn.nbhope.smarthomelib.app.type.HopeCommandType;
import cn.nbhope.smarthomelib.app.uitls.CONST;
import cn.nbhope.smarthomelib.app.uitls.HttpRequest;
import cn.nbhope.smarthomelib.app.uitls.ResponseHandler;

/**
 * Created by wuliquan on 2016/5/26.
 */
public class MusicManager {
    /**
     "Index": 10,
     "State": 2,
     "Mode": 3,
     "MaxVol": 15,
     "CurrentVol": 1,
     "Source": 1,
     "Effect": 5,
     "Progress": 0,
     */
    private String deviceId;
    private String Index;
    private String State;
    private String Mode;
    private String MaxVol;
    private String CurrentVol;
    private String Source;
    private String Effect;

    private String token;
    private DeviceSongBean deviceSongBean;

    private volatile static MusicManager instance;

    private List<OnSongListListener> listenerList = new ArrayList<OnSongListListener>();


    private MusicManager(String deviceId,String token) {
        this.token = token;
        this.deviceId = deviceId;
        deviceSongBean = new DeviceSongBean();
    }
    public static MusicManager getInstance(String deviceId, String token) {
        if(instance == null) {
            synchronized (MusicManager.class) {
                if(instance == null) {
                    instance = new MusicManager( deviceId,token);
                }
            }
        }else{
            instance.deviceId = deviceId;
            instance.token = token;
        }
        return instance;
    }

    /**
     * 释放资源
     */
    public void releaseInstance(){
        if(listenerList!=null)
           listenerList.clear();
        instance=null;
        deviceSongBean = null;
    }
    public void registerListener(OnSongListListener listener){
        if(listener!=null){
            if(listenerList.contains(listener)){
                listenerList.remove(listener);
            }
            listenerList.add(listener);
        }
    }
    public void unRegisterListener(OnSongListListener listener){
        if(listenerList.contains(listener)){
            listenerList.remove(listener);
        }
    }


    /**
     * 获取设备上的歌曲
     */
    public void getSong(String pageIndex,String pageSize) {

        HttpRequest.GetRequest().Post(CONST.HOPE_HTTP_URL, querySongList(deviceId, token,pageIndex,pageSize), new ResponseHandler() {

            @Override
            public void onFailure(HttpException arg0, String arg1) {
                if(listenerList!=null){
                    for(OnSongListListener listener:listenerList){
                        listener.onDeviceStateChange(false,null,arg1);
                    }
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> arg0) {

                JsonParser parser = new JsonParser();
                JsonElement root = parser.parse(arg0.result);
                JsonObject rootJson = root.getAsJsonObject();
                Log.e("getSongList",rootJson.toString());
                JsonElement cmdElement = rootJson.get("Cmd");
                String cmd = cmdElement.getAsString();
                if(cmd.equals(HopeCommandType.HOPECOMMAND_TYPE_GETDEVICESONGINFOS)){
                    JsonElement resultElement=rootJson.get("Result");
                    String result = resultElement.getAsString();
                    if(result.equals("Success")){
                        JsonElement DataElement = rootJson.get("Data");
                        if(DataElement!=null){
                            JsonObject asJsonObject = DataElement.getAsJsonObject();
                            DeviceSongBean bean =  MusicUtil.Jsonobject2DeviceSongBean(asJsonObject);
                            if(bean!=null){
                                if(deviceId.equals(bean.getDeviceId())){
                                    deviceSongBean.setTotal(bean.getTotal());
                                    deviceSongBean.setSongList(bean.getSongList());
                                    if(listenerList!=null){
                                        for(OnSongListListener listener:listenerList){
                                            listener.onDeviceStateChange(true,deviceSongBean,"");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else if(result.equals("Failed")){
                        if(listenerList!=null){
                            JsonElement DataElement = rootJson.get("Data");
                            JsonObject dataJson =DataElement.getAsJsonObject();
                            JsonElement dataElement =dataJson.get("Message");
                            String data = dataElement.getAsString();
                            for(OnSongListListener listener:listenerList){
                                listener.onDeviceStateChange(false,null,data);
                            }
                        }
                    }
                }
            }
        });
    }

    public String querySongList(String DeviceId, String token,String pageIndex,String pageSize) {
        Cmd cmd = new Cmd();
        Gson gson = new Gson();
        cmd.setCmd(HopeCommandType.HOPECOMMAND_TYPE_GETDEVICESONGINFOS);
        HashMap map = new HashMap();
        map.put("DeviceId", DeviceId);
        map.put("Token", token);
        map.put("AlbumName", "");
        map.put("ArtistName", "");
        map.put("SongName", "");
        map.put("PageIndex", pageIndex);
        map.put("PageSize",pageSize);
        cmd.setData(map);
        System.out.println("获取音乐列表--json = " + gson.toJson(cmd));
        return gson.toJson(cmd);
    }
    /**
     * 刷行歌曲列表
     */
    public interface OnSongListListener{
              void onDeviceStateChange(boolean result,DeviceSongBean bean,String data);
    }
}
