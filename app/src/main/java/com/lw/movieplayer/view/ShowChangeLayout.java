package com.lw.movieplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lw.movieplayer.R;


/**
 * Created by luow on 2017/12/26.
 */
public class ShowChangeLayout extends RelativeLayout {
    private static final String TAG = "gesturetest";
    private ImageView iv_center;
    private ProgressBar pb;
    private HideRunnable mHideRunnable;
    private int duration = 1000;
    private TextView tv;
    private ProgressBar mFastPb;

    public ShowChangeLayout(Context context) {
        super(context);
        init(context);
    }

    public ShowChangeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.show_change_layout,this);
        iv_center = (ImageView) findViewById(R.id.iv_center);
        pb = (ProgressBar) findViewById(R.id.pb);
        mFastPb = (ProgressBar) findViewById(R.id.fast_pb);
        tv = (TextView)findViewById(R.id.show_tv);

        mHideRunnable = new HideRunnable();
        ShowChangeLayout.this.setVisibility(GONE);
    }

    //显示
    public void show(){
        setVisibility(VISIBLE);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable,duration);
    }

    //设置进度
    public void setProgress(int progress){
        pb.setProgress(progress);
        Log.d(TAG, "setProgress: " +progress);
    }
    public void setMaxProgress(int maxProgress){
        mFastPb.setMax(maxProgress);
    }
    public void setValue(String value){
        tv.setText(value);
    }
    public void setPbVisibles(){
        pb.setVisibility(VISIBLE);
    }
    public void setfastVisibles(){
        mFastPb.setVisibility(VISIBLE);
    }

    public void setPbGone(){
        pb.setVisibility(GONE);
    }
    public void setFastGone(){
        mFastPb.setVisibility(GONE);
    }
    public void setFastProgress(int progress){
        mFastPb.setProgress(progress);
        Log.d(TAG, "setProgress: " +progress);
    }


    //设置持续时间
    public void setDuration(int duration) {
        this.duration = duration;
    }

    //设置显示图片
    public void setImageResource(int resource){
        iv_center.setImageResource(resource);
    }

    //隐藏自己的Runnable
    private class HideRunnable implements Runnable{
        @Override
        public void run() {
            ShowChangeLayout.this.setVisibility(GONE);
        }
    }
}
