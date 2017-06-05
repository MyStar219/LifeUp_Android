package com.orvibo.homemate.common.apatch.apatchinterface;

/**
 * Created by yu on 2016/5/6.
 */
public interface OnDownAllPatchSuccessListener {
     //这里的都成功是指处理成功，不一定从服务器能够加载到补丁文件
     void  allSuccess(String filePath);
     void  downFail();
}
