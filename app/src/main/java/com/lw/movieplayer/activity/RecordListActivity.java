package com.lw.movieplayer.activity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.lw.movieplayer.R;
import com.lw.movieplayer.adapter.VideosListAdapter;
import com.lw.movieplayer.bean.VideoInfo;
import com.lw.movieplayer.utils.Constants;
import com.lw.movieplayer.utils.FileUtil;
import com.lw.movieplayer.utils.PlayerUtil;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by luow on 2017/12/27.
 */

public class RecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    String TAG = "com.xiaolajiao.videoplayer.activity.RecordListActivity";
    private TextView mTitle;
    private ListView mListView;
    private VideosListAdapter mAdapter;
    private List<VideoInfo> mVideos;
    private boolean mState;

    @Override
    protected void initListener() {
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_record_list;
    }

    @Override
    protected void initData() {
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.record_video);

        mState = PlayerUtil.getState(getApplicationContext(), Constants.FILTER_SHORT);
        mVideos = FileUtil.getOtherAllVideos(getApplicationContext(), Constants.recordPath,mState);
        mAdapter = new VideosListAdapter(getApplicationContext(), mVideos);
        mListView.setAdapter(mAdapter);

    }

    @Override
    public void initView() {
        mTitle = (TextView) findViewById(R.id.tv_title);
        findViewById(R.id.back).setVisibility(View.VISIBLE);
        mListView = (ListView) findViewById(R.id.lv_record);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String path = mVideos.get(i).getPath();
        File file = new File(path);
        if (file.exists()) {
            Intent intent = new Intent(RecordListActivity.this, VideoPlayActivity.class);
            intent.putExtra("list", (Serializable) mVideos);
            intent.putExtra("position", i);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(),"文件不存在",Toast.LENGTH_SHORT).show();
            //重新设置数据
            mVideos = FileUtil.getOtherAllVideos(getApplicationContext(), Constants.recordPath,mState);
            mAdapter = new VideosListAdapter(getApplicationContext(), mVideos);
            mListView.setAdapter(mAdapter);
        }
    }
}
