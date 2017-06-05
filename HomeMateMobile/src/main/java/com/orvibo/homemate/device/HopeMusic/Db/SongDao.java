package com.orvibo.homemate.device.HopeMusic.Db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.fasterxml.jackson.databind.node.POJONode;
import com.orvibo.homemate.bo.Scene;
import com.orvibo.homemate.device.HopeMusic.Bean.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliquan on 2016/6/28.
 */
public class SongDao {
    public static SongDao bean;
    public static volatile SQLiteDatabase sDB;
    private static String TABLE_NAME="song_table";

    public SongDao(Context context) {
        initDB(context);
    }

    private  void initDB(Context context) {
        if (sDB == null) {
            sDB = SongDbHelper.getInstance(context).getWriteDb();
        }
    }

    public static void releaseDB() {
        synchronized (SongDbHelper.LOCK) {
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
    public void insSong(String userid,Song song) {
        synchronized (SongDbHelper.LOCK) {
            try {
                sDB.insert(TABLE_NAME, null,
                        getContentValues(null,userid, song));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void insertSong(String userId,ArrayList<Song> songs){
        if(songs!=null){
            for(Song song:songs){
                insSong(userId,song);
            }
        }
    }

    public ArrayList<Song> selItemByTyple(String userId, String deviceId){
        ArrayList<Song> songList = new ArrayList<Song>();
        Cursor cursor = null;
        synchronized (SongDbHelper.LOCK) {
            try {
                cursor = sDB.rawQuery("select * from " + TABLE_NAME
                                + " where deviceId = '"+deviceId+"'"+" and userId = '"+userId+"' and delFlag = " + 0 + " order by updateTime",
                        null);
                while (cursor.moveToNext()) {

                    Song song = getSong(cursor);
                    if (song != null) {
                        songList.add(song);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                SongDbHelper.closeCursor(cursor);
            }
        }
        return songList;
    }


    public void deleteDataByUserId(String userId){
        try {
            final String[] whereArgs = {userId};
            sDB.execSQL("delete from " + TABLE_NAME
                    + " where userId = ? ", whereArgs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private Song getSong(Cursor cursor) {
        Song song = new Song();
        String deviceId   = cursor.getString(cursor.getColumnIndex("deviceId"));
        String Id   = cursor.getString(cursor.getColumnIndex("deviceId"));
        String position   = cursor.getString(cursor.getColumnIndex("position"));
        int duration   = cursor.getInt(cursor.getColumnIndex("duration"));
        String title   = cursor.getString(cursor.getColumnIndex("title"));
        String album   = cursor.getString(cursor.getColumnIndex("album"));
        String artist   = cursor.getString(cursor.getColumnIndex("artist"));
        String img   = cursor.getString(cursor.getColumnIndex("img"));

        song.setDeviceId(deviceId);
        song.setId(Id);
        song.setIndex(position);
        song.setDuration(duration);
        song.setTitle(title);
        song.setAlbum(album);
        song.setArtist(artist);
        song.setImg(img);

        return song;
    }

    private ContentValues getContentValues(ContentValues cv, String userId, Song song) {
        if (cv == null) {
            cv = new ContentValues();
        } else {
            cv.clear();
        }
        cv.put("userId",userId);
        cv.put("deviceId",song.getDeviceId());
        cv.put("id",song.getId());
        cv.put("position", song.getIndex());
        cv.put("duration", song.getDuration());
        cv.put("title",song.getTitle());
        cv.put("album",song.getAlbum());
        cv.put("artist",song.getArtist());
        cv.put("img",song.getImg());
        cv.put("delFlag",0);

        return cv;
    }


}
