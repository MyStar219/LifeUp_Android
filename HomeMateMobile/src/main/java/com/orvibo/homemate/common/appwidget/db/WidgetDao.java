package com.orvibo.homemate.common.appwidget.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.orvibo.homemate.bo.Device;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.common.appwidget.WidgetItem;
import com.orvibo.homemate.data.DBHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wuliquan on 2016/6/2.
 */
public class WidgetDao {
    public static WidgetDao bean;
    public static volatile SQLiteDatabase sDB;
    private static String TABLE_NAME="widget_table";

    public WidgetDao(Context context) {
        initDB(context);
    }

    private  void initDB(Context context) {
        if (sDB == null) {
            sDB = WidgetItemDbHelper.getInstance(context).getWriteDb();
        }
    }

    public static void releaseDB() {
        synchronized (WidgetItemDbHelper.LOCK) {
            close();
            sDB = null;
        }
    }

    /**
     * 关闭数据库
     */
    public static void close() {
        if (sDB != null) {
            try {
                sDB.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    /**
     * 添加一条记录
     *
     * @param
     */
    public void insWidgetItem(String userid,WidgetItem widgetItem) {
        synchronized (WidgetItemDbHelper.LOCK) {
            try {
                sDB.insert(TABLE_NAME, null,
                        getContentValues(null,userid, widgetItem));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<WidgetItem> selItemByTyple(String userId, String typle){
        List<WidgetItem> widgetItems = new ArrayList<WidgetItem>();
        Cursor cursor = null;
        synchronized (WidgetItemDbHelper.LOCK) {
            try {
                cursor = sDB.rawQuery("select * from " + TABLE_NAME
                                + " where typle = '"+typle+"'"+" and userId = '"+userId+"' and delFlag = " + 0 + " order by updateTime",
                        null);
                while (cursor.moveToNext()) {

                    WidgetItem widgetItem = getWidgetItem(cursor);
                    if (widgetItem != null) {
                        widgetItems.add(widgetItem);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                WidgetItemDbHelper.closeCursor(cursor);
            }
        }
        return widgetItems;
    }
    public List<WidgetItem> selWidgetItem() {
        List<WidgetItem> widgetItems = new ArrayList<WidgetItem>();
        Cursor cursor = null;
        synchronized (WidgetItemDbHelper.LOCK) {
            try {
                cursor = sDB.rawQuery("select * from " + TABLE_NAME
                                + " where delFlag = " + 0 + " order by updateTime",
                        null);
                while (cursor.moveToNext()) {

                    WidgetItem widgetItem = getWidgetItem(cursor);
                    if (widgetItem != null) {
                        widgetItems.add(widgetItem);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                WidgetItemDbHelper.closeCursor(cursor);
            }
        }
        return widgetItems;
    }

    public void deleteAllData() {

        synchronized (DBHelper.LOCK) {
            try {
                sDB.execSQL(" DELETE FROM " + TABLE_NAME );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteDataByType(String type){
        try {
            final String[] whereArgs = {type};
            sDB.execSQL("delete from " + TABLE_NAME
                    + " where typle = ? ", whereArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDataById(String id){
        try {
            final String[] whereArgs = {id};
            sDB.execSQL("delete from " + TABLE_NAME
                    + " where widgetId = ? ", whereArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insertDevice(String userId,List<Device> devices){
        if(devices!=null){
            for(Device device:devices){
                WidgetItem widgetItem = new WidgetItem();
                widgetItem.setTyple("device");
                widgetItem.setDeviceType(device.getDeviceType());
                widgetItem.setUid(device.getUid());
                widgetItem.setWidgetName(device.getDeviceName());
                widgetItem.setDeviceId(device.getDeviceId());
                widgetItem.setRoomId(device.getRoomId());
                insWidgetItem(userId,widgetItem);
            }
        }
    }

    public void updateIcon(String iconId,String widgetId){
        try {
            final String[] whereArgs = {widgetId};
            sDB.execSQL("update  " + TABLE_NAME
                    + " set iconId = "+iconId+"  where widgetId = "+widgetId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertScene(String userId,List<Scene> scenes){
        if(scenes!=null){
            for(Scene scene:scenes){
                WidgetItem widgetItem = new WidgetItem();
                widgetItem.setWidgetName(scene.getSceneName());
                widgetItem.setTyple("scene");
                widgetItem.setUid(scene.getUid());
                widgetItem.setTableId(scene.getSceneId());
                insWidgetItem(userId,widgetItem);
            }
        }
    }
    private WidgetItem getWidgetItem(Cursor cursor) {
        WidgetItem widgetItem = new WidgetItem();

        String widgetId   = cursor.getString(cursor.getColumnIndex("widgetId"));
        String widgetName = cursor.getString(cursor.getColumnIndex("widgetName"));
        String tableName  = cursor.getString(cursor.getColumnIndex("tableName"));
        int tableId       = cursor.getInt(cursor.getColumnIndex("tableId"));
        String typle      = cursor.getString(cursor.getColumnIndex("typle"));
        int deviceType    = cursor.getInt(cursor.getColumnIndex("deviceType"));
        String index      = cursor.getString(cursor.getColumnIndex("position"));
        String uid        = cursor.getString(cursor.getColumnIndex("uid"));
        String deviceId   = cursor.getString(cursor.getColumnIndex("deviceId"));
        int iconId        = cursor.getInt(cursor.getColumnIndex("iconId"));
        String status     = cursor.getString(cursor.getColumnIndex("status"));
        String roomId     = cursor.getString(cursor.getColumnIndex("roomId"));

        widgetItem.setWidgetId(widgetId);
        widgetItem.setWidgetName(widgetName);
        widgetItem.setTableName(tableName);
        widgetItem.setTableId(tableId);
        widgetItem.setTyple(typle);
        widgetItem.setDeviceType(deviceType);
        widgetItem.setIndex(index);
        widgetItem.setUid(uid);
        widgetItem.setDeviceId(deviceId);
        widgetItem.setSrcId(iconId);
        widgetItem.setStatus(status);
        widgetItem.setRoomId(roomId);

        return widgetItem;
    }

    private ContentValues getContentValues(ContentValues cv, String userId,WidgetItem widgetItem) {
        if (cv == null) {
            cv = new ContentValues();
        } else {
            cv.clear();
        }
        cv.put("userId",userId);
        cv.put("widgetName",widgetItem.getWidgetName());
        cv.put("tableName", widgetItem.getTableName());
        cv.put("tableId", widgetItem.getTableId());
        cv.put("deviceId",widgetItem.getDeviceId());
        cv.put("typle",widgetItem.getTyple());
        cv.put("deviceType",widgetItem.getDeviceType());
        cv.put("uid",widgetItem.getUid());
        cv.put("position", widgetItem.getIndex());
        cv.put("iconId",widgetItem.getSrcId());
        cv.put("status",widgetItem.getStatus());
        cv.put("roomId",widgetItem.getRoomId());
        cv.put("delFlag",0);

        return cv;
    }

}
