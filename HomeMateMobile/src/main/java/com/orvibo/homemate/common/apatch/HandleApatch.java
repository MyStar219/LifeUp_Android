package com.orvibo.homemate.common.apatch;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;


import com.orvibo.homemate.common.apatch.apatchinterface.OnDownAllPatchSuccessListener;
import com.orvibo.homemate.common.apatch.apatchinterface.OnDownPatchListener;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by wuliquan on 2016/5/8.
 */
public class HandleApatch {
    private String version;
    //所有的补丁处理完成后的回调接口,不保证成功下载
    private OnDownAllPatchSuccessListener onDownAllPatchSuccessListener;
    private Map<String,AsyDownApatch> hashMap = new ConcurrentHashMap<String,AsyDownApatch>();
    private String rootDie =  Environment.getExternalStorageDirectory()
            .getAbsolutePath();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //在设定的超时间里面没有下载成功,清空hashMap
            if (msg.what==1){
                if(hashMap!=null){
                    if(hashMap.size()>0){
                        if(onDownAllPatchSuccessListener!=null){
                            hashMap.clear();
                            onDownAllPatchSuccessListener.downFail();
                        }
                    }
                }
            }
        }
    };

    public HandleApatch(String version) {
        this.version = version;
    }

    public void setOnDownAllPatchSuccessListener(OnDownAllPatchSuccessListener onDownAllPatchSuccessListener){
        this.onDownAllPatchSuccessListener=onDownAllPatchSuccessListener;
    }
    public void downApatchList(final Context context) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(PatchConfig.DOWN_APATCH_LIST_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(PatchConfig.CONNECT_OUT_TIME);
                    conn.setReadTimeout(PatchConfig.READ_OUT_TIME);
                    conn.setRequestMethod("GET");
                    InputStream is = conn.getInputStream();
                    String result = StreamTools.getStreamString(is);

                    if(result!=null){
                    parseXML(context,result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 解析服务器返回来的xml数据
     * @param content
     * @return
     * @throws Exception
     */
    private  void parseXML(Context context, String content) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        ArrayList<ApatchItem> apatchList = new ArrayList<ApatchItem>();
        reader.setContentHandler(new ApathXmlContentHandler(apatchList));
        reader.parse(new InputSource(new StringReader(content)));

        //判断版本是否匹配
        if(version!=null) {
            for (ApatchItem apatchItem : apatchList) {
                //必须先判断该版本是否匹配该补丁
                if(apatchItem.isMatch(version)) {
                    final String name = apatchItem.getName();
                    String md5 = apatchItem.getMd5();
                    String url = apatchItem.getUrl();

                    if (FileUtils.isGetPatchFromHttp(context, url, name,md5)) {
                        AsyDownApatch asyDownApatch = new AsyDownApatch(context, url, rootDie, name);
                        asyDownApatch.setOnDownPatchListener(new OnDownPatchListener() {
                            @Override
                            public void downSuccess() {
                                handleHashMap(name);
                            }

                            @Override
                            public void downFail() {
                                handleHashMap(name);
                            }
                        });
                        hashMap.put(name, asyDownApatch);
                        asyDownApatch.doGetApatch();
                    }
                }
            }
        }

        //设置延时操作
        int size=apatchList.size();
        long delayTime=size*PatchConfig.READ_OUT_TIME;
        handler.sendEmptyMessageDelayed(1,delayTime);
    }

    private void handleHashMap(String name){
        if(hashMap!=null){
            if (hashMap.containsKey(name)) {
                hashMap.remove(name);
                if(hashMap.size()==0){
                    if (onDownAllPatchSuccessListener!=null){
                        onDownAllPatchSuccessListener.allSuccess(rootDie + File.separator+PatchConfig.DIR);
                    }
                }
            }
        }
    }

}
