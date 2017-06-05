package com.orvibo.homemate.common.appwidget;

import com.orvibo.homemate.event.ViewEvent;

/**
 * Created by wuliquan on 2016/6/3.
 */
public class WidgetUpdateEvent extends ViewEvent {
    /**
     * type 有两种类型 0：fresh 刷新widget显示
     *                 1：scene 点击了
     *                 2: damp 或者 插座 点击了
     *                 3：布防点击了
     *                 4：撤销安防点击了
     *                 5：关闭全部灯光
     */
    private int type;
    private int position;
    private WidgetItem widgetItem;
    public WidgetUpdateEvent(int type) {
        this(0,type);
    }

    public WidgetUpdateEvent(int position,int type){
        this.position=position;
        this.type=type;
    }

    public void setWidgetItem(WidgetItem widgetItem){
        this.widgetItem=widgetItem;
    }
    public WidgetItem getWidgetItem(){
        return widgetItem;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
