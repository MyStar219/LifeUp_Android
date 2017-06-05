package com.orvibo.homemate.common.appwidget;

import java.io.Serializable;

/**
 * Created by wuliquan on 2016/6/2.
 */
public class WidgetItem implements Serializable{
    //检索的表的主键
    private String widgetId;

    private String widgetName="";
    //检索的表名
    private String tableName ="1";

    private String uid;

    private int tableId ;

    private String deviceId;
    //类型
    private String typle  ;
    //设备类别
    private int  deviceType;

    private int    isArm;

    //显示在桌面的位置下标
    private String index ="1" ;
    private int delFlag = 0;

    private int srcId;

    private String status="0";

    private String roomId="";

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getWidgetName() {
        return widgetName;
    }

    public void setWidgetName(String widgetName) {
        this.widgetName = widgetName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTyple() {
        return typle;
    }

    public void setTyple(String typle) {
        this.typle = typle;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getSrcId() {
        return srcId;
    }

    public void setSrcId(int srcId) {
        this.srcId = srcId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIsArm() {
        return isArm;
    }

    public void setIsArm(int isArm) {
        this.isArm = isArm;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String toString(){
        return "WidgetItem:"+
                "widgetName="+widgetName+
                "widgetId="+widgetId+
                ",tableName="+tableName+
                ",tableKey="+tableId+
                ",typle="+typle+
                ",isArm="+isArm+
                ",uid="+uid+
                ",index="+index;
    }
}

