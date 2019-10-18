package com.lw.movieplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lw.movieplayer.bean.VideoInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 15212 on 2017/12/30.
 */

public class PlayerDao {
    private PlayerOpenHelper mHelper;
    private String NAME = "name";
    private String PATH = "path";
    private String DURATION= "duration";
    private String COMITION = "complition";
    private String CURRENTDURATION = "currentduration";

    public PlayerDao(Context context){
        mHelper = new PlayerOpenHelper(context);
    }

    public boolean isExist(String name){
        boolean result = false;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query("player", null, "name=?", new String[]{name}, null, null, null);
        if(cursor.moveToNext()){
            result = true;
        }cursor.close();
        db.close();
        return result;
    }
    public long getCurrentDuration(String name){
        SQLiteDatabase db = mHelper.getReadableDatabase();
        long duration = 0;
        Cursor cursor = db.query("player", null, "name=?", new String[]{name}, null, null, null);
        if(cursor.moveToNext()){
            duration = cursor.getInt(cursor.getColumnIndex("duration"));
        }
        cursor.close();
        return duration;
    }

    //添加播放记录到数据库中
    public boolean insertVideoList(String path,String name,long duration,String complition){
        if (isExist(name)){
            Log.d("----playerDao --","该数据已存在");
            boolean b = deleteList(name);
            if (b){
                Log.d("----playerDao --","该片原先的播放记录已删除");
            }else{
                Log.d("----playerDao --","该片原先的播放记录删除失败");
            }
        }
        SQLiteDatabase writableDatabase = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PATH,path);
        values.put(NAME,name);
        values.put(DURATION,duration);
        values.put(COMITION,complition);
        //values.put(CURRENTDURATION,currentduration);
        long insert = writableDatabase.insert("player", null, values);
        if (insert==-1){
            return false;
        }
        writableDatabase.close();
        return true;
    }

    //删除播放记录
    public boolean deleteList(String name){
        SQLiteDatabase database = mHelper.getWritableDatabase();
        int result = database.delete("player", "name=?", new String[]{name});
        database.close();
        if (result>0){
            return true;
        }else{
            return false;
        }
    }

    //查询所有播放记录
    public List<VideoInfo> getAllPlayList(){
        ArrayList<VideoInfo> data = new ArrayList<>();
        SQLiteDatabase readableDatabase = mHelper.getReadableDatabase();
        Cursor cursor = readableDatabase.query("player", new String[]{ "name", "path", "duration", "complition"}, null, null, null, null, "_id desc ");
        while (cursor.moveToNext()){
            //int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            //long duration = cursor.getInt(cursor.getColumnIndex("duration"));
            long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex("duration")));
            String complition = cursor.getString(cursor.getColumnIndex("complition"));
           // long currentduration = cursor.getLong(cursor.getColumnIndex("currentduration"));

            VideoInfo info = new VideoInfo();
            info.setTitle(name);
            info.setPath(path);
            info.setDuration(duration);
            info.setComplition(complition);
            //info.setCurrentduration(currentduration);
            data.add(info);
        }
        cursor.close();
        readableDatabase.close();
        return data;
    }

    //删除表中所有记录
    public void deleteAllList(){
        SQLiteDatabase writableDatabase = mHelper.getWritableDatabase();
        writableDatabase.delete("player",null,null);
        writableDatabase.close();
    }

    /**
     *
     * @param startrow
     *           起始行 从0开始
     * @param rowcount
     *           取多少条
     * @return
     *        从startrow位置开始取rowcount数据
     */
    public  List<VideoInfo> getMoreDatas(Context context,int startrow,int rowcount){
        List<VideoInfo> datas = new ArrayList<VideoInfo>();
        //获取数据封装
        SQLiteDatabase readableDatabase = mHelper.getReadableDatabase();

        //2. sql select
        Cursor cursor = readableDatabase.rawQuery("select _id,name,path,duration from player order by _id desc limit ?,?",
                new String[]{startrow+"",rowcount + ""});
        while (cursor.moveToNext()) {
            // 封装数据
            VideoInfo bean = new VideoInfo();
            //bean.set(cursor.getInt(0));// 主键
            bean.setTitle(cursor.getString(1));// 号码
            bean.setPath(cursor.getString(2)); // 模式
            // 添加数据
            datas.add(bean);
        }
        // 3. 关闭游标
        cursor.close();

        // 4. 关闭数据库
        readableDatabase.close();

        return datas;
    }
}
