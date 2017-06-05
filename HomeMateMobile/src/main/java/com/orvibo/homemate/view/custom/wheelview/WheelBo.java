package com.orvibo.homemate.view.custom.wheelview;

import java.io.Serializable;

/**
 * Created by huangqiyao on 2015/4/14.
 */
public class WheelBo implements Serializable {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "WheelBo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
