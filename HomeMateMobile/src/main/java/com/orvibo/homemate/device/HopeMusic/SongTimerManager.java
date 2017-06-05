package com.orvibo.homemate.device.HopeMusic;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wuliquan on 2016/5/16.
 */
public class SongTimerManager {
    private boolean isPause;
    private int currentProgress;
    private long totalProgress;
    private OnPlayProgress onPlayProgress;
    private final int TIME_PROGRESS = 0x001;
    private volatile static SongTimerManager instance;
    private final Handler handler = new Handler(){
             public void handleMessage(Message msg) {
                   switch (msg.what) {
                       case TIME_PROGRESS:
                              if (onPlayProgress!=null){
                                  currentProgress++;
                                  if(currentProgress>=totalProgress){
                                      isPause=true;
                                      onPlayProgress.playFinsh();
                                  }else {
                                      onPlayProgress.playProgress(currentProgress);
                                      if (!isPause){
                                          handler.sendEmptyMessageDelayed(TIME_PROGRESS,1000);
                                      }

                                  }
                              }
                           break;
                            }
                       super.handleMessage(msg);
                   }
            };
    private SongTimerManager() {
    }
    public static SongTimerManager getInstance() {
        if(instance == null) {
            synchronized (SongTimerManager.class) {
                if(instance == null) {
                    instance = new SongTimerManager();
                }
            }
        }
        return instance;
    }

     //释放资源
    public void release(){
           if(handler!=null){
               handler.removeCallbacksAndMessages(null);
           }

    }
    public void setOnPlayProgress(OnPlayProgress onPlayProgress){
        this.onPlayProgress=onPlayProgress;
    }
    public void setcurrentProgress(int currentProgress){
        this.currentProgress=currentProgress;
    }
    public void setTotalProgress(int totalProgress){

        this.totalProgress=totalProgress;
    }
    public void initPlay(){
        isPause=true;
        handler.sendEmptyMessage(TIME_PROGRESS);
    }

    public void reStart(){
        if(isPause) {
            isPause = false;
            handler.sendEmptyMessage(TIME_PROGRESS);
        }
    }
    public void pausePlay(){
        isPause = true;
    }

    public void stopPlay(){
        currentProgress=0;
    }


    /**
     *
     */
    public interface OnPlayProgress{
        void playProgress(int progress);
        void playFinsh();
}
}
