package com.lw.movieplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luow on 2018/1/2.
 */

public class StringUtil {
    private static final int HOUR = 60*60*1000;
    private static final int MIN = 60*1000;
    private static final int SEC = 1000;
    //视频时长解析
    public static String parseDuration(long duration){
        long hour = duration / HOUR;
        long min = duration%HOUR/MIN;
        long sec = duration%MIN/SEC;
        if(hour==0){
            return  String.format("%02d:%02d", min, sec);
        }else {
            return  String.format("%02d:%02d:%02d", hour, min, sec);
        }
    }
    //获取当前时间
    public static String getCrrentTime(){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date(System.currentTimeMillis()));
    }
}
