package com.orvibo.homemate.user;

import android.content.Context;
import android.content.SharedPreferences;

import com.orvibo.homemate.data.Constant;
import com.orvibo.homemate.util.StringUtil;

/**
 * Created by yuwei on 2016/7/25.
 * 记录未发送的用户反馈内容
 */
public class FeedBackPreference {

    /**
     * 保存未发送的反馈内容
     * @param context
     * @param userid
     * @param tag
     * @param content
     */
    public static void saveFeedBackEditText(Context context, String userid, String tag, String content){
        if (context != null && !StringUtil.isEmpty(content)) {
            SharedPreferences sp = context.getSharedPreferences(Constant.SPF_NAME,0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(userid+"_"+tag,content);
            editor.commit();
        }
    }

    /**
     * 获取未发送的反馈内容
     * @param context
     * @param userid
     * @param tag
     * @return
     */
    public static String getFeedBackEditText(Context context,String userid,String tag){
        String content = "";
        if (context != null){
            content = context.getSharedPreferences(Constant.SPF_NAME, 0).getString(userid+"_"+tag,"");
        }
        return content;
    }

    /**
     * 清除本地记忆
     * @param context
     * @param userid
     * @param tag
     */
    public static void removeFeedBackEditText(Context context,String userid,String tag){
        if (context != null){
            SharedPreferences sp = context.getSharedPreferences(Constant.SPF_NAME,0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(userid+"_"+tag,"");
            editor.remove(userid+"_"+tag);
            editor.commit();
        }
    }

    /**
     * 保存上传图片的本地路径
     * @param context
     * @param userid
     * @param tag
     * @param path
     */
    public static void saveUploadImagePath(Context context, String userid, String tag, String path){
        if (context != null && !StringUtil.isEmpty(path)) {
            SharedPreferences sp = context.getSharedPreferences(Constant.SPF_NAME,0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(userid+"_"+tag+"_Path",path);
            editor.commit();
        }
    }

    /**
     * 获取上次未发送的上次图片路径
     * @param context
     * @param userid
     * @param tag
     * @return
     */
    public static String getUploadImagePath(Context context,String userid,String tag){
        String content = "";
        if (context != null){
            content = context.getSharedPreferences(Constant.SPF_NAME, 0).getString(userid+"_"+tag+"_Path","");
        }
        return content;
    }

    /**
     * 清除缓存的上次图片路径
     * @param context
     * @param userid
     * @param tag
     */
    public static void removeUploadImagePath(Context context,String userid,String tag){
        if (context != null){
            SharedPreferences sp = context.getSharedPreferences(Constant.SPF_NAME,0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(userid+"_"+tag+"_Path","");
            editor.remove(userid+"_"+tag+"_Path");
            editor.commit();
        }
    }

    /**
     * 保存未发送的上传图片pid
     * @param context
     * @param userid
     * @param tag
     * @param pid
     */
    public static void saveUploadImagePid(Context context, String userid, String tag, String pid){
        if (context != null && !StringUtil.isEmpty(pid)) {
            SharedPreferences sp = context.getSharedPreferences(Constant.SPF_NAME,0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(userid+"_"+tag+"_Pid",pid);
            editor.commit();
        }
    }

    /**
     * 获取未发送的上传图片Pid
     * @param context
     * @param userid
     * @param tag
     * @return
     */
    public static String getUploadImagePid(Context context,String userid,String tag){
        String content = "";
        if (context != null){
            content = context.getSharedPreferences(Constant.SPF_NAME, 0).getString(userid+"_"+tag+"_Pid","");
        }
        return content;
    }

    /**
     * 清除未发送的上传图片Pid
     * @param context
     * @param userid
     * @param tag
     */
    public static void removeUploadImagePid(Context context,String userid,String tag){
        if (context != null){
            SharedPreferences sp = context.getSharedPreferences(Constant.SPF_NAME,0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(userid+"_"+tag+"_Pid","");
            editor.remove(userid+"_"+tag+"_Pid");
            editor.commit();
        }
    }
}
