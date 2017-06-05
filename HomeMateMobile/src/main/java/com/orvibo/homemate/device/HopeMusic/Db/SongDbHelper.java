package com.orvibo.homemate.device.HopeMusic.Db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wuliquan on 2016/6/28.
 */
public class SongDbHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "song.db";

    private final Context context;
    private static String TABLE_NAME="song_table";
    private static SongDbHelper deviceDescDBHelper;

    public static final String LOCK = "lock";


    public static SongDbHelper getInstance(Context context) {
        if (deviceDescDBHelper == null) {
            deviceDescDBHelper = new SongDbHelper(context);
        }
        return deviceDescDBHelper;
    }

    private SongDbHelper(Context context) {
        //必须通过super调用父类当中的构造函数
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer stringBuffer = new StringBuffer();
        db.execSQL(widgetSQL(stringBuffer));
    }

    /**
     * {"id":"112","position":0,"duration":245428,"title":"荷塘月色","album":"2011十大发烧名声","artist":"群星","img":""}
     * @param stringBuffer
     * @return
     */
    private String widgetSQL(StringBuffer stringBuffer) {
        setEmpty(stringBuffer);
        addHead(stringBuffer, TABLE_NAME)
                .append("songId integer PRIMARY KEY autoincrement, ")// 主机自增长，app不做任何处理，备用
                .append("userId text, ") //用户Id
                .append("deviceId text, ")//设备Id
                .append("id text, ")     //歌曲Id
                .append("position text, ")
                .append("duration text, ")
                .append("title text, ")
                .append("album text, ")
                .append("artist text, ")
                .append("img text, ");
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
