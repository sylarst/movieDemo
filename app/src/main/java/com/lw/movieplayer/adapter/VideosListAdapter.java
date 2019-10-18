package com.lw.movieplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lw.movieplayer.R;
import com.lw.movieplayer.bean.VideoInfo;
import com.lw.movieplayer.utils.StringUtil;

import java.util.List;

/**
 * Created by luow on 2017/12/29.
 */

public class VideosListAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<VideoInfo> mVideoInfos;

    public VideosListAdapter(Context context, List<VideoInfo> list){
        this.mContext = context;
        this.mVideoInfos = list;
    }

    @Override
    public int getCount() {
        return mVideoInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return mVideoInfos.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Glide.with(mContext).load(mVideoInfos.get(position).getPath()).error(R.mipmap.playlist).centerCrop().into(viewHolder.mImage);
        viewHolder.mName.setText(mVideoInfos.get(position).getTitle());
        viewHolder.mTime.setText(StringUtil.parseDuration(mVideoInfos.get(position).getDuration()));
        return convertView;
    }
     class ViewHolder{
         private final ImageView mImage;
         private final TextView mName;
         private final TextView mTime;
         private final LinearLayout mItem;

         public ViewHolder(View view){
             mImage = view.findViewById(R.id.video_image);
             mName = (TextView) view.findViewById(R.id.video_name);
             mTime = (TextView) view.findViewById(R.id.video_time);
             mItem = (LinearLayout) view.findViewById(R.id.ll_item);
         }
    }
}
