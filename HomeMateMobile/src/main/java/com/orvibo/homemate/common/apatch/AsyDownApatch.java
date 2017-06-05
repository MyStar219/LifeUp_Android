package com.orvibo.homemate.common.apatch;

import android.content.Context;
import android.os.AsyncTask;

import com.orvibo.homemate.common.apatch.apatchinterface.OnDownPatchListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wu on 2016/5/6.
 */
public class AsyDownApatch {
    private Context context;
    private String url;
    /* 目录 */
    private String rootDie;
    /* 输出文件名称 */
    private String outFileName;
   //从网络获取补丁的回调接口
    private OnDownPatchListener onDownPatchListener;

    public AsyDownApatch(Context context, String url, String rootDie, String outFileName) {
        this.context = context;
        this.url = url;
        this.rootDie = rootDie;
        this.outFileName = outFileName;
    }
    public void setOnDownPatchListener(OnDownPatchListener downPatchListener){
        this.onDownPatchListener = downPatchListener;
    }
    public void doGetApatch(){
        if(url!=null) {
            try {
                //对请求进行utf-8编码
                String getUrl = url;
                new LoadAsyncTask().execute(getUrl);
            }catch (Exception e){}

        }
    }
    /* 异步任务 */
    class LoadAsyncTask extends AsyncTask<String, String, String> {
        /* 后台线程 */
        @Override
        protected String doInBackground(String... params) {
            InputStream in=null;
            FileOutputStream out=null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                /* URL属性设置 */
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(PatchConfig.CONNECT_OUT_TIME);
                conn.setReadTimeout(PatchConfig.READ_OUT_TIME);
               /* URL建立连接 */
                conn.connect();
               /* 下载文件的大小 */
                int fileOfLength = conn.getContentLength();
                int length = 0;
               /* 输入流 */
                in = conn.getInputStream();

                File file=new File(rootDie + File.separator+PatchConfig.DIR + File.separator, outFileName);
                if(file.exists()){
                    file.delete();
                }
                String dir = rootDie + File.separator+PatchConfig.DIR ;
                new File(dir).mkdir();//新建文件夹
                file.createNewFile();//新建文件
                /* 输出流 */
                out = new FileOutputStream(file);
                /* 缓存模式，下载文件 */
                int buff_size=fileOfLength/1024+1;
                byte[] buff = new byte[buff_size * 1024];
                while ((length = in.read(buff)) > 0) {
                    out.write(buff, 0, length);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (onDownPatchListener!=null){
                    onDownPatchListener.downFail();
                }
            }finally{
                   /* 关闭输入输出流 */
                try {
                    if(in!=null){
                    in.close();}
                    if(out!=null){
                    out.flush();
                    out.close();}
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        /* 预处理UI线程 */
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }


        /* 结束时的UI线程 */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            onDownPatchListener.downSuccess();
        }


        /* 处理UI线程，会被多次调用,触发事件为publicProgress方法 */
        @Override
        protected void onProgressUpdate(String... values) {
        }
    }
}
