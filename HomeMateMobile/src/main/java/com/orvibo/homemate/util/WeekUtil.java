package com.orvibo.homemate.util;

import android.content.Context;
import android.widget.CheckBox;

import com.smartgateway.app.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author smagret
 */
public class WeekUtil {
//    private static final String TAG = WeekUtil.class.getSimpleName();

    /**
     * 获取星期，格式：每周一、二、。。。。
     *
     * @return
     */
    public static String getSelectedWeekString(Context context, List<Integer> weeks) {
        String selectedWeeks = "";
        Iterator<Integer> iterator = weeks.iterator();
        if (weeks.size() == 7) {
            selectedWeeks = context.getResources().getString(R.string.device_timing_repeat_everyday);
            return selectedWeeks;
        } else if (weeks.size() == 0) {
            selectedWeeks = context.getResources().getString(R.string.device_timing_repeat_never);
            return selectedWeeks;
        }

        final String space = context.getString(R.string.device_timing_repeat_splite);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.device_timing_repeat_everyweek)).append(" ");
        String[] weekArr = context.getResources()
                .getStringArray(R.array.weeks_);

        while (iterator.hasNext()) {
            Integer week = iterator.next();
            stringBuilder.append(weekArr[week - 1] + space);
        }
        selectedWeeks = stringBuilder.toString();
        if (selectedWeeks.length() > 0) {
            // 去掉最后的逗号
            return stringBuilder.toString().substring(0, selectedWeeks.length() - 1);
            //return stringBuilder.toString();
        }
        return selectedWeeks;
    }

    /**
     * 获取星期有效位
     * <p></p>
     * 一个字节有8位，最高位为0的时候表示执行周期为单次；
     * 最高位为1的时候，从低位到高位的前7位分别表示星期一到星期天的有效位。1表示有效、0表示无效
     *
     * @param weeks
     * @return
     */
    public static int getSelectedWeekInt(List<Integer> weeks) {
        int ret = -1;
        if (weeks.size() == 0) {
            ret = 0;
        } else {
            // 周期
            String[] tempArr = {"1", "0", "0", "0", "0", "0", "0", "0"};
            for (int i = 1; i < 8; i++) {
                if (weeks.contains(i)) {
                    tempArr[8 - i] = "" + 1;
                }
            }
            StringBuffer weekStr = new StringBuffer();
            for (int i = 0; i < tempArr.length; i++) {
                weekStr.append(tempArr[i]);
            }

            ret = StringUtil.binaryStringToInt(weekStr.toString());
        }
        return ret;
    }

    /**
     * 获取星期有效位
     * <p></p>
     * 一个字节有8位，最高位为0的时候表示执行周期为单次；
     * 最高位为1的时候，从低位到高位的前7位分别表示星期一到星期天的有效位。1表示有效、0表示无效
     *
     * @param weeks
     * @return
     */
    public static int getSingleSelectedWeekInt(List<Integer> weeks) {
        int ret = -1;
        if (weeks.size() == 0) {
            ret = 0;
        } else {
            // 周期
            String[] tempArr = {"0", "0", "0", "0", "0", "0", "0", "0"};
            for (int i = 1; i < 8; i++) {
                if (weeks.contains(i)) {
                    tempArr[8 - i] = "" + 1;
                }
            }
            StringBuffer weekStr = new StringBuffer();
            for (int i = 0; i < tempArr.length; i++) {
                weekStr.append(tempArr[i]);
            }

            ret = StringUtil.binaryStringToInt(weekStr.toString());
        }
        return ret;
    }

    /**
     * 获取星期，格式：每周一、二、。。。。
     *
     * @param week
     * @return
     */
    public static String getSelectedWeekString(Context context, int week) {
        // 1234567
        // final String TAG = "TimingAdapter";
        // LogUtil.d(TAG, "week=" + week);
        if (week == 255) {
            // 每天
            return context.getString(R.string.device_timing_repeat_everyday);
            // return "Everyday";
        } else if (week == 0) {
            return context.getString(R.string.device_timing_repeat_never);
        } else if (week > 255 || week < 0) {
            return context.getResources().getString(R.string.INVALID_WEEK);
        }

        String[] weekArr = context.getResources()
                .getStringArray(R.array.weeks_);

        String weekStr = StringUtil.byte2BinaryString((byte) week);
        byte[] weeks = weekStr.getBytes();
        final String space = "、";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.device_timing_repeat_everyweek));

        int len = weeks.length;
        for (int i = len - 1; i > 0; i--) {
            char w = (char) weeks[i];
            if (w == '1') {// 1表示有效
                stringBuilder.append(weekArr[7 - i] + space);
            }
        }
        String weeksStr = stringBuilder.toString();
        int strLen = weeksStr.length();
        if (strLen > 0) {
            // 去掉最后的逗号
            return stringBuilder.toString().substring(0, strLen - 1);
        } else {
            return "";
        }
    }


    /**
     * 初始化CheckBox
     *
     * @param week
     * @return
     */
    public static void initWeekCheckBoxes(Context context, int week, List<CheckBox> weekCheckBoxes) {

        if (week == 255) {
            // 每天
            Iterator<CheckBox> iterator = weekCheckBoxes.iterator();
            while (iterator.hasNext()) {
                CheckBox checkBox = iterator.next();
                checkBox.setChecked(true);
            }
            // return "Everyday";
        } else if (week == 0) {
            Iterator<CheckBox> iterator = weekCheckBoxes.iterator();
            while (iterator.hasNext()) {
                CheckBox checkBox = iterator.next();
                checkBox.setChecked(false);
            }
        } else if (week > 255 || week < 0) {
            return;
        }

        String[] weekArr = context.getResources()
                .getStringArray(R.array.weeks_);

        String weekStr = StringUtil.byte2BinaryString((byte) week);
        byte[] weeks = weekStr.getBytes();
        final String space = "、";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getString(R.string.device_timing_repeat_everyweek));

        int len = weeks.length;
        for (int i = len - 1; i > 0; i--) {
            char w = (char) weeks[i];
            if (w == '1') {// 1表示有效
                //stringBuilder.append(weekArr[7 - i] + space);
                weekCheckBoxes.get(7 - i).setChecked(true);
            }
        }

    }

    public static String getWeeks(Context context, int week) {
        // 1234567
        // final String TAG = "TimingAdapter";
        // LogUtil.d(TAG, "week=" + week);
        if (week == 159) {
            return context.getString(R.string.timing_repeat_workday);
        } else if (week == 255) {
            return context.getString(R.string.timing_repeat_everyday);
        } else if (week == 0) {
            return context.getString(R.string.timing_repeat_single);
        }

        String[] weekArr = context.getResources()
                .getStringArray(R.array.weeks_new);
        String weekStr = StringUtil.byte2BinaryString((byte) week);
        byte[] weeks = weekStr.getBytes();
        // final String temp = "week";
        final String space = " ";
        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(context.getString(R.string.device_timing_repeat_everyweek));
        // 7654321

        int len = weeks.length;
        if (len > 2 && weekStr.charAt(1) == '1') {
            stringBuilder.append(weekArr[6] + space);
        }
        // x765 4321
        // 1111 1111
        for (int i = len - 1; i > 1; i--) {
            char w = (char) weeks[i];
            if (w == '1') {
                stringBuilder.append(weekArr[7 - i] + space);
                // LogUtil.d(TAG, "" + weekArr[7 - i] + ",i=" + i);
            }
        }
        if (stringBuilder.length()<=0)
            return "";
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String weeksStr = stringBuilder.toString();
        int strLen = weeksStr.length();
        if (strLen > 0) {
            // return stringBuilder.toString().substring(0, strLen - 1);
            return stringBuilder.toString();
        } else {
            return "";
        }
    }

    public static Map<Integer, Integer> weekIntToMap(int week) {
        Map<Integer, Integer> weekMap = new HashMap<Integer, Integer>();
        if (week > 0) {
            weekMap.put(0, 1);
            String weekStr = StringUtil.byte2BinaryString((byte) week);
            byte[] weekByts = weekStr.getBytes();
            int len = weekStr.length();

            for (int i = 1; i < len; i++) {
                char w = (char) weekByts[i];
                // LogUtil.d(TAG, "weekIntToMap()-w[" + w + "]");
                if (w == '1') {
                    weekMap.put(8 - i, 8 - i);
                }
            }
        } else if (week == 0) {
            weekMap.put(0, 0);
        }


        return weekMap;
    }

    /**
     * 1-7对应周一到周六
     * @param week
     * @return
     */
    public static int getDayOfWeek(int week) {
        if (week > 0) {
            String weekStr = StringUtil.byte2BinaryString((byte) week);
            byte[] weekBytes = weekStr.getBytes();
            int len = weekStr.length();
            for (int i = 1; i < len; i++) {
                char w = (char) weekBytes[i];
                if (w == '1') {
                    return 8 - i;
                }
            }
        }
        return 0;
    }

    public static boolean[] weekIntoBoolean(int week){
        boolean[] weekbooleans = new boolean[7];
        if (week>0){
            String weekStr = StringUtil.byte2BinaryString((byte) week);
            byte[] weekByts = weekStr.getBytes();
            int len = weekStr.length();
            if (weekByts[1] == '1'){
                weekbooleans[0] = true;
            }else {
                weekbooleans[0] = false;
            }
            for (int i = 2; i < len; i++) {
                char w = (char) weekByts[i];
                if (w == '1') {
                    weekbooleans[8-i] = true;
                }else {
                    weekbooleans[8-i] = false;
                }
            }
        }
        return weekbooleans;
    }

    /**
     * 判读是否一个星期只选择了一天
     * @return
     */
    public static boolean isOnlyOneDayInWeek(int week){
        for (int i=0;i<7;i++){
            if (week == Math.pow(2,7)+Math.pow(2,i)){
                return true;
            }
        }
        return false;
    }

    /**
     * 只剩下一天，就不可再取消
     * @param week
     * @param weekCheckBoxes
     */
    public static void weekCheckChangeNotAllNull(int week, List<CheckBox> weekCheckBoxes) {
        String weekStr = StringUtil.byte2BinaryString((byte) week);
        byte[] weeks = weekStr.getBytes();
        int len = weeks.length;
        for (int i = len - 1; i > 0; i--) {
            char w = (char) weeks[i];
            if (w == '1') {// 1表示有效
                //stringBuilder.append(weekArr[7 - i] + space);
                weekCheckBoxes.get(7 - i).setClickable(false);
            }
        }

    }

    public static boolean hasEqualDayOfWeek(int week1, int week2) {
        int result = week1 & week2;
        return !(result == 0 || result == 128);
    }
}
