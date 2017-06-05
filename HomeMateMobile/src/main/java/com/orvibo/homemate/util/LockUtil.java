package com.orvibo.homemate.util;

import android.content.Context;

import com.smartgateway.app.R;
import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.common.ViHomeProApp;
import com.orvibo.homemate.core.product.ProductManage;

/**
 * Created by snown on 2016/4/25.
 *
 * @描述: 门锁辅助类
 */
public class LockUtil {
    private static Context context = ViHomeProApp.getContext();

    /**
     * 根据门锁类型获取type名称
     *
     * @param type
     * @return
     */
    public static String getLockType(int type) {
        String str = context.getString(R.string.lock_type_close);
        switch (type) {
            case 1:
                str = context.getString(R.string.lock_type_finger);
                break;
            case 2:
                str = context.getString(R.string.lock_type_pass);
                break;
            case 3:
                str = context.getString(R.string.lock_type_card);
                break;
            case 4:
                str = context.getString(R.string.lock_type_tmp_pass);
                break;
            case 5:
                str = context.getString(R.string.lock_type_machine);
                break;
            case 6:
                str = context.getString(R.string.lock_type_alarm_tip1);
                break;
            case 7:
                str = context.getString(R.string.lock_type_alarm_tip2);
                break;
            case 8:
                str = context.getString(R.string.lock_type_alarm_tip3);
                break;
            case 9:
                str = context.getString(R.string.lock_type_alarm_tip4);
                break;
            case 10:
                str = context.getString(R.string.lock_type_alarm_tip5);
                break;
            case 11:
                str = context.getString(R.string.lock_type_alarm_tip6);
                break;
            case 12:
                str = context.getString(R.string.lock_type_alarm_tip7);
                break;
            case 13:
                str = context.getString(R.string.lock_type_alarm_tip8);
                break;
            case 14:
                str = context.getString(R.string.lock_type_alarm_tip9);
                break;
            case 15:
                str = context.getString(R.string.lock_type_alarm_tip10);
                break;
            case 16:
                str = context.getString(R.string.lock_type_alarm_tip11);
                break;
            case 17:
                str = context.getString(R.string.lock_type_alarm_tip12);
                break;
            case 18:
                str = context.getString(R.string.lock_type_alarm_tip13);
                break;
        }
        return str;
    }

    /**
     * 根据门锁类型获取图片
     *
     * @param type
     * @return
     */
    public static int getLockRecordImage(int type) {
        int imageId = R.drawable.record_pic_date;
        switch (type) {
            case 1:
                imageId = R.drawable.record_pic_fingerprint;
                break;
            case 2:
                imageId = R.drawable.record_pic_password;
                break;
            case 3:
                imageId = R.drawable.record_pic_card;
                break;
            case 4:
                imageId = R.drawable.record_pic_password;
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                imageId = R.drawable.record_pic_gate_lock;
                break;
        }
        return imageId;
    }

    /**
     * 根据门锁类型获取用户类型
     *
     * @param type
     * @return
     */
    public static String getLockUserType(Device device, int type, int authorizedId) {
        String str = context.getString(R.string.lock_type_custom);
        if (ProductManage.isHTLLock(device)) {
            if (authorizedId == 0) {
                str = context.getString(R.string.lock_type_admin);
            } else if (type == 4)
                str = context.getString(R.string.lock_type_tmp);
        } else if (ProductManage.isBLLock(device)) {
            if (authorizedId == 0) {
                str = context.getString(R.string.lock_type_super_admin);
            } else if (authorizedId == 131||authorizedId == 1000) {
                str = context.getString(R.string.lock_type_tmp_phone);
            } else if (authorizedId >= 1 && authorizedId <= 10) {
                str = context.getString(R.string.lock_type_admin);
            } else if (authorizedId >= 111 && authorizedId <= 130) {
                str = context.getString(R.string.lock_type_tmp);
            }
        }
        return str;
    }


}
