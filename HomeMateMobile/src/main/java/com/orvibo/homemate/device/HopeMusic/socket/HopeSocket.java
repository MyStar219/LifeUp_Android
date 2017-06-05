package com.orvibo.homemate.device.HopeMusic.socket;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.protocol.RequestUserAgent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import cn.nbhope.smarthomelib.app.api.AccountService;
import cn.nbhope.smarthomelib.app.api.impl.AccountServiceImpl;
import cn.nbhope.smarthomelib.app.uitls.AppUItls;
import cn.nbhope.smarthomelib.app.uitls.CONST;

/**
 * @author wuliquan
 */
public class HopeSocket {
    private static final String TAG  =HopeSocket.class.getSimpleName();
    private static final int OUT_TIME = 3*1000;
    private static final int BUFFER_SIZE = 1024 * 32;
    public static final int MSG_HANDLE_SERVER_CALLBACK = 0x101;
    public static final int MSG_HANDLE_CONNECT_SUCCESS = 0x102;
    public static final int SEND_OUT_TIME              = 0x103;
    public Socket client = null;
    Context ctx;
    String mToken;
    private static final Object lockObj = new Object();
    private static Handler mHandler = null;
    public boolean flag = true;
    private static OutputStream os = null;
    private static boolean isKeep = false;
    private boolean isStop;
    private ExecutorService executor = null;
    private ConnectServerListener mConnectListener;
    private List<Request> sendedList = Collections.synchronizedList(new ArrayList());
    //消息队列
    private static LinkedBlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>(
            128);
    AccountService accountService = new AccountServiceImpl();
    List<Future<String>> resultList = new ArrayList<Future<String>>();

    public HopeSocket(Context context,String token) {
        Log.e(TAG,"HopeSocket init");
        ctx = context;
        mToken= token;
        //初始化线程池
        executor = Executors.newFixedThreadPool(2);

//        Runnable worker = new RequestWorker();
//        Runnable heart  = new HeartRunable();
//
//        executor.execute(worker);
//        executor.execute(heart);

        // 使用ExecutorService执行Callable类型的任务，并将结果保存在future变量中
        Future<String> heartFuture = executor.submit(new HeartCallable());
        Future<String> requestFuture = executor.submit(new RequestCallback());
        // 将任务执行结果存储到List中
        resultList.add(heartFuture);
        resultList.add(requestFuture);

        executor.shutdown();
    }

    public static void setOutHandler(Handler handler) {
        synchronized (lockObj) {
            mHandler = handler;
        }
    }

    public void setmToken(String token){
        mToken= token;
    }



