package com.orvibo.homemate.util;

import android.content.Context;
import android.text.format.DateFormat;

import com.smartgateway.app.R;
import com.orvibo.homemate.application.ViHomeApplication;
import com.orvibo.homemate.common.ViHomeProApp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by wenchao on 2015/6/6.
 * TODO 应该放到Lib Model里面，引用到的string资源也应该移到Lib Model. -- By Allen
 */
public class TimeUtil {
    private static Context context = ViHomeProApp.getContext();

    /**
     * 分钟转小时和分数组
     *
     * @param minute
     * @return
     */
    public static int[] getTimeByMin(int minute) {
        int time[] = new int[2];
        int hour = minute / 60;
        int min = minute % 60;
        time[0] = hour;
        time[1] = min;
        return time;
    }

    /*
     * 毫秒转化时分秒毫秒
     */
    public static Long[] formatTime(Long ms) {
        Long time[] = new Long[3];
        time[0] = Long.parseLong("0");
        time[1] = Long.parseLong("0");
        time[2] = Long.parseLong("0");
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;

        if (hour > 0) {
            time[0] = hour;
        }
        if (minute > 0) {
            time[1] = minute;
        }
        if (second > 0) {
            time[2] = second;
        }
        return time;
    }

    /*
 * 毫秒转化天时分秒毫秒,返回天数据
 */
    public static Long[] formatTimeDay(Long ms) {
        Long time[] = new Long[4];
        time[0] = Long.parseLong("0");
        time[1] = Long.parseLong("0");
        time[2] = Long.parseLong("0");
        time[3] = Long.parseLong("0");
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;

        if (day > 0) {
            time[0] = day;
        }
        if (hour > 0) {
            time[1] = hour;
        }
        if (minute > 0) {
            time[2] = minute;
        }
        if (second > 0) {
            time[3] = second;
        }
        return time;
    }

    /**
     * 服务端返回的触发时间是“0”时区的时间，客户端必须转换成对应时区的时间
     *
     * @param utcTime
     * @return
     */
    public static int getUtcDurationByMin(String utcTime) {
        int utcDuration = -1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            // 对于已经设定为GMT时间标准的dateFormat来说，一切需要他转换的字符串日期都是GMT标准时间，
            // 转换后返回的Date由于默认遵守系统默认时区，所以转换给Date的日期需要+8（例如北京标准时区），
            // 也就是时区与标准不同导致的时差。
//            long lightingStateTimeInMillis = dateFormat.parse(utcTime).getTime() + DateUtil.getTimeOffset();
            long lightingStateTimeInMillis = dateFormat.parse(utcTime).getTime() + DateUtil.getTimeOffset() * 1000;
            long now = System.currentTimeMillis();
            utcDuration = (int) (now - lightingStateTimeInMillis) / (60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return utcDuration;
        }
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyyMMdd
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static long getUtcTimeByMillis(String utcTime) {
        long lightingStateTimeInMillis = -1;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            // 对于已经设定为GMT时间标准的dateFormat来说，一切需要他转换的字符串日期都是GMT标准时间，转换后返回的Date由于默认遵守系统默认时区，所以转换给Date的日期需要+8（例如北京标准时区），也就是时区与标准不同导致的时差。
            lightingStateTimeInMillis = dateFormat.parse(utcTime).getTime() + DateUtil.getTimeOffset() * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return lightingStateTimeInMillis;
        }
    }

    /**
     * 获取时间
     */
    public static String getTime(Context context, int hour, int min) {
        boolean is24HourFormat = DateFormat.is24HourFormat(context);
        String h = "" + hour;
        if (hour < 10) {
            h = "0" + hour;
        }

        String m = "" + min;
        if (min < 10) {
            m = "0" + min;
        }
        String ret = "";
        if (is24HourFormat) {
            ret = h + ":" + m;
        } else {
            ret = TimeUtil.get12HourFormat(context, hour, min);
        }
        return ret;
    }

