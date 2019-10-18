package com.lw.movieplayer.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by luow on 2017/12/28.
 */

public class Constants {
    public static String PLAYBACK = "playlist";  //启用播放记录的sp文件名
    public static String FILTER_SHORT = "short"; //自动过滤短视频的sp文件名
    public static String LOOP = "loop";          //循环播放的sp文件名

    //微信文件夹
    public static String weiXinPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"tencent"+File.separator+
            "MicroMsg"+File.separator+"WeiXin"+File.separator;
    //录制视频目录
    public static String recordPath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"DCIM"+File.separator+"Camera"+File.separator;
    //内部存储根目录
    public static String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator;
}