    public void setmConnectListener(ConnectServerListener listener){
        mConnectListener = listener;
    }
    /**
     * 连接服务器
     */
    public void connect() {
        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isStop) {
                        try {
                            if (mConnectListener != null) {
                                mConnectListener.onReConnecting();
                            }
                            // 三次握手
                            if (client == null || client.isClosed()) {
                                client = new Socket(CONST.HOPE_IP, CONST.HOPE_PORT);
                                client.setReceiveBufferSize(1024 * 32);
                                client.setKeepAlive(true);
                                SendHeartWithToken(mToken);
                                isKeep = true;
                                if (mConnectListener != null) {
                                    mConnectListener.onConnected();
                                }
                                reciveData();

                            }
                        } catch (Exception e) {
                            Log.e(TAG + "error", "" + e.getMessage());
                            isKeep = false;
                            if (mConnectListener != null) {
                                mConnectListener.onDisconnected();
                            }
                            try {
                                disconnect(false);
                                Thread.sleep(3000);
                                if (mConnectListener != null) {
                                    mConnectListener.onReConnecting();
                                }
                                continue;
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                }
            }
        });
        connectThread.start();
    }

    /**
     * 关闭本地socket
     * @param isExitSocket  是否彻底退出socket
     */
    public void disconnect(boolean isExitSocket) {
        // 断开连接
        try {
            if (client != null) {
                client.close();
                client = null;
            }
            if (os != null) {
                os = null;
            }
            if(isExitSocket){
                mHandler = null;
                for(Future future:resultList){
                    future.cancel(true);
                }
            }
            isKeep = false;
            isStop=isExitSocket;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class HeartCallable implements Callable<String> {

        @Override
        public String call() throws Exception {
            while (!isStop) {
                while (isKeep) {
                    ByteBuffer buffer = AppUItls.sendPacket(accountService.heartTick());
                    try {
                        Thread.sleep(10 * 1 * 1000);
                        if (client != null && os == null && !client.isClosed()) {
                            os = client.getOutputStream();
                        }
                        os.write(buffer.array());
                    } catch (Exception e) {
                        isKeep = false;
                        reConnection();
                        Log.e(TAG, e.toString());
                    }
                }
                //如果需要重新连接则先循环休眠1秒钟，直到重新连接成功
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            return "";
        }
    }


    public class RequestCallback implements Callable<String>{

        @Override
        public String call() throws Exception {
            Request request=null;
            while (!isStop) {
                synchronized (HopeSocket.class) {
                    try {
                        request = requestQueue.take();
                        if (client != null && os == null && !client.isClosed()) {
                            os = client.getOutputStream();

                        }
                        if (request != null) {
                            if (request!= null) {
                                ByteBuffer buffer = request.getCmd();
                                os.write(buffer.array());
                            }
                        }



                    } catch (Exception e) {
                        try {
                            Log.e("e",e.toString());
                            Thread.sleep(1000);
                            continue;
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }


    /**
     * 重连机制
     */
    private void reConnection() {
        Log.e(TAG, "reConnection");
        try {
            if (client != null) {
                client.close();
                client = null;
            }
            if (os != null) {
                os = null;
            }
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    /**
     * 添加请求队列
     */
    /**
     * 添加请求到请求队列中
     *
     * @param request
     */
    public void addRequest(Request request) {
        try {
            if(request==null)
                return;
            requestQueue.put(request);
            Log.e(TAG, "responseMap:" +  "|request.size:" + requestQueue.size() + "|isKeep:" + isKeep);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 请求失败
        }
    }


    /**
     * 从服务端接收数据
     */
    private void reciveData() {
        while (!isStop) {
                try {
                    InputStream is = client.getInputStream();
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len, recvlen = 0, size = 0;
                    ByteBuffer bufferPacket = ByteBuffer.allocate(BUFFER_SIZE);
                    while ((len = is.read(buffer)) != -1) {
                        bufferPacket.put(buffer, 0, len);
                        recvlen += len;
                        if (flag) {
                            size = (((buffer[5] + 256) % 256) << 8) | ((buffer[6] + 256) % 256);
                            if (recvlen < size + 7) {
                                flag = false;
                            } else {
                                parsingData(size, bufferPacket);
                                recvlen = 0;
                            }
                        } else {
                            if (recvlen >= size + 7) {
                                parsingData(size, bufferPacket);
                                recvlen = 0;
                                flag = true;
                            }
                        }
                    }

                } catch (IOException io) {
                    try {
                        Thread.sleep(2000);
                        reciveData();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        }
    }


    /**
     * 实时接受数据
     */

    /***
     * @param size
     * @param bufferPacket
     */
    private void parsingData(int size, ByteBuffer bufferPacket) {

        byte[] resultData = new byte[size];
        bufferPacket.position(7);
        bufferPacket.get(resultData, 0, size);
        String data = new String(resultData);

        //这里监听返回的数据，如果不是心跳则认为是反馈（逻辑不严谨）
        if(!data.contentEquals("HeartTick")){
            if(sendedList.size()>0)
            sendedList.remove(0);
        }
        Log.d(TAG, "socket receive = " + data);
        Message msg = new Message();
        msg.what = MSG_HANDLE_SERVER_CALLBACK;
        msg.obj = data;
        synchronized (lockObj) {
            if (mHandler != null) {
                mHandler.sendMessage(msg);// 结果返回给UI处理
            }
        }
        bufferPacket.clear();
        bufferPacket.position(0);

    }

    /**
     * 发送带token的心跳包
     */
    private void SendHeartWithToken(String token) {
        Log.e(TAG,"Heart token is "+token);
        String heartTick = accountService.heartTick(token);
        ByteBuffer buffer = AppUItls.sendPacket(heartTick);
        Request request = new Request(buffer,"heard",0);
        addRequest(request);
    }


   public interface ConnectServerListener{
       void onReConnecting();
       void onConnected();
       void onDisconnected();
   }
}