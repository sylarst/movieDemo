package com.lw.movieplayer.activity;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.lw.movieplayer.R;
import com.lw.movieplayer.bean.VideoInfo;
import com.lw.movieplayer.db.PlayerDao;
import com.lw.movieplayer.utils.Constants;
import com.lw.movieplayer.utils.FileUtil;
import com.lw.movieplayer.utils.PlayerUtil;
import com.lw.movieplayer.utils.ScreenSwitchUtils;
import com.lw.movieplayer.utils.StringUtil;
import com.lw.movieplayer.view.BrightnessHelper;
import com.lw.movieplayer.view.ShowChangeLayout;
import com.lw.movieplayer.view.VideoGestureRelativeLayout;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by luow on 2017/12/29.
 * Video 播放类
 */

public class VideoPlayActivity extends Activity implements VideoGestureRelativeLayout.VideoGestureListener, MediaPlayer.OnCompletionListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnErrorListener {
    //private static final int MSG_UPDATE_TIME = 0;
    public static final int RESULT_SUCCESS = 0;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final int MSG_HIDE = 2;
    private String TAG = "---VideoPlayActivity";

    private AudioManager mAudioManager;
    private int maxVolume = 0;
    private int oldVolume = 0;
    private int newProgress = 0,
            oldProgress = 0;
    private BrightnessHelper mBrightnessHelper;
    private float brightness = 1;
    private Window mWindow;
    private WindowManager.LayoutParams mLayoutParams;
    private VideoView mVideoView;
    private VideoGestureRelativeLayout mFingerTouchView;
    private ShowChangeLayout mScl;  //快进快退图标
    private int mPosition;

    private boolean isUriPath = false;
    private Uri mUri;
    private ImageView mLock; //锁图标
    private LinearLayout mControl_Top; //控制栏顶部
    private LinearLayout mControl_bottom; //控制栏底部
   // private TextView mTvSystemTime;     //系统时间
    private TextView mTvVideoName;      //片名
    private TextView mTvCurrentTime;    //已播放时长
    private TextView mTvVideoDuration;  //视频总时长
    private ImageView mBtnPre;          //上一部
    private ImageView mBtnPlay;         //播放
    private ImageView mBtnNext;         //下一部
    private SeekBar mSeekBar;           //进度条
    private boolean isLocked = false;
    private boolean mHide = false;
    private List<VideoInfo> mAllVideos;

