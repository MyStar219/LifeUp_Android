package com.orvibo.homemate.device.allone2.epg;

import com.hzy.tvmao.model.db.bean.ChannelInfo;
import com.kookong.app.data.ProgramData;

import java.io.Serializable;

/**
 * Created by yuwei on 2016/4/18.
 */
public class PairProgramHasChannelName implements Serializable {

    private ProgramData.PairProgram pairProgram;
    private ChannelInfo mChannelInfo;

    public ProgramData.PairProgram getPairProgram() {
        return pairProgram;
    }

    public void setPairProgram(ProgramData.PairProgram pairProgram) {
        this.pairProgram = pairProgram;
    }

    public ChannelInfo getChannelInfo() {
        return mChannelInfo;
    }

    public void setChannelInfo(ChannelInfo channelInfo) {
        mChannelInfo = channelInfo;
    }

    public boolean equals(Object o) {
        PairProgramHasChannelName pairProgramHasChannelName = (PairProgramHasChannelName) o;
        return this.mChannelInfo.equals(pairProgramHasChannelName.mChannelInfo);
    }
}
