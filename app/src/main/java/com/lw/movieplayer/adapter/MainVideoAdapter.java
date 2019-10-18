package com.lw.movieplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lw.movieplayer.R;
import com.lw.movieplayer.utils.Constants;
import com.lw.movieplayer.utils.FileUtil;
import com.lw.movieplayer.utils.PlayerUtil;

import java.io.File;
import java.util.List;

/**
 * Created by luow on 2018/1/3.
 */

public class MainVideoAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<String> mList;

    public MainVideoAdapter(Context context, List<String> list){
        this.mContext = context;
        this.mList = list;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.main_item,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        String s = mList.get(i);
        File file = new File(s);
        if (file.isDirectory()){
            String name = file.getName();
            if ((file.getAbsolutePath()+File.separator).equals(Constants.storagePath)){
                holder.mFloderName.setText("内部存储");
            } else {
                holder.mFloderName.setText(name);
            }
        }

        boolean mState = PlayerUtil.getState(mContext, Constants.FILTER_SHORT);
       // List<VideoInfo> floderFiles = FileUtil.getStorageVideos(mContext, s, mState);
            List<File> floderFiles = FileUtil.getFloderFiles(s, mState);
            holder.mVideoCount.setText(floderFiles.size()+"");
            Glide.with(mContext).load(floderFiles.get(0).getPath()).error(R.mipmap.playlist).centerCrop().into(holder.mFloderPhoto);

        //List<VideoInfo> floderFiles = FileUtil.getStorageVideos(mContext, s, mState);


        return convertView;
    }

    public class ViewHolder{

        private final TextView mVideoCount;
        private final TextView mFloderName;
        private final ImageView mFloderPhoto;

        public ViewHolder(View v){
            mFloderPhoto = (ImageView) v.findViewById(R.id.dir_image);
            mFloderName = (TextView) v.findViewById(R.id.floder_name);
            mVideoCount = (TextView) v.findViewById(R.id.video_count);
        }
    }
}