    private Handler playhandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                /*case MSG_UPDATE_TIME:
                    startUpdateTime();

                    break;*/
                case MSG_UPDATE_PROGRESS:
                    getCurrentTime();
                    break;
                case MSG_HIDE:
                    hide();
                    break;
            }
            return false;
        }
    });

    private int mSavePosition;
    private PlayerDao mDao;
    private int mNavigationBarHeight;
    private String mFromWherePath;
    private boolean isPlayCompletion = false;   //标记是否是完成了播放，以便保存记录
    private ScreenSwitchUtils instance;
    private long mVideoDuration;
    private ImageView mIvBack;
    private boolean mPlayBackState;
    private boolean mIsHaveBackKey;

    private void getCurrentTime() {
        mSavePosition = mVideoView.getCurrentPosition();    //获取当前播放的进度，以便在用户直接退出时保存播放进度
        mSeekBar.setMax(mVideoView.getDuration());
        mSeekBar.setProgress(mVideoView.getCurrentPosition());
        mTvCurrentTime.setText(StringUtil.parseDuration(mSavePosition));
        playhandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 100);
    }

    @Override
    protected void onStop() {
        super.onStop();
        instance.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        instance.start(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_play);
        mIsHaveBackKey = PlayerUtil.checkDeviceHasNavigationBar(getApplicationContext());

        //屏幕旋转播放实例化
        instance = ScreenSwitchUtils.init(this.getApplicationContext());
        // 获取播放记录是否打开
         mPlayBackState = PlayerUtil.getState(getApplicationContext(), Constants.PLAYBACK);
        initView();
        initListener();
        //全屏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //取消状态栏
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //初始化获取音量属性
        mAudioManager = (AudioManager)getSystemService(Service.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //初始化亮度调节
        mBrightnessHelper = new BrightnessHelper(this);

        //设置当前APP亮度
        mWindow = getWindow();
        mLayoutParams = mWindow.getAttributes();
        brightness = mLayoutParams.screenBrightness;

        mDao = new PlayerDao(getApplicationContext());

        //隐藏NavigationBar
        PlayerUtil.setSystemUIVisible(VideoPlayActivity.this,false);
        mNavigationBarHeight = PlayerUtil.getNavigationBarHeight(VideoPlayActivity.this);
        System.out.println("------mNavigationBarHeight = "+mNavigationBarHeight);

        Intent intent = getIntent();
        mUri = intent.getData();
        if (mUri !=null){
            isUriPath = true;//判断是不是应用外调起的视频播放
            mVideoView.setVideoURI(mUri);
            String path = FileUtil.getUriPath(getApplicationContext(), mUri);
            String[] split = path.split("/");
            mTvVideoName.setText(split[split.length-1]);//设置外来文件名
            String videoDuration = FileUtil.getVideoDuration(path);
            getCurrentTime();
            mTvVideoDuration.setText(StringUtil.parseDuration(Long.parseLong(videoDuration))); //设置外来文件时长
            mVideoView.start();

        }else {
            mAllVideos = (List<VideoInfo>) intent.getSerializableExtra("list");
            mPosition = intent.getIntExtra("position", 0);

            //screenState();
            play(mPosition);
        }
        screenState();
        //startUpdateTime();//开启系统时间更新
//        mBtnPlay.setImageResource(R.mipmap.btn_pause);
        //updateBtnBg();
        mBtnPlay.setBackground(getResources().getDrawable(R.drawable.play_pause_bg));
    }

    //根据屏幕朝向选择正确的显示方式
    private void screenState(){
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
           // Toast.makeText(this, "现在为横屏", Toast.LENGTH_SHORT).show();
            videoLandscapeShow();
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Toast.makeText(this, "现在为竖屏", Toast.LENGTH_SHORT).show();
            videoPortraitShow();
        }
    }
    String url="http://play.g3proxy.lecloud.com/vod/v2/MjQ5LzM3LzIwL2xldHYtdXRzLzE0L3Zlcl8wMF8yMi0xMTA3NjQxMzkwLWF2Yy00MTk4MTAtYWFjLTQ4MDAwLTUyNjExMC0zMTU1NTY1Mi00ZmJjYzFkNzA1NWMyNDc4MDc5OTYxODg1N2RjNzEwMi0xNDk4NTU3OTYxNzQ4Lm1wNA==?b=479&mmsid=65565355&tm=1499247143&key=98c7e781f1145aba07cb0d6ec06f6c12&platid=3&splatid=345&playid=0&tss=no&vtype=13&cvid=2026135183914&payff=0&pip=08cc52f8b09acd3eff8bf31688ddeced&format=0&sign=mb&dname=mobile&expect=1&tag=mobile&xformat=super";
    private void play(int position) {
        long duration = mDao.getCurrentDuration(mAllVideos.get(position).getTitle());//当前播放进度
        mVideoView.setVideoPath(mAllVideos.get(position).getPath());
//        mVideoView.setVideoPath(url);     //播放网络视频
        //总时长
        mVideoDuration = Long.parseLong(FileUtil.getVideoDuration(mAllVideos.get(position).getPath()));

        System.out.println("-----------duration= "+duration+"--videoDuration = "+mVideoDuration);
        if (duration<mVideoDuration){    //小于播放总时长就定位到之前播放的位置
            mVideoView.seekTo((int) duration);
            mTvVideoDuration.setText(StringUtil.parseDuration(mVideoDuration));
        }else{
            mTvVideoDuration.setText(StringUtil.parseDuration(mVideoDuration));
        }

        mVideoView.start();
        getCurrentTime();
        mTvVideoName.setText(mAllVideos.get(position).getTitle());

    }

    @Override
    protected void onResume() {
        if (isOnPause) {
            if (!mVideoView.isPlaying()) {
                mVideoView.seekTo(cu);
                mVideoView.start();
                isOnPause = false;
                mBtnPlay.setBackground(getResources().getDrawable(R.drawable.play_pause_bg));
//                mBtnPlay.setImageResource(R.mipmap.btn_pause);
//                updateBtnBg();
               // updateBtnState();
                playhandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 500);

            }else{

            }
        }
        super.onResume();
    }
    private int cu = 0;
    private boolean isOnPause = false;
    @Override
    protected void onPause() {
        //此标记为了记录是否有失去焦点，并记录当前播放的时长
        isOnPause = true;
        cu = mVideoView.getCurrentPosition();
        if (mVideoView.isPlaying()){
//            mBtnPlay.setImageResource(R.mipmap.btn_play);
//            updateBtnBg();
            mBtnPlay.setBackground(getResources().getDrawable(R.drawable.play_bg));
            mVideoView.pause();
           // handler.removeCallbacks(mCurrentTimeRunnable);
            playhandler.removeMessages(MSG_UPDATE_PROGRESS);

        }
        super.onPause();
    }

    private void initListener() {
        mFingerTouchView.setVideoGestureListener(this);
        mVideoView.setOnCompletionListener(this);
        mLock.setOnClickListener(this);
        mBtnPre.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mIvBack.setOnClickListener(this);
        mVideoView.setOnErrorListener(this);
    }
    private void initView() {
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mFingerTouchView = (VideoGestureRelativeLayout) findViewById(R.id.finger_touch_view);
        mScl = (ShowChangeLayout) findViewById(R.id.show_change);
        mLock = (ImageView) findViewById(R.id.lock);
        mControl_Top = (LinearLayout) findViewById(R.id.control_top);
        mControl_bottom = (LinearLayout) findViewById(R.id.control_bottom);
        //mTvSystemTime = (TextView)findViewById(R.id.tv_system_time);
        mTvVideoName = (TextView)findViewById(R.id.tv_video_name);
        mTvCurrentTime = (TextView)findViewById(R.id.tv_current_time);
        mTvVideoDuration = (TextView)findViewById(R.id.tv_video_duration);
        mBtnPre = (ImageView) findViewById(R.id.btn_pre);
        mBtnPlay = (ImageView) findViewById(R.id.btn_play);
        mBtnNext = (ImageView) findViewById(R.id.btn_next);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
    }

    //根据重力感应调节视频控件大小
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (instance.isPortrait()) {
            // 切换成横屏
            videoLandscapeShow();
        } else {
            // 切换成竖屏
            videoPortraitShow();
        }
    }

    //横屏下控制操作栏的显示问题
    private void videoLandscapeShow(){
        if (isUriPath){ //如果是uri传递来的文件，就用这个路径
            mFromWherePath = FileUtil.getUriPath(getApplicationContext(), mUri);
        }else{
            mFromWherePath =mAllVideos.get(mPosition).getPath();
            File file  = new File(mFromWherePath);
            if (!file.exists()){
                Toast.makeText(getApplicationContext(),"找不到该文件",Toast.LENGTH_LONG).show();
                return;
            }

        }
            //横屏下应该将下方控制栏保持原样，因为NavigationBar在横屏时显示的是在右边不是下边
            RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) mControl_bottom.getLayoutParams();
            layoutParams1.bottomMargin = 0;
            layoutParams1.leftMargin = mNavigationBarHeight - 25; //横屏时让控件左右间隔开虚拟按键
            layoutParams1.rightMargin = mNavigationBarHeight - 25;
            mControl_bottom.setLayoutParams(layoutParams1);
            //横屏时设置锁图标的左靠宽度值
            RelativeLayout.LayoutParams lockParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            lockParams.addRule(RelativeLayout.CENTER_VERTICAL);
            lockParams.leftMargin = mNavigationBarHeight + 16;
            mLock.setLayoutParams(lockParams);

            Map<String, String> videoWH = FileUtil.getVideoWH(mFromWherePath);
            String width = videoWH.get("width");
            String height = videoWH.get("height");
        //说明是竖向视频
            if (Integer.parseInt(width) < Integer.parseInt(height)) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mVideoView.setLayoutParams(layoutParams);

            }else{
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mVideoView.setLayoutParams(layoutParams);
            }

    }
    //竖屏时控制栏及相关view的显示问题
    private void videoPortraitShow(){
            //纵向的时候就需要给其设置margin属性
            RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) mControl_bottom.getLayoutParams();
            layoutParams1.bottomMargin = mNavigationBarHeight - 25;
            layoutParams1.leftMargin = 0;   //竖屏时上方控制栏左右2边要恢复原样
            layoutParams1.rightMargin = 0;
            mControl_bottom.setLayoutParams(layoutParams1);
            //竖屏时设置锁离左边的间距
            RelativeLayout.LayoutParams lockParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                    , ViewGroup.LayoutParams.WRAP_CONTENT);
            lockParams.addRule(RelativeLayout.CENTER_VERTICAL);
            lockParams.leftMargin = 41;
            mLock.setLayoutParams(lockParams);

            if (isUriPath){ //如果是uri传递来的文件，就用这个路径
                mFromWherePath = FileUtil.getUriPath(getApplicationContext(), mUri);
            }else{
                 mFromWherePath =mAllVideos.get(mPosition).getPath();
                File file  = new File(mFromWherePath);
                if (!file.exists()){
                    Toast.makeText(getApplicationContext(),"找不到该文件",Toast.LENGTH_LONG).show();
                    return;
                }
            }

            Map<String, String> videoWH = FileUtil.getVideoWH(mFromWherePath); //获取正在播放视频的宽高
            String width = videoWH.get("width");
            String height = videoWH.get("height");
            if (Integer.parseInt(width)>Integer.parseInt(height)) { //说明是竖向视频
                RelativeLayout.LayoutParams videoViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT);
                videoViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mVideoView.setLayoutParams(videoViewParams);
        }

    }

    @Override
    public void onBrightnessGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mScl.setFastGone();
        mScl.setPbVisibles();
        //下面这是设置当前APP亮度的方法
        Log.d(TAG, "onBrightnessGesture: old" + brightness);
        //只需滑动屏幕一半的高度   去除*2 则亮度从0到100需要滑动整个屏幕的高度
        float newBrightness = (e1.getY() - e2.getY()) / mFingerTouchView.getHeight()*2 ;
        newBrightness += brightness;
        Log.d(TAG, "onBrightnessGesture: new" + newBrightness);
        if (newBrightness < 0){
            newBrightness = 0;
        }else if (newBrightness > 1){
            newBrightness = 1;
        }
        System.out.println("-----newBrightness = "+newBrightness+"--brightness = "+brightness);
        mLayoutParams.screenBrightness = newBrightness;
        mWindow.setAttributes(mLayoutParams);
        mScl.setProgress((int) (newBrightness * 100));
        mScl.setValue((int) (newBrightness * 100)+"");
        mScl.setImageResource(R.drawable.brightness_w);
        mScl.show();
    }

    @Override
    public void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mScl.setFastGone();
        mScl.setPbVisibles();
        Log.d(TAG, "onVolumeGesture: oldVolume " + oldVolume);
        int value = mFingerTouchView.getHeight()/2/maxVolume ;
        int newVolume = (int) ((e1.getY() - e2.getY())/value + oldVolume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,newVolume,AudioManager.FLAG_PLAY_SOUND);

        Log.d(TAG, "onVolumeGesture: newVolume "+ newVolume);
        System.out.println("---------value = "+value+"--newVolume = "+newVolume);
        //要强行转Float类型才能算出小数点，不然结果一直为0
        int volumeProgress = (int) (newVolume/Float.valueOf(maxVolume) *100);
        System.out.println("--------maxVolume ="+maxVolume+"--newVolume = "+newVolume+"--volumeProgress = "+volumeProgress);
        if (newVolume>15){
            newVolume = 15;
        }
        mScl.setValue(newVolume+"");
        if (volumeProgress >= 50){
            mScl.setImageResource(R.drawable.volume_higher_w);
        }else if (volumeProgress > 0){
            mScl.setImageResource(R.drawable.volume_lower_w);
        }else {
            mScl.setImageResource(R.drawable.volume_off_w);
            mScl.setValue(0+"");
        }

        mScl.setProgress(volumeProgress);
        mScl.show();
    }

    @Override
    public void onFF_REWGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mScl.setPbGone();
        mScl.setfastVisibles();
        mScl.setMaxProgress((int) mVideoDuration);
            float offset = e2.getX() - e1.getX();
            oldProgress = mVideoView.getCurrentPosition();
            //根据移动的正负决定快进还是快退
            if (offset > 0) {
                mScl.setImageResource(R.drawable.ff);
                newProgress = (int) (oldProgress + offset/mFingerTouchView.getWidth()/5 * 100*1000);
                System.out.println("------mVideoDuration = "+mVideoDuration);
                if (newProgress > mVideoDuration){
                    newProgress = (int)mVideoDuration;
                }
            }else {
                mScl.setImageResource(R.drawable.fr);
                newProgress = (int) (oldProgress + offset/mFingerTouchView.getWidth()/5 * 100*1000);
                if (newProgress < 0){
                    newProgress = 0;
                }
            }
            mVideoView.seekTo(newProgress);
            System.out.println("-----offset"+offset+"--oldProgress = "+oldProgress+"--newProgress = "+newProgress+"--offset/mFingerTouchView.getWidth()/5 * 100"+offset/mFingerTouchView.getWidth()/5 * 100);
            //mScl.setProgress(newProgress/1000);
           mScl.setValue(StringUtil.parseDuration(mVideoView.getCurrentPosition())+"/"+StringUtil.parseDuration(mVideoDuration));
            mScl.setFastProgress(mVideoView.getCurrentPosition());
            mScl.show();



        //
      /*  mScl.setPbGone();
        mScl.setfastVisibles();
        mScl.setMaxProgress((int) mVideoDuration);*/
       /* float offset = e2.getX() - e1.getX();
        int oldProgress = mVideoView.getCurrentPosition() / 1000;
        //根据移动的正负决定快进还是快退
        if (offset > 0) {
            mScl.setImageResource(R.drawable.ff);

            newProgress = (int) (oldProgress + offset/mFingerTouchView.getWidth()/5 * 100); // *的数越大，快进的秒数越多
            if (newProgress > mVideoDuration/1000){
                newProgress = (int)(mVideoDuration/1000);
            }
        }else {
            mScl.setImageResource(R.drawable.fr);
           // int oldProgress = mVideoView.getCurrentPosition() / 1000;
            newProgress = (int) (oldProgress + offset/mFingerTouchView.getWidth()/5 * 100); // *的数值越大，快退的秒数越多
            if (newProgress < 0){
                newProgress = 0;
            }
        }
        System.out.println("-----old = "+oldProgress+"--new = "+newProgress+"---videoDuration = "+mVideoDuration/1000+"current = "+mVideoView.getCurrentPosition()/1000);
        mVideoView.seekTo(newProgress*1000);
        mScl.setValue(StringUtil.parseDuration(mVideoView.getCurrentPosition())+"/"+StringUtil.parseDuration(mVideoDuration));
        mScl.setFastProgress(newProgress);
        //mScl.setProgress(newProgress/1000);
        //System.out.println("-------快进快退值 = "+newProgress);
        mScl.show();
        show();*/
    }
    private void lockState() {
        playhandler.removeMessages(MSG_HIDE);
        isLocked = !isLocked;
        if (isLocked){
            mLock.setImageResource(R.mipmap.lock_close);
        }else{
            mControl_bottom.setVisibility(View.VISIBLE);
            mControl_Top.setVisibility(View.VISIBLE);
            mLock.setImageResource(R.mipmap.lock_open);
        }
        playhandler.sendEmptyMessageDelayed(MSG_HIDE,5000);
    }
    @Override
    public void onSingleTapGesture(MotionEvent e) {
        mHide = !mHide; //上下栏隐藏开关
        if (mHide){ //如果为true 就就上2端都显示
           show();
        }else{  //如果为false 就就上2端都不显示 ，只显示锁图标
               hide();
        }
        playhandler.sendEmptyMessageDelayed(MSG_HIDE,5000);
    }
    //隐藏上下栏目
    private void hide() {
        mHide = false;  //方便下次点击时再走show的逻辑
        mControl_Top.setVisibility(View.GONE);
         mControl_bottom.setVisibility(View.GONE);
       /* ViewCompat.animate(mControl_Top).translationY(-mControl_Top.getHeight());
        ViewCompat.animate(mControl_bottom).translationY(mControl_bottom.getHeight()+mNavigationBarHeight);*/

        mLock.setVisibility(View.GONE);
        PlayerUtil.setSystemUIVisible(VideoPlayActivity.this,false); //连同NavigationBar一同隐藏
        playhandler.removeMessages(MSG_HIDE);

    }
    //显示上下栏目
    private void show(){
        PlayerUtil.setSystemUIVisible(VideoPlayActivity.this,true);
        if (isLocked){
            mControl_Top.setVisibility(View.GONE);
            mControl_bottom.setVisibility(View.GONE);
            mLock.setVisibility(View.VISIBLE);

        }else{
            mLock.setVisibility(View.VISIBLE);
            mControl_Top.setVisibility(View.VISIBLE);
            mControl_bottom.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDoubleTapGesture(MotionEvent e) {
        Log.d(TAG, "onDoubleTapGesture: ");
        playOrPause();
    }

    @Override
    public void onDown(MotionEvent e) {
        //每次按下的时候更新当前亮度和音量，还有进度
        oldProgress = newProgress;
        oldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        brightness = mLayoutParams.screenBrightness;
        if (brightness == -1){
            //一开始是默认亮度的时候，获取系统亮度，计算比例值
            brightness = mBrightnessHelper.getBrightness() / 255f;
        }
    }

    @Override
    public void onEndFF_REW(MotionEvent e) {
      //  System.out.println("------newProgress"+newProgress);
    }
    //这是直接设置系统亮度的方法
    private void setBrightness(int brightness) {
        //要是有自动调节亮度，把它关掉
        mBrightnessHelper.offAutoBrightness();

        int oldBrightness = mBrightnessHelper.getBrightness();
        Log.d(TAG, "onBrightnessGesture: oldBrightness: " + oldBrightness);
        int newBrightness = oldBrightness + brightness;
        Log.d(TAG, "onBrightnessGesture: newBrightness: " + newBrightness);
        //设置亮度
        mBrightnessHelper.setSystemBrightness(newBrightness);
        //设置显示
        mScl.setProgress((int) (Float.valueOf(newBrightness)/mBrightnessHelper.getMaxBrightness() * 100));
        mScl.setImageResource(R.drawable.brightness_w);
        mScl.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isUriPath) {   //外部调用的播放器不保存播放记录
            if (mVideoView.getCurrentPosition()<mVideoDuration&&mPlayBackState){  //如果是未播完成的情况下才保存当前进度
                    boolean b = mDao.insertVideoList(mAllVideos.get(mPosition).getPath(), mAllVideos.get(mPosition).getTitle(),
                            mSavePosition, "观看至" + StringUtil.parseDuration(mSavePosition));

                    System.out.println("-----b = " + b + "--mSavePosition = " + mSavePosition + "--getPath()" + mAllVideos.get(mPosition).getPath()
                            + "getTitle() = " + mAllVideos.get(mPosition).getTitle());
            }
        }
        setResult(RESULT_SUCCESS);
        if (playhandler!=null){
            //playhandler.removeMessages(MSG_UPDATE_TIME);
            playhandler.removeMessages(MSG_UPDATE_PROGRESS);
            playhandler.removeMessages(MSG_HIDE);
            playhandler = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        isPlayCompletion = true;
        //如果是打开状态就添加进来
        if (mPlayBackState&& !isUriPath){
            boolean b = mDao.insertVideoList(mAllVideos.get(mPosition).getPath(), mAllVideos.get(mPosition).getTitle(), mVideoDuration,"已看完");
            if (b) {
                System.out.println("----添加成功");
            }
        }
            boolean state = PlayerUtil.getState(getApplicationContext(), Constants.LOOP);
            if (state){
                if (isUriPath){
                    mVideoView.start();
                }else {
                    play(mPosition);
                }
            }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lock:
               lockState();
                break;
            case R.id.btn_pre:
                playPre();
                break;
            case R.id.btn_play:
                playOrPause();
                break;
            case R.id.btn_next:
                playNext();
                break;
                case R.id.iv_back:
                finish();
                break;
                default:
        }
    }

    //更新播放按键状态
    private void updateBtnState(){
        /*if (mVideoView.isPlaying()){
            mBtnPlay.setImageResource(R.mipmap.btn_play);
        }else{
            mBtnPlay.setImageResource(R.mipmap.btn_pause);
        }*/
        if (mVideoView.isPlaying()){
            mBtnPlay.setBackground(getResources().getDrawable(R.drawable.play_pause_bg));
        }else{
            mBtnPlay.setBackground(getResources().getDrawable(R.drawable.play_bg));
        }
    }

    private void playPre() {
        playhandler.removeMessages(MSG_HIDE);
        if (mPosition==0){
            Toast.makeText(getApplicationContext(),"已是第一个视频",Toast.LENGTH_SHORT).show();
            return;
        }
        //添加到播放记录
        if (!isPlayCompletion&&mPlayBackState) {
                PlayerDao dao = new PlayerDao(getApplicationContext());
                int currentPosition = mVideoView.getCurrentPosition();
                dao.insertVideoList(mAllVideos.get(mPosition).getPath(), mAllVideos.get(mPosition).getTitle()
                        , mVideoView.getCurrentPosition(), "观看至" + StringUtil.parseDuration(currentPosition));
        }

        mPosition--;
        play(mPosition);
        updateBtnState();
        playhandler.sendEmptyMessageDelayed(MSG_HIDE,5000);
    }

    private void playNext() {
        if (mPosition==mAllVideos.size()-1){
            Toast.makeText(getApplicationContext(),"已是最后一个视频",Toast.LENGTH_SHORT).show();
            return;
        }
        playhandler.removeMessages(MSG_HIDE);
        //添加到播放记录
        if (mVideoView.getCurrentPosition()<mVideoDuration&&mPlayBackState&&!isPlayCompletion) {
                PlayerDao dao = new PlayerDao(getApplicationContext());
                int currentPosition = mVideoView.getCurrentPosition();
                dao.insertVideoList(mAllVideos.get(mPosition).getPath(), mAllVideos.get(mPosition).getTitle(),
                        mVideoView.getCurrentPosition(), "观看至" + StringUtil.parseDuration(currentPosition));
        }
        mPosition++;
        play(mPosition);
        isPlayCompletion = false;
        playhandler.sendEmptyMessageDelayed(MSG_HIDE,5000);
    }

    private void playOrPause() {
        playhandler.removeMessages(MSG_HIDE);
        if (mVideoView.isPlaying()){
            mVideoView.pause();
            //updateBtnBg();
            updateBtnState();
//            mBtnPlay.setImageResource(R.mipmap.btn_play);
        }else{
            mVideoView.start();
//            updateBtnBg();
            updateBtnState();
//            mBtnPlay.setImageResource(R.mipmap.btn_pause);

        }
        playhandler.sendEmptyMessageDelayed(MSG_HIDE,5000);
    }

    //时间更新
   /* private void startUpdateTime() {
        mTvSystemTime.setText(StringUtil.getCrrentTime());
        playhandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
    }*/

    //seekBar
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) {
            mVideoView.seekTo(i);
        }
    }
    //seekBar
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mHide){
            playhandler.removeMessages(MSG_HIDE);
        }
    }
    //seekBar
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mHide){
            playhandler.sendEmptyMessageDelayed(MSG_HIDE,5000);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
      Toast.makeText(getApplicationContext(),"该视频无法正常播放",Toast.LENGTH_SHORT).show();
        return true;//
    }


}
