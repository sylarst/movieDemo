package com.lw.movieplayer.activity;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.lw.movieplayer.R;
import com.lw.movieplayer.db.PlayerDao;
import com.lw.movieplayer.utils.Constants;
import com.lw.movieplayer.utils.PlayerUtil;


/**
 * Created by luow on 2017/12/27.
 */

public class SettingsActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private TextView mTv_back;
    private Switch mIvOpenList;
    private Switch mShortVideoState;
    private Switch mLoopState;
    private boolean mFilterShort;   //过滤短视频状态
    private boolean mLoop;          //循环播放状态
    private boolean mPlayList;      //播放记录状态

    @Override
    protected void initListener() {
        mLoopState.setOnCheckedChangeListener(this);
        mShortVideoState.setOnCheckedChangeListener(this);
        mIvOpenList.setOnCheckedChangeListener(this);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_settings;
    }

    @Override
    protected void initData() {
        mTv_back.setVisibility(View.VISIBLE);
        mTv_back.setText(R.string.setting);
        mFilterShort = PlayerUtil.getState(getApplicationContext(),Constants.FILTER_SHORT);
        mLoop = PlayerUtil.getState(getApplicationContext(),Constants.LOOP);
        mPlayList = PlayerUtil.getState(getApplicationContext(), Constants.PLAYBACK);
        mShortVideoState.setChecked(mFilterShort);
        mIvOpenList.setChecked(mPlayList);
        mLoopState.setChecked(mLoop);

    }

    @Override
    public void initView() {
        findViewById(R.id.back).setVisibility(View.VISIBLE);
        mTv_back = (TextView) findViewById(R.id.tv_back);
        mIvOpenList = (Switch) findViewById(R.id.iv_open_play_list);
        mShortVideoState = (Switch) findViewById(R.id.filter_shortvideo_state);
        mLoopState = (Switch) findViewById(R.id.loop_state);

    }

    private void deleteAllPlayList() {
        PlayerDao dao = new PlayerDao(getApplicationContext());
        dao.deleteAllList();
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton==mShortVideoState){
            if (b){
                PlayerUtil.savePlayListState(getApplicationContext(),Constants.FILTER_SHORT,b);
            }else{
                PlayerUtil.savePlayListState(getApplicationContext(),Constants.FILTER_SHORT,b);
            }
        }else if(compoundButton==mLoopState){
            if (b){
                PlayerUtil.savePlayListState(getApplicationContext(),Constants.LOOP,b);
            }else{
                PlayerUtil.savePlayListState(getApplicationContext(),Constants.LOOP,b);
            }
        }else if(compoundButton==mIvOpenList){
            if (b){
                PlayerUtil.savePlayListState(getApplicationContext(),Constants.PLAYBACK,b);
            }else{
                PlayerUtil.savePlayListState(getApplicationContext(),Constants.PLAYBACK,b);
                    System.out.println("-------mpLaylist = "+b);
                    deleteAllPlayList();
            }
        }
    }
}
