package com.orvibo.homemate.view.custom.sortlistview;

import com.kookong.app.data.BrandList;

import java.util.Comparator;

/**
 * Created by yuwei on 2016/3/24.
 * 源数据排序
 */
public class PinyinComparator implements Comparator<BrandList.Brand> {

    @Override
    public int compare(BrandList.Brand o1, BrandList.Brand o2) {
        if (o1.initial.equals("@") || o2.initial.equals("#")) {
            return 1;
        } else if (o1.initial.equals("#") || o2.initial.equals("@")) {
            return -1;
        } else {
            return o1.initial.compareTo(o2.initial);
        }
    }
}
