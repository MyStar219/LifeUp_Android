package com.orvibo.homemate.common.apatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliquan on 2016/5/8.
 */
public class ApatchItem {
    //补丁的名称
    private String name;
    //补丁的md5
    private String md5;
    //补丁的绝对路径
    private String url;
    //补丁使用的版本号
    private List<String> versionList = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setUrl(String url){
        this.url=url;
    }
    public String getUrl(){
        return url;
    }
    public void setVersionList(String target) {
        if(target!=null){
            String[] split = target.split("#");
            int size=split.length;
            this.versionList.clear();
            for(int i=0;i<size;i++){
                this.versionList.add(split[i]);
            }
        }
    }

    public boolean isMatch(String version){
        if(versionList!=null){
            if(versionList.contains(version)){
                return true;
            }else
                return false;
        }
        return false;
    }
}