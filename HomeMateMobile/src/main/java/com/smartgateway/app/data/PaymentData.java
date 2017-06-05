package com.smartgateway.app.data;

import ru.johnlife.lifetools.data.AbstractData;

/**
 * Created by yanyu on 5/14/2016.
 */
public class PaymentData extends AbstractData {
    private int iconRes;
    private String name;

    public PaymentData(int iconRes, String name) {
        this.iconRes = iconRes;
        this.name = name;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getName() {
        return name;
    }
}