    /**
     * 获取时间
     */
    public static String getTime24(Context context, int hour, int min) {
        String h = "" + hour;
        if (hour < 10) {
            h = "0" + hour;
        }

        String m = "" + min;
        if (min < 10) {
            m = "0" + min;
        }
        return h + ":" + m;
    }

    public static String get12HourFormat(Context context, int hour, int min) {
        String ret = "";
        String apm = "";
        int fHour;
        if (hour < 12) {
            apm = context.getString(R.string.AM);
            fHour = hour;
        } else {
            apm = context.getString(R.string.PM);
            fHour = hour - 12;
        }

        String h = "" + fHour;
        if (fHour < 10) {
            h = "0" + fHour;
        }

        String m = "" + min;
        if (min < 10) {
            m = "0" + min;
        }

        if (PhoneUtil.isCN(context)) {
            if (fHour == 0) {
                ret = apm + 12 + ":" + m;
            } else {
                ret = apm + h + ":" + m;
            }
        } else {
            if (fHour == 0) {
                ret = 12 + ":" + m + " " + apm;
            } else {
                ret = h + ":" + m + " " + apm;
            }
        }


        return ret;
    }
    /**
     * 根据手机系统的时间格式
     * 把一个utc的毫秒值转化成时、分的格式
     * @param ms
     * @return
     */
    public static String millisecondToHour(long ms) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(ms);
        boolean is24HourFormat = DateFormat.is24HourFormat(ViHomeApplication.getAppContext());
        java.text.SimpleDateFormat format = null;
        if (is24HourFormat) {
            format = new java.text.SimpleDateFormat(
                    "HH:mm");
        } else {
            if (PhoneUtil.isCN(ViHomeApplication.getAppContext())) {
                format = new java.text.SimpleDateFormat(
                        "aahh:mm");
            } else {
                format = new java.text.SimpleDateFormat(
                        "hh:mm aa");
            }
        }
        String time = format.format(gc.getTime());
        if (PhoneUtil.isCN(ViHomeApplication.getAppContext())) {
            if (time.contains("AM")) {
                time = time.replaceFirst("AM", ViHomeApplication.getAppContext().getString(R.string.timing_morning));
            } else if (time.contains("PM")) {
                time = time.replaceFirst("PM", ViHomeApplication.getAppContext().getString(R.string.timing_afternoon));
            }
        }
        return time;
    }


    /**
     * 根据手机系统的时间格式
     * 把一个utc的毫秒值转化成时、分、秒的格式
     * @param ms
     * @return
     */
    public static String millisecondToForMatTime(long ms) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(ms);
        boolean is24HourFormat = DateFormat.is24HourFormat(ViHomeApplication.getAppContext());
        java.text.SimpleDateFormat format = null;
        if (is24HourFormat) {
            format = new java.text.SimpleDateFormat(
                    "HH:mm:ss");
        } else {
            if (PhoneUtil.isCN(ViHomeApplication.getAppContext())) {
                format = new java.text.SimpleDateFormat(
                        "aahh:mm:ss");
            } else {
                format = new java.text.SimpleDateFormat(
                        "hh:mm:ss aa");
            }
        }
        String time = format.format(gc.getTime());
        if (PhoneUtil.isCN(ViHomeApplication.getAppContext())) {
            if (time.contains("AM")) {
                time = time.replaceFirst("AM", ViHomeApplication.getAppContext().getString(R.string.timing_morning));
            } else if (time.contains("PM")) {
                time = time.replaceFirst("PM", ViHomeApplication.getAppContext().getString(R.string.timing_afternoon));
            }
        }
        return time;
    }
    /**
     * 根据手机系统的时间格式
     * 把一个utc的毫秒值转化成时、分、秒的格式
     * 加上夏令时的偏移(国外账号)
     * @param ms
     * @return
     */
    public static String millisecondToForMatTimeOffSet(long ms) {
        GregorianCalendar gc = new GregorianCalendar();
        Calendar cal = Calendar.getInstance();// 1、取得本地时间：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);// 3、取得夏令时差：
        ms += dstOffset;
        gc.setTimeInMillis(ms);
        boolean is24HourFormat = DateFormat.is24HourFormat(ViHomeApplication.getAppContext());
        java.text.SimpleDateFormat format = null;
        if (is24HourFormat) {
            format = new java.text.SimpleDateFormat(
                    "HH:mm:ss");
        } else {
            if (PhoneUtil.isCN(ViHomeApplication.getAppContext())) {
                format = new java.text.SimpleDateFormat(
                        "aahh:mm:ss");
            } else {
                format = new java.text.SimpleDateFormat(
                        "hh:mm:ss aa");
            }
        }
        String time = format.format(gc.getTime());
        if (PhoneUtil.isCN(ViHomeApplication.getAppContext())) {
            if (time.contains("AM")) {
                time = time.replaceFirst("AM", ViHomeApplication.getAppContext().getString(R.string.timing_morning));
            } else if (time.contains("PM")) {
                time = time.replaceFirst("PM", ViHomeApplication.getAppContext().getString(R.string.timing_afternoon));
            }
        }
        return time;
    }

    /**
     * @param ms
     * @return
     * @author Smagret
     * 12小时制
     */
    public static String millisecondToHour12(long ms) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(ms);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "hh:mm");
        return format.format(gc.getTime());
    }

    public static String millisecondToYesterdayHour(long ms) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(ms);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "HH:mm");
        return format.format(gc.getTime());
    }

    public static String millisecondToDate(long ms) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(ms);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "MM-dd HH:mm");
        return format.format(gc.getTime());
    }

    /**
     * 获取当前时间，并根据是否是同一天进行判断处理
     *
     * @param ms
     * @return
     */
    public static String getCurrentTime(long ms) {
        String md = getMd(ms);
        String hm = getHm(ms);
        if (getMd(System.currentTimeMillis()).equalsIgnoreCase(md)) {
            return hm;
        } else
            return md + " " + hm;
    }

    public static String getMd(long ms) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(ms);
        int month = mCalendar.get(Calendar.MONTH) + 1;
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        return getFormatNumber(month) + "/" + getFormatNumber(day);
    }

    /**
     * 获取月和日
     *
     * @param date 格式为yyyyMMdd
     * @return
     */
    public static String getMd(String date) {
        try {
            String[] dates = date.split("/");
            String dateStr = dates[0] + context.getString(R.string.month) + dates[1] + context.getString(R.string.day);
            return dateStr;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    public static String getHm(long ms) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(ms);
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);
        return getFormatNumber(hour) + ":" + getFormatNumber(minute);
    }

    public static String getFormatNumber(int time) {
        if (String.valueOf(time).length() == 1) {
            return "0" + time;
        }
        return String.valueOf(time);
    }

    /**
     * @param ms
     * @return 返回年份
     */
    public static String getYear(long ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("y");
        return formatter.format(ms);
    }

    /**
     * @param ms
     * @return 返回月份 如果个位数前面不加0
     */
    public static String getMonth(long ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("M");
        return formatter.format(ms);
    }

    /**
     * @param ms
     * @return 返回日期天 如果个位数前面不加0
     */
    public static String getDay(long ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("d");
        return formatter.format(ms);
    }

    /**
     * 获取计算了夏令时的偏移的消失
     * @param ms
     * @return
     */
    public static String getHourDst_OffSet(long ms) {
        Calendar cal = Calendar.getInstance();// 1、取得本地时间：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);// 3、取得夏令时差：
        ms += dstOffset;
        SimpleDateFormat formatter = new SimpleDateFormat("HH");
        return formatter.format(ms);
    }
    public static String getHour(long ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH");
        return formatter.format(ms);
    }

    public static String getMinute(long ms) {

        SimpleDateFormat formatter = new SimpleDateFormat("mm");
        return formatter.format(ms);
    }

    public static String getSecond(long ms) {

        SimpleDateFormat formatter = new SimpleDateFormat("ss");
        return formatter.format(ms);
    }

    public static String getYMD(long ms) {
        SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
        return formatter.format(ms);
    }

    /**
     * 返回星期中的一天
     *
     * @param ms
     * @return
     */
    public static String getWeekDay(long ms) {

        if (PhoneUtil.isCN(context)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(ms));
            int format = calendar.get(Calendar.DAY_OF_WEEK);
            if (format == Calendar.MONDAY) {
                return context.getString(R.string.weekday_monday);
            } else if (format == Calendar.TUESDAY) {
                return context.getString(R.string.weekday_tuesday);
            } else if (format == Calendar.WEDNESDAY) {
                return context.getString(R.string.weekday_wednesday);
            } else if (format == (Calendar.THURSDAY)) {
                return context.getString(R.string.weekday_thursday);
            } else if (format == (Calendar.FRIDAY)) {
                return context.getString(R.string.weekday_friday);
            } else if (format == (Calendar.SATURDAY)) {
                return context.getString(R.string.weekday_saturday);
            } else {
                return context.getString(R.string.weekday_sunday);
            }
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
            String format_en = simpleDateFormat.format(ms);
            return format_en;
        }

    }

    public static String millisToDateString(long ms) {
        String dateString = "";
        long oneDayMillis = 24 * 60 * 60 * 1000;
        long currentTimeMillis = System.currentTimeMillis();

        long hour = Long.parseLong(getHour(currentTimeMillis));
        long min = Long.parseLong(getMinute(currentTimeMillis));
        long sec = Long.parseLong(getSecond(currentTimeMillis));
//        MyLogger.sLog().i("hour = " + hour
//                + " min = " + min
//                + " sec = " + sec);
        long currentMillis = (hour * 60 * 60 + min * 60 + sec) * 1000;

        if (ms >= currentTimeMillis - currentMillis) {
            dateString = millisecondToHour(ms);
//        } else if (ms < (currentTimeMillis - currentMillis) && ms >= (currentTimeMillis - currentMillis - oneDayMillis)) {
//            String yesterday = ViHomeApplication.getAppContext().getResources().getString(R.string.yesterday);
//            if (PhoneUtil.isCN(context)) {
//                dateString = yesterday + " " + millisecondToHour(ms);
//            } else {
//                dateString = millisecondToHour(ms) + ", " + yesterday;
//            }
        } else if (ms < (currentTimeMillis - currentMillis - oneDayMillis)) {
            //TODO 手机是英文需要转化字符串格式
            if (PhoneUtil.isCN(context)) {
//                String month = ViHomeApplication.getAppContext().getResources().getString(R.string.month);
//                String day = ViHomeApplication.getAppContext().getResources().getString(R.string.day);
                dateString = getMonth(ms) + "/" + getDay(ms) + " " + getWeekDay(ms);
            } else {
                Date data = new Date(ms);
                String month = String.format("%tb", data);
                if (!month.equals("May")) {
                    month = month + ".";
                }
                dateString = millisecondToHour(ms) + ", " + month + " " + getDay(ms);
            }
        }
        return dateString;
    }

    /**
     * 某天的时分秒 hh:mm:ss
     *
     * @param ms
     * @return
     */
    public static String getDayofHMS(long ms) {

        return getHourDst_OffSet(ms) + ":" + getMinute(ms) + ":" + getSecond(ms);
    }


    public static String secondToDateString(int s) {

        long ms = ((long) s) * 1000;

        String dateString = "";
        long oneDayMillis = 24 * 60 * 60 * 1000;
        long currentTimeMillis = System.currentTimeMillis();//当前的utc时间

        long hour = Long.parseLong(getHour(currentTimeMillis));
        long min = Long.parseLong(getMinute(currentTimeMillis));
        long sec = Long.parseLong(getSecond(currentTimeMillis));
//        MyLogger.sLog().i("hour = " + hour
//                + " min = " + min
//                + " sec = " + sec);
        long currentMillis = (hour * 60 * 60 + min * 60 + sec) * 1000;

        if (ms >= currentTimeMillis - currentMillis) {
            dateString = millisecondToHour(ms);
        } else if (ms < (currentTimeMillis - currentMillis) && ms >= (currentTimeMillis - currentMillis - oneDayMillis)) {
            String yesterday = ViHomeApplication.getAppContext().getResources().getString(R.string.yesterday);
            if (PhoneUtil.isCN(context)) {
                dateString = yesterday + " " + millisecondToHour(ms);
            } else {
                dateString = millisecondToHour(ms) + ", " + yesterday;
            }
        } else if (ms < (currentTimeMillis - currentMillis - oneDayMillis)) {
            //TODO 手机是英文需要转化字符串格式
            if (PhoneUtil.isCN(context)) {
                String month = ViHomeApplication.getAppContext().getResources().getString(R.string.month);
                String day = ViHomeApplication.getAppContext().getResources().getString(R.string.day);
                dateString = getMonth(ms) + month + getDay(ms) + day + " " + millisecondToHour(ms);
            } else {
                Date data = new Date(ms);
                String month = String.format("%tb", data);
                if (!month.equals("May")) {
                    month = month + ".";
                }
                dateString = millisecondToHour(ms) + ", " + month + " " + getDay(ms);
            }
        }
        return dateString;
    }

    /**
     * 根据带小时和分钟的字符串获取到分钟
     *
     * @param time
     * @return
     */
    public static int getMinuteByString(String time) {
        String minuteStr = context.getString(R.string.time_minutes);
        String hourStr = context.getString(R.string.time_hours);
        String minute, hour;
        if (time.contains(minuteStr) && time.contains(hourStr)) {
            minute = time.substring(time.lastIndexOf(hourStr) + hourStr.length(), time.lastIndexOf(minuteStr));
            hour = time.substring(0, time.lastIndexOf(hourStr));
            return Integer.parseInt(hour) * 60 + Integer.parseInt(minute);
        } else if (time.endsWith(minuteStr)) {
            return Integer.parseInt(time.substring(0, time.lastIndexOf(minuteStr)));
        } else if (time.endsWith(hourStr)) {
            return Integer.parseInt(time.substring(0, time.lastIndexOf(hourStr))) * 60;
        }
        return 0;
    }

    /**
     * 时间组装
     *
     * @return
     */
    public static String getTime(int min) {
        int[] time = getTimeByMin(min);
        StringBuilder stringBuilder = new StringBuilder();
        String h = context.getString(R.string.time_hours);
        String m = context.getString(R.string.time_minutes);
        int hour = time[0];
        int minute = time[1];
        if (hour == 0) {
            stringBuilder.append(minute).append(m);
        } else if (minute == 0) {
            stringBuilder.append(hour).append(h);
        } else {
            stringBuilder.append(hour).append(h).append(minute).append(m);
        }
        return stringBuilder.toString();
    }

    /**
     * 时间组装
     *
     * @param hour
     * @param minute
     * @return
     */
    public static String getTime(int hour, int minute) {
        StringBuilder stringBuilder = new StringBuilder();
        String h = context.getString(R.string.time_hours);
        String m = context.getString(R.string.time_minutes);
        if (hour == 0) {
            stringBuilder.append(minute).append(m);
        } else if (minute == 0) {
            stringBuilder.append(hour).append(h);
        } else {
            stringBuilder.append(hour).append(h).append(minute).append(m);
        }
        return stringBuilder.toString();
    }

    /**
     * 时间组装
     *
     * @return
     */
    public static String getTime(int time[]) {
        StringBuilder stringBuilder = new StringBuilder();
        String h = context.getString(R.string.time_hours);
        String m = context.getString(R.string.time_minutes);
        int hour = time[0];
        int minute = time[1];
        if (hour == 0) {
            stringBuilder.append(minute).append(m);
        } else if (minute == 0) {
            stringBuilder.append(hour).append(h);
        } else {
            stringBuilder.append(hour).append(h).append(minute).append(m);
        }
        return stringBuilder.toString();
    }

    /**
     * @param context
     * @param seconds 总秒数
     * @return 返回格式 xx分xx秒后/xx秒后
     */
    public static String getDelayTimeTip(Context context, int seconds) {
        if (context == null) {
            return "Error";
        }
        int minute = seconds / 60;
        int second = seconds % 60;
        if (seconds < 60) {
            String tip = context.getString(R.string.scene_delay_time_second);
            return String.format(tip, seconds);
        } else {
            String tip2 = context.getString(R.string.scene_delay_time_minute_second);
            return String.format(tip2, minute, second);
        }
    }

    /**
     * @param context
     * @param seconds 总秒数
     * @return 返回格式 xx时xx分后/xx秒后
     */
    public static String getDelayTimeTip2(Context context, int seconds) {
        if (context == null) {
            return "Error";
        }
        int hour = seconds / 3600;
        int minute = (seconds - hour * 3600) / 60;
        int sec = seconds - hour * 3600 - minute * 60;
        if (seconds < 60) {
            String tip = context.getString(R.string.scene_delay_time_second);
            return String.format(tip, seconds);
        } else {
            String tip2 = context.getString(R.string.scene_delay_time_hour_minute_second);
            return String.format(tip2, hour, minute,sec);
        }
    }

    /**
     * 获取当前时间今天的分钟数
     *
     * @param ms
     * @return
     */
    public static int getSecondTime(long ms) {
        return Integer.parseInt(getHour(ms)) * 60 + Integer.parseInt(getMinute(ms));
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date   字符串日期
     * @param format 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long date2TimeStamp(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取现在时间并兼容12小时制
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate1(long ms) {
        Date currentTime = new Date(ms);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        String hourTime = millisecondToHour(ms);
        return dateString + " " + hourTime;
//        return dateString;
    }

    /**
     * 转换为“今天”、“明天”、“周二”、“下周二”
     *
     * @param context
     * @param dateString
     * @param format
     * @return
     */
    public static String getDateFormatString(Context context, String dateString, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dateFormat = DateUtil.getTimingDateFormat(calendar);
            if (dateFormat == 0) {
                dateString = context.getString(R.string.today);
            } else if (dateFormat == 1) {
                dateString = context.getString(R.string.tomorrow);
            } else {
                Calendar now = Calendar.getInstance();
                if (calendar.getTimeInMillis() < now.getTimeInMillis() + 7 * 24 * 60 * 60 * 1000) {
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                    if (dayOfWeek == 0) {
                        dayOfWeek = 7;
                    }
                    int nowDayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;
                    if (nowDayOfWeek == 0) {
                        nowDayOfWeek = 7;
                    }
                    String[] weeks = context.getResources().getStringArray(R.array.weeks_new);
                    if (dayOfWeek >= nowDayOfWeek) {
                        dateString = weeks[dayOfWeek - 1];
                    } else {
                        dateString = context.getString(R.string.next_week) + weeks[dayOfWeek - 1];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;
    }

    /**
     * @param dateString
     * @param format
     * @return 一个字节有8位，最高位为0的时候表示执行周期为单次；最高位为1的时候，从低位到高位的前7位分别表示星期一到星期天的有效位。1表示有效、0表示无效
     */
    public static int getWeekInt(String dateString, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek == 0) {
                dayOfWeek = 7;
            }
            List<Integer> weeks = new ArrayList<>();
            weeks.add(dayOfWeek);
            return WeekUtil.getSingleSelectedWeekInt(weeks);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getDateByDayOfWeek(int dayOfWeek) {
        String dateString = null;
        Calendar calendar = Calendar.getInstance();
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (nowDayOfWeek == 0) {
            nowDayOfWeek = 7;
        }
        if (dayOfWeek == nowDayOfWeek) {
            dateString = context.getString(R.string.today);
        } else if (dayOfWeek - nowDayOfWeek == 1) {
            dateString = context.getString(R.string.tomorrow);
        } else {
            String[] weeks = context.getResources().getStringArray(R.array.weeks_new);
            if (dayOfWeek >= nowDayOfWeek) {
                dateString = weeks[dayOfWeek - 1];
            } else {
                dateString = context.getString(R.string.next_week) + weeks[dayOfWeek - 1];
            }
        }
        return dateString;
    }
}
