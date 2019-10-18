package com.lw.movieplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lw.movieplayer.R;
import com.lw.movieplayer.bean.VideoInfo;
import com.lw.movieplayer.utils.PlayerUtil;
import com.lw.movieplayer.utils.StringUtil;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luow on 2017/12/29.
 */

public class OthersAdapter extends BaseAdapter {
    // 存储勾选框状态的map集合
    private Map<Integer, Boolean> isCheck = new HashMap<Integer, Boolean>();
    private final Context mContext;
    private final List<VideoInfo> mVideoInfos;


    public OthersAdapter(Context context, List<VideoInfo> list){
            this.mContext = context;
        this.mVideoInfos = list;
        // 默认为不选中
        initCheck(false);
    }
    // 初始化map集合
    public void initCheck(boolean flag) {
        // map集合的数量和list的数量是一致的
        for (int i = 0; i < mVideoInfos.size(); i++) {
            // 设置默认的显示
            isCheck.put(i, flag);
        }
    }
    @Override
    public int getCount() {
        return mVideoInfos != null ? mVideoInfos.size() : 0;
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
        VideoInfo info = mVideoInfos.get(position);
        Glide.with(mContext).load(info.getPath()).into(viewHolder.mImage);//.thumbnail(0.0001f)
        viewHolder.mName.setText(info.getTitle());
        viewHolder.mTime.setText(PlayerUtil.FormetFileSize(info.getSize())+"/"+ StringUtil.parseDuration(info.getDuration()));

        viewHolder.mBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // 用map集合保存
                        isCheck.put(position, isChecked);
                       // listHandler.sendEmptyMessage(LIST_MSG); //发送更新删除按钮可否点击
                        System.out.println("---------put了");
                    }
                });

// 设置状态
        if (isCheck.get(position) == null) {
            isCheck.put(position, false);
        }
        viewHolder.mBox.setChecked(isCheck.get(position));
        return convertView;
    }
     class ViewHolder{
         private final ImageView mImage;
         private final TextView mName;
         private final TextView mTime;
         private final CheckBox mBox;
         private final LinearLayout mItem;

         public ViewHolder(View view){
             mImage = view.findViewById(R.id.video_image);
             mName = (TextView) view.findViewById(R.id.video_name);
             mTime = (TextView) view.findViewById(R.id.video_time);
             mItem = (LinearLayout) view.findViewById(R.id.ll_item);
             mBox = view.findViewById(R.id.box);
         }
    }
    // 全选按钮获取状态
    public Map<Integer, Boolean> getMap() {
// 返回状态
        return isCheck;
    }

    // 删除一个数据
    public void removeData(int position) {
        mVideoInfos.remove(position);
    }
}
