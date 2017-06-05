package com.orvibo.homemate.device.HopeMusic.socket;

import java.nio.ByteBuffer;

/**
 * Created by wuliquan on 2016/6/27.
 */
public class Request {
    private String type;
    private ByteBuffer cmd;
    private long createTime;

    public Request(ByteBuffer cmd, String type,long createTime) {
        this.cmd = cmd;
        this.type = type;
        this.createTime = createTime;
    }

    public ByteBuffer getCmd() {
        return cmd;
    }

    public void setCmd(ByteBuffer cmd) {
        this.cmd = cmd;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
