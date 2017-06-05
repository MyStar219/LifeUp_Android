package com.orvibo.homemate.device.hub;

/**
 * Created by huangqiyao on 2016/7/21 15:41.
 *
 * @version v1.10
 */
public class HubEvent {
    private int type;
    private String uid;
    /**
     *  0 ： 开始升级   1：升级结束{@link com.orvibo.homemate.model.gateway.HubConstant.Upgrade#FINISH},{@link com.orvibo.homemate.model.gateway.HubConstant.Upgrade#UPGRADING}
     */
    private int updateStatus;

    public HubEvent(String uid, int type, int updateStatus) {
        this.uid = uid;
        this.type = type;
        this.updateStatus = updateStatus;
    }

    public int getType() {
        return type;
    }

    public int getUpdateStatus() {
        return updateStatus;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public String toString() {
        return "HubEvent{" +
                "uid=" + uid +
                "type=" + type +
                ", updateStatus=" + updateStatus +
                '}';
    }
}
