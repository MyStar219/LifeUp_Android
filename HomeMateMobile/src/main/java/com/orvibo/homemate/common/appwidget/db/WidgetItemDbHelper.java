package com.orvibo.homemate.common.appwidget.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by WULIQUAN on 2016/6/2.
 * 桌面Widget读取该表进行页面展示
 */
public class WidgetItemDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "widget.db";
    private static final String ASSETS_NAME = "widget.sql";

    private final Context context;
    private static String TABLE_NAME="widget_table";
    private static WidgetItemDbHelper deviceDescDBHelper;

    public static final String LOCK = "lock";


    public static WidgetItemDbHelper getInstance(Context context) {
        if (deviceDescDBHelper == null) {
            deviceDescDBHelper = new WidgetItemDbHelper(context);
        }
        return deviceDescDBHelper;
    }

    private WidgetItemDbHelper(Context context) {
        //必须通过super调用父类当中的构造函数
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer stringBuffer = new StringBuffer();
        db.execSQL(widgetSQL(stringBuffer));
    }

    private String widgetSQL(StringBuffer stringBuffer) {
        setEmpty(stringBuffer);
        addHead(stringBuffer, TABLE_NAME)
                .append("widgetId integer PRIMARY KEY autoincrement, ")// 主机自增长，app不做任何处理，备用
                .append("userId text, ")
                .append("widgetName text, ")
                .append("tableName text, ")//表名称
                .append("tableId Integer, ")//读取表的主键
                .append("typle text, ")//类别
                .append("deviceType text, ")
                .append("deviceId text, ")
                .append("uid text, ")
                .append("iconId text, ")
                .append("status text, ")
                .append("roomId text, ")
                .append("position text, ");//显示在桌面的位置下标
        addEnd(stringBuffer);
        return stringBuffer.toString();
    }

    private void setEmpty(StringBuffer stringBuffer) {
        stringBuffer.setLength(0);
    }
    /**
     * @param stringBuffer
     * @param tableName    表名称
     * @return
     */
    private StringBuffer addHead(StringBuffer stringBuffer, String tableName) {
        return stringBuffer.append("CREATE TABLE ").append(tableName)// 表名
                .append(" (");//
    }

    private StringBuffer addEnd(StringBuffer stringBuffer) {
        return stringBuffer.append("delFlag integer, createTime long, updateTime long);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE +"+TABLE_NAME+";");
        StringBuffer stringBuffer = new StringBuffer();
        db.execSQL(widgetSQL(stringBuffer));
    }

    public SQLiteDatabase getWriteDb() {
        SQLiteDatabase db = null;
            try {
                db = this.getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return db;
    }

    public static void close(SQLiteDatabase db, Cursor c) {
        if (db != null) {
            try {
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            c = null;
        }
    }

    public static void closeCursor(Cursor c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            c = null;
        }
    }

}
