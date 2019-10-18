package com.lw.movieplayer.utils;


import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import com.lw.movieplayer.bean.VideoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luow on 2017/12/28.
 */

public class FileUtil {

//获取文件夹下所有视频文件
    public static List<File> getFloderFiles(String path,boolean isFilter){
        List<File> list = new ArrayList<>();
        File file = new File(path);
        if (file.exists()){
            File[] files = file.listFiles();
            if (files.length>0){
                for (int i = 0; i < files.length; i++) {
                    String filename = files[i].getName();
                    if (filename.trim().toLowerCase().endsWith(".mp4")||filename.trim().toLowerCase().endsWith(".3gp")||
                            filename.trim().toLowerCase().endsWith(".avi")||filename.trim().toLowerCase().endsWith(".flv")
                            ||filename.trim().toLowerCase().endsWith(".wmv")||filename.trim().toLowerCase().endsWith(".mov")
                            ||filename.trim().toLowerCase().endsWith(".mpg")
                            ||filename.trim().toLowerCase().endsWith(".wmv")||filename.trim().toLowerCase().endsWith(".vob")
                            ||filename.trim().toLowerCase().endsWith(".webm")||filename.trim().toLowerCase().endsWith(".mkv")) {
                        if (isFilter){
                            if (files[i].length()<100*1000){ //大于100K才添加进来
                                continue;
                            }
                            list.add(files[i]);
                        }else{
                            list.add(files[i]);
                        }
                    }
                }
            }
        }else {
            return null;
        }
        return list;
    }
    public static Bitmap getVideoPhoto(String videoPath) {
        File file = new File(videoPath);
        if (file.exists()){
        MediaMetadataRetriever media =new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        Bitmap bitmap = media.getFrameAtTime();
        return bitmap;
        }else{
            return null;
        }
    }


    //获取指定文件夹下所有视频文件 - 不带过滤小于100K的短视频
    public static List<VideoInfo> getVideoIsFilter(Context context, String fileAbsolutePath,boolean isFilter) {
        ArrayList<VideoInfo> list = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        if (!file.exists()){
            //Toast.makeText(context, "文件夹不存在", Toast.LENGTH_SHORT).show();
            return null;
        }
        File[] subFile = file.listFiles();

        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                // System.out.println("-------subFile[iFileLength] = "+subFile[iFileLength].length());
                String filename = subFile[iFileLength].getName();
                // 判断是否为MP4结尾
                if (filename.trim().toLowerCase().endsWith(".mp4")||filename.trim().toLowerCase().endsWith(".3gp")||
                        filename.trim().toLowerCase().endsWith(".avi")||filename.trim().toLowerCase().endsWith(".flv")
                        ||filename.trim().toLowerCase().endsWith(".wmv")) {
                    //list.add(subFile[iFileLength].g);

                    VideoInfo info = new VideoInfo();
                    if (isFilter){
                        if (subFile[iFileLength].length()<100*1000){ //大于100K才添加进来
                           continue;
                        }
                        info.setPath(subFile[iFileLength].getPath());
                        info.setTitle(subFile[iFileLength].getName());
                        info.setDuration(Long.valueOf(getVideoDuration(subFile[iFileLength].getPath())));
                        list.add(info);
                    }else{
                        info.setPath(subFile[iFileLength].getPath());
                        info.setTitle(subFile[iFileLength].getName());
                        info.setDuration(Long.valueOf(getVideoDuration(subFile[iFileLength].getPath())));
                        list.add(info);
                    }

                }
            }
        }
        return list;
    }

    //获取视频总时长
    public static String getVideoDuration(String path){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        String s = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return s;
    }


    //获取sd卡目录下所有视频文件夹路径
    public static List<String> getAllVideoInfo(Context context){
        boolean state = PlayerUtil.getState(context, Constants.FILTER_SHORT);
        List<String> list = null;
        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    long size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    //if (state&&size>100*1024){//过滤小于100k的视频
                    File file = new File(path);
                    if (!file.exists()){
                        continue;
                    }
                    String[] split = path.split("/");
                    String s = path.replace(split[split.length-1],"");
                    //System.out.println("---path = "+path.replace(split[split.length-1],""));
                   if (!list.contains(s)){ //过滤录制的视频
                       if (s.equals(Constants.recordPath)||s.equals(Constants.weiXinPath)){
                           continue;
                       }
                       if (state){
                           if (size<100*1000){
                               continue;
                           }
                       }
                       System.out.println("-------path = "+path);
                       list.add(s);
                   }
                }
                cursor.close();
            }
        }
        return list;

    }
//获取视频的宽高
    public static Map<String ,String> getVideoWH(String path){
        Map<String ,String> map = new HashMap<>();
        MediaMetadataRetriever media =new MediaMetadataRetriever();
        File file = new File(path);
        if (!file.exists()){
            return null;
        }else {
            media.setDataSource(path);
            String width = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            map.put("width", width);
            map.put("height", height);
            return map;
        }
    }


    //根据URI获取路径
    public static String getUriPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static List<VideoInfo> getOtherAllVideos(Context context,String path,boolean isFilter){
        List<VideoInfo> list = null;

        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                    File file = new File(videoPath);
                    //不加此判断会造成文件删除了该文件还会显示在列表中
                    if (!file.exists()){
                        continue;
                    }
                    String[] split = videoPath.split("/");
                    String s = videoPath.replace(split[split.length-1],""); //裁剪当前循环到的视频路径   去掉文件名
                    if (s.equals(path)){    //如果当前循环的文件是列表视频的文件
                        VideoInfo info = new VideoInfo();
                        if (isFilter){
                            if (size<100*1000){
                                continue;
                            }else{
                                info.setPath(videoPath);
                                info.setTitle(title);
                                info.setDuration(duration);
                                info.setSize(size);
                                list.add(info);
                            }
                        }else{
                            info.setPath(videoPath);
                            info.setTitle(title);
                            info.setDuration(duration);
                            info.setSize(size);
                            list.add(info);
                        }
                    }
                }
                cursor.close();
            }
        }
        return list;
    }

    //获取内部存储视频文件
    public static List<VideoInfo> getStorageVideos(Context context,String path,boolean isFilter){
        List<VideoInfo> list = null;

        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                    null, null);
            if (cursor != null) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    long size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

                    File file = new File(videoPath);
                    if (!file.exists()){
                        continue;
                    }
                    String[] split = videoPath.split("/");
                    String s = videoPath.replace(split[split.length-1],""); //裁剪当前循环到的视频路径   去掉文件名
//"---videopath = "+videoPath+
                    //如果当前循环的文件是列表视频的文件
                    if (s.equals(path)){
                        System.out.println("----------s = "+s);
                        VideoInfo info = new VideoInfo();
                        if (isFilter){
                            if (size<100*1000){
                                continue;
                            }else{
                                info.setPath(videoPath);
                                info.setTitle(title);
                                info.setDuration(duration);
                                list.add(info);
                            }
                        }else{
                            info.setPath(videoPath);
                            info.setTitle(title);
                            info.setDuration(duration);
                            list.add(info);
                        }
                    }
                }
                cursor.close();
            }
        }
        return list;
    }
}
