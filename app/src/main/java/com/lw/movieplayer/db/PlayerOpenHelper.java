package com.lw.movieplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 15212 on 2017/12/30.
 */

public class PlayerOpenHelper extends SQLiteOpenHelper {

    public PlayerOpenHelper(Context context) {
        super(context, "playerdb.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        * name 片名
        * path 路径
        * duration 当前播放的进度
        * complition 播放完或者播放到哪里
        * */
        String sql = "create table player (_id integer primary key autoincrement,name text,path text,duration text,complition text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
