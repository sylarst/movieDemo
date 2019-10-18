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
import com.lw.movieplayer.utils.FileUtil;
import com.lw.movieplayer.utils.StringUtil;

import java.io.File;
import java.util.List;

/**
 * Created by luow on 2018/1/11.
 */

public class TestAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<File> mList;

    public TestAdapter(Context context, List<File> list){
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
        ViewHolder viewHolder = null;
        if (convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String path = mList.get(i).getAbsolutePath();

        Glide.with(mContext).load(path).thumbnail(0.0001f).into(viewHolder.mImage);
        viewHolder.mName.setText(mList.get(i).getName());
        viewHolder.mTime.setText(StringUtil.parseDuration(Long.parseLong(FileUtil.getVideoDuration(path))));
        //viewHolder.mTime.setText(StringUtil.parseDuration(mList.get(position).getDuration())+"");
        //viewHolder.mTime.setText(mVideoInfos.get(position).getDuration()+"");
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
