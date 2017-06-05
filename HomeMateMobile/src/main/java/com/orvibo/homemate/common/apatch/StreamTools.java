package com.orvibo.homemate.common.apatch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wuliquan on 2016/5/8.
 */
public class StreamTools {

    /**
     * 将一个输入流转化为字符串
     */
    public static String getStreamString(InputStream tInputStream){
        if (tInputStream != null){
            try{
                BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(tInputStream));
                StringBuffer tStringBuffer = new StringBuffer();
                String sTempOneLine = new String("");
                while ((sTempOneLine = tBufferedReader.readLine()) != null){
                    tStringBuffer.append(sTempOneLine);
                }
                return tStringBuffer.toString();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }
}