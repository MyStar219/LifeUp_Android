package com.orvibo.homemate.device.allone2.epg;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yuwei on 2016/4/18.
 */
public class CatItemDataHasChannelName implements Serializable{
    private int categoryid;
    //节目类别
    private String catTitle;
    //节目列表
    private List<PairProgramHasChannelName> singleDataList;


    public String getCatTitle() {
        return catTitle;
    }

    public void setCatTitle(String catTitle) {
        this.catTitle = catTitle;
    }

    public List<PairProgramHasChannelName> getSingleDataList() {
        return singleDataList;
    }

    public void setSingleDataList(List<PairProgramHasChannelName> singleDataList) {
        this.singleDataList = singleDataList;
    }

    public int getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(int categoryid) {
        this.categoryid = categoryid;
    }
}
