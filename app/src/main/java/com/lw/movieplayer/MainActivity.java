package com.lw.movieplayer;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lw.movieplayer.activity.BaseActivity;
import com.lw.movieplayer.activity.OtherFloderVideoActivity;
import com.lw.movieplayer.activity.PlayListActivity;
import com.lw.movieplayer.activity.SettingsActivity;
import com.lw.movieplayer.adapter.MainVideoAdapter;
import com.lw.movieplayer.bean.VideoInfo;
import com.lw.movieplayer.db.PlayerDao;
import com.lw.movieplayer.utils.Constants;
import com.lw.movieplayer.utils.FileUtil;
import com.lw.movieplayer.utils.PlayerUtil;

import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "com.xiaolajiao.videoplayer.MainActivity";

    private TextView mTitle;
    private ImageView mSetting;
    private ListView mListView;
    private List<String> mAllVideoInfo;
    private boolean mState;
    private PlayerDao mDao;
    private MainVideoAdapter mAdapter;
    private List<VideoInfo> mAllPlayList;
    private List<VideoInfo> mCameraFiles;
    private LinearLayout mNoVideo;
    public static final int REQUEST_ALL_VIDEO = 1;
    private View mWeixinHeadView;
    private View mCameraHeadView;
    private View mPlayListHeadView;
    private boolean isAddList = false;
    private boolean isAddCamera = false;
    private boolean isAddWeiXin = false;
    private List<VideoInfo> mWeixinVideos;
    private boolean mPlayBackState;

    @Override
    protected void initListener() {
        mSetting.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mState = PlayerUtil.getState(getApplicationContext(), Constants.FILTER_SHORT);
        //查询是否开启了播放记录
        mPlayBackState = PlayerUtil.getState(getApplicationContext(), Constants.PLAYBACK);
        //查询所有数据
        mDao = new PlayerDao(getApplicationContext());
        //获取拍摄的视频
        mCameraFiles = FileUtil.getStorageVideos(getApplicationContext(),Constants.recordPath,mState);
        //获取微信视频
        mWeixinVideos = FileUtil.getStorageVideos(getApplicationContext(),Constants.weiXinPath,false);
        //获取播放记录
        mAllPlayList = mDao.getAllPlayList();
        //获取所有视频
        mAllVideoInfo = FileUtil.getAllVideoInfo(getApplicationContext());
        System.out.println("----mCameraFiles = "+mCameraFiles.size()+"--mWeixinVideos = "
                +mWeixinVideos.size()+"--mAllPlayList = "+mAllPlayList.size()+"--mAllVideoInfo = "+mAllVideoInfo.size());

        if (mAllPlayList.size()==0&&mAllVideoInfo.size()==0&&mCameraFiles.size()==0){
            mNoVideo.setVisibility(View.VISIBLE);
            mListView.removeAllViewsInLayout(); //需要移除所有view， 否则当文件全被删除后会崩溃
            mAdapter = null;
            return;
        }
        updateInfo();
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_main;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void initData() {
        mTitle.setVisibility(View.VISIBLE);
        mSetting.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.video);
        mSetting.setImageResource(R.mipmap.icon_nav_setting);


    }
    private void updateInfo() {
            //避免相机和微信视频的位置移动到第一个
            if (mPlayListHeadView!=null){
                mListView.removeHeaderView(mPlayListHeadView);
            }

            if (!mPlayBackState){

            }else {
                if (mAllPlayList.size() > 0) {
                    mPlayListHeadView = getLayoutInflater().inflate(R.layout.headview_playlist, null);
                    LinearLayout ll = (LinearLayout) mPlayListHeadView.findViewById(R.id.LinearLayout_item);
                    ImageView image = (ImageView) mPlayListHeadView.findViewById(R.id.dir_image);
                    TextView name = (TextView) mPlayListHeadView.findViewById(R.id.floder_name);
                    TextView count = (TextView) mPlayListHeadView.findViewById(R.id.video_count);
                    //Glide.with(getApplicationContext()).load(mAllPlayList.get(0).getPath()).into(wechatImage);
                    image.setImageResource(R.mipmap.playlist);
                    name.setText("播放记录");
                    ll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startClass(PlayListActivity.class);
                        }
                    });
                    count.setText(mAllPlayList.size() + "");
                    mListView.addHeaderView(mPlayListHeadView);

                }
            }

        //录制视频
        if (mCameraHeadView!=null){
            mListView.removeHeaderView(mCameraHeadView);
        }

        if (mCameraFiles.size()>0){
            mCameraHeadView = getLayoutInflater().inflate(R.layout.main_item, null);
            LinearLayout ll = (LinearLayout) mCameraHeadView.findViewById(R.id.LinearLayout_item);
            ImageView image = (ImageView) mCameraHeadView.findViewById(R.id.dir_image);
            TextView name = (TextView) mCameraHeadView.findViewById(R.id.floder_name);
            TextView count = (TextView) mCameraHeadView.findViewById(R.id.video_count);
            Glide.with(getApplicationContext()).load(mCameraFiles.get(0).getPath()).error(R.mipmap.playlist).centerCrop().into(image);
            name.setText("相机");
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //startClass(RecordListActivity.class);
                    Intent intent = new Intent(MainActivity.this,OtherFloderVideoActivity.class);
                    intent.putExtra("path",Constants.recordPath);
                    startActivity(intent);
                    //startActivityForResult(intent,REQUEST_ALL_VIDEO);
                }
            });
            count.setText(mCameraFiles.size()+"");
            mListView.addHeaderView(mCameraHeadView);
            isAddCamera = true;

        }
        //微信 视频headView
            if (mWeixinHeadView!=null){
                mListView.removeHeaderView(mWeixinHeadView);
            }

        if (mWeixinVideos!=null&&mWeixinVideos.size()>0){
            System.out.println("--- mWeixinVideos = "+mWeixinVideos.size());
            mWeixinHeadView = getLayoutInflater().inflate(R.layout.main_item, null);
            LinearLayout ll = (LinearLayout) mWeixinHeadView.findViewById(R.id.LinearLayout_item);
            ImageView wechatImage = (ImageView) mWeixinHeadView.findViewById(R.id.dir_image);
            TextView wechatFloderName = (TextView) mWeixinHeadView.findViewById(R.id.floder_name);
            TextView wechatVideoCount = (TextView) mWeixinHeadView.findViewById(R.id.video_count);
            Glide.with(getApplicationContext()).load(mWeixinVideos.get(0).getPath()).into(wechatImage);
            wechatFloderName.setText("微信视频");
            wechatVideoCount.setText(mWeixinVideos.size()+"");
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,OtherFloderVideoActivity.class);
                    intent.putExtra("path",Constants.weiXinPath);
                    //startActivityForResult(intent,REQUEST_ALL_VIDEO);
                    startActivity(intent);
                }
            });
            mListView.addHeaderView(mWeixinHeadView);
        }

        mAdapter = new MainVideoAdapter(getApplicationContext(), mAllVideoInfo);
        mListView.setAdapter(mAdapter);
    }


    @Override
    public void initView() {
        PlayerUtil.getPermission(this);
        findViewById(R.id.iv_pen).setVisibility(View.GONE);
        mNoVideo = (LinearLayout)findViewById(R.id.no_video);
        mTitle = (TextView) findViewById(R.id.tv_main_title);
//        mTvOther = (TextView) findViewById(R.id.tv_other);
//        mPlayList = (LinearLayout) findViewById(R.id.play_list_ll);
//        mRecordList = (LinearLayout) findViewById(R.id.record_list_ll);
        mSetting = (ImageView) findViewById(R.id.setting);
//        mIvRecord = (ImageView) findViewById(R.id.iv_record);
//        mIvPlayList = (ImageView) findViewById(R.id.iv_playlist);
//        mLine = findViewById(R.id.other_line);
        mListView = (ListView) findViewById(R.id.main_listview);

    }

    private void startClass(Class c){
        Intent intent = new Intent(MainActivity.this,c);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.setting:
                startClass(SettingsActivity.class);
                break;
                default:
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(MainActivity.this, OtherFloderVideoActivity.class);//OtherFloderVideoActivity
        intent.putExtra("path",adapterView.getAdapter().getItem(i).toString());
        startActivity(intent);

    }
}
