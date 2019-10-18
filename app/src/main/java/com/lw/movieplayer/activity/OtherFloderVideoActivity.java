package com.lw.movieplayer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lw.movieplayer.R;
import com.lw.movieplayer.bean.VideoInfo;
import com.lw.movieplayer.utils.Constants;
import com.lw.movieplayer.utils.FileUtil;
import com.lw.movieplayer.utils.PlayerUtil;
import com.lw.movieplayer.utils.StringUtil;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luow on 2018/1/9.
 */

public class OtherFloderVideoActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private String TAG = "com.xiaolajiao.videoplayer.activity.OtherFloderVideoActivity";
    private TextView mTitle;
    private ListView mListView;
    private String mPath;
    private List<VideoInfo> mFloderFiles;
    private boolean mState;
    private Button mBtn_cancle;
    private Button mBtn_select_all;
    private ImageView mIv_pen;
    private TextView mTvDelete;
    private ImageView mBack;
    private LinearLayout mRl_delete;
    private ImageView mIv_delete;
    private OthersAdapter mAdapter;
    private boolean isIntoCheck;
    private int chooseCount = 0;
    private final int LIST_MSG = 10000;
    private final int UPDATE_LIST = 10001;
    private Handler listHandler = new Handler(new Handler.Callback() {
        @SuppressLint("ResourceAsColor")
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what){
                case LIST_MSG: //实时更新删除是否可点击，需要先获得当前所有的选择数
                    int count = 0;
                    final Map<Integer, Boolean> isCheck_delete = mAdapter.getMap();
                    for (int i = 0; i < isCheck_delete.size(); i++) {
                        if (isCheck_delete.get(i)){
                            count++;
                        }
                    }
                    if (count==mFloderFiles.size()){
                        mBtn_select_all.setText("全不选");
                    }
                    if (count==0){
                        mTvDelete.setTextColor(getResources().getColor(R.color.no_select));
                        mIv_delete.setImageResource(R.mipmap.icon_delete_no);
                        mRl_delete.setClickable(false);
                    }else{
                        mTvDelete.setTextColor(getResources().getColor(R.color.is_select));
                        mIv_delete.setImageResource(R.mipmap.icon_delete);
                        mRl_delete.setClickable(true);
                    }
                    break;
                case UPDATE_LIST:
                    mListView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void initListener() {
        mListView.setOnItemClickListener(this);
        mIv_pen.setOnClickListener(this);
        mBtn_select_all.setOnClickListener(this);
        // mIv_delete.setOnClickListener(this);
        mRl_delete.setOnClickListener(this);
        mBtn_cancle.setOnClickListener(this);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_other_list;
    }

    @Override
    protected void initData() {
        mState = PlayerUtil.getState(getApplicationContext(), Constants.FILTER_SHORT);
        mTitle.setVisibility(View.VISIBLE);
        mIv_pen.setVisibility(View.VISIBLE);
        mTvDelete.setTextColor(getResources().getColor(R.color.no_select));
        mIv_delete.setImageResource(R.mipmap.icon_delete_no);
        mBtn_cancle.setVisibility(View.GONE);
        mIv_pen.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        if (intent != null) {
            mPath = intent.getStringExtra("path");
        }
        System.out.println("------mPath = "+mPath);
        String[] split = mPath.split("/");
        if (mPath.equals(Constants.storagePath)) {
            mTitle.setText("内部存储");
            mFloderFiles = FileUtil.getStorageVideos(getApplicationContext(),mPath, mState);
        } else if (mPath.equals(Constants.weiXinPath)){
            //微信视频不过滤
            mFloderFiles = FileUtil.getOtherAllVideos(getApplicationContext(), mPath, false);
            mTitle.setText("微信视频");
        }else  if (mPath.equals(Constants.recordPath)){
            mFloderFiles = FileUtil.getOtherAllVideos(getApplicationContext(), mPath, mState);
            mTitle.setText("相机");
        } else{
            mFloderFiles = FileUtil.getOtherAllVideos(getApplicationContext(), mPath, mState);
            mTitle.setText(split[split.length - 1]);
        }

        //Log.d(TAG,mPath);
        if (mPath.equals(Constants.storagePath)){

        }if (mPath.equals(Constants.weiXinPath)){

        } else {

        }
        Log.d(TAG,mFloderFiles.size()+"");
        mAdapter = new OthersAdapter(getApplicationContext(),mFloderFiles);
        mListView.setAdapter(mAdapter);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listHandler!=null){
            listHandler.removeMessages(LIST_MSG);
            listHandler.removeMessages(UPDATE_LIST);
            listHandler = null;
        }

    }
    @Override
    public void initView() {
        findViewById(R.id.back).setVisibility(View.VISIBLE);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mListView = (ListView) findViewById(R.id.lv_otherlist);
        mIv_pen = (ImageView) findViewById(R.id.iv_pen);
        mIv_delete = (ImageView) findViewById(R.id.iv_delete_other);
        mTvDelete = (TextView) findViewById(R.id.tv_delete_other);
        mBtn_select_all = (Button) findViewById(R.id.btn_select_all);
        mBtn_cancle = (Button) findViewById(R.id.btn_cancle);
        mBack = (ImageView) findViewById(R.id.back);
        mRl_delete = (LinearLayout) findViewById(R.id.ll_delete_other);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String path = mFloderFiles.get(i).getPath();
        File file = new File(path);
        if (file.exists()) {
            Intent intent = new Intent(OtherFloderVideoActivity.this, VideoPlayActivity.class);//VideoActivity
            intent.putExtra("list", (Serializable) mFloderFiles);
            intent.putExtra("position", i);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(),"文件不存在",Toast.LENGTH_SHORT).show();
            if (mPath.equals(Constants.storagePath)){
                mFloderFiles = FileUtil.getStorageVideos(getApplicationContext(),mPath,mState);
            }else {
                mFloderFiles = FileUtil.getOtherAllVideos(getApplicationContext(), mPath, mState);

            }
            Log.d(TAG,mFloderFiles.size()+"");
            OthersAdapter adapter = new OthersAdapter(getApplicationContext(),mFloderFiles);
            mListView.setAdapter(adapter);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_pen:
                intoCheck();    //进入编辑模式
                break;
            case R.id.btn_cancle:
                cancelEdit();    //取消编辑模式
                break;
            case R.id.btn_select_all:
                selectAll();    //全选和反选
                break;
            case R.id.ll_delete_other:
                deleteItem();
                break;
            default:
        }
    }

    private void intoCheck() {
        mBtn_select_all.setVisibility(View.VISIBLE);
        mBtn_select_all.setText("全选");
        mRl_delete.setVisibility(View.VISIBLE);
        mBtn_cancle.setVisibility(View.VISIBLE);
        mBtn_cancle.setText("取消");
        mIv_pen.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);
        isIntoCheck = true;
        mRl_delete.setClickable(false);
        mTitle.setVisibility(View.GONE);
    }
    private void cancelEdit() {
        mAdapter.initCheck(false);
        mRl_delete.setVisibility(View.GONE);
        mIv_delete.setImageResource(R.mipmap.icon_delete_no);
        mTvDelete.setTextColor(getResources().getColor(R.color.no_select));
        mBtn_select_all.setVisibility(View.GONE);
        mBtn_cancle.setVisibility(View.GONE);
        mIv_pen.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.VISIBLE);
        isIntoCheck = false;

    }
    /**
     * 全选和反选
     */
    private void selectAll() {
        mAdapter.initCheck(false);
        // 全选——全不选
        if (mBtn_select_all.getText().equals("全选")) {
            mAdapter.initCheck(true);
            listHandler.sendEmptyMessage(LIST_MSG);
           /* mIv_delete.setImageResource(R.mipmap.icon_delete);
            mTvDelete.setTextColor(getResources().getColor(R.color.is_select));*/
// 通知刷新适配器
            mAdapter.notifyDataSetChanged();
            mBtn_select_all.setText("全不选");
        } else if (mBtn_select_all.getText().equals("全不选")) {
            mAdapter.initCheck(false);
            listHandler.sendEmptyMessage(LIST_MSG);
           /* mIv_delete.setImageResource(R.mipmap.icon_delete_no);
            mTvDelete.setTextColor(getResources().getColor(R.color.no_select));*/
// 通知刷新适配器
            mAdapter.notifyDataSetChanged();
            mBtn_select_all.setText("全选");
        }
    }

    private void deleteItem() {
        chooseCount = 0;
        // 拿到所有数据
        final Map<Integer, Boolean> isCheck_delete = mAdapter.getMap();
        for (int i = 0; i < isCheck_delete.size(); i++) {
            if (isCheck_delete.get(i)){
                chooseCount++;
            }
        }
        if (chooseCount>1){
            final AlertDialog builder = new AlertDialog.Builder(this).create();
            builder.show();
            if (builder.getWindow() == null) return;
            builder.getWindow().setContentView(R.layout.pop_user);//设置弹出框加载的布局
            TextView msg = (TextView) builder.findViewById(R.id.tv_msg);
            Button cancle = (Button) builder.findViewById(R.id.btn_cancle);
            Button sure = (Button) builder.findViewById(R.id.btn_sure);
            if (msg == null || cancle == null || sure == null) return;


            if (chooseCount==mFloderFiles.size()){
                msg.setText("是否清空所有播放记录");
            }else{
                msg.setText("是否删除选中播放记录");
            }
            cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    builder.dismiss();
                }
            });
            sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //通过获取key的值来得到保存的是哪个选中的position，再从删除该文件
                    for (int key : isCheck_delete.keySet()) {
                        if (isCheck_delete.get(key)){
                            //mDao.deleteList(mList.get(key).getTitle());
                            // TODO: 2018/1/18 删除文件
                            File file = new File(mFloderFiles.get(key).getPath());
                            if (file.exists()){
                                file.delete();
                            }
                        }
                    }

                    int count = mAdapter.getCount();
                    for (int i = 0; i < count; i++) {
                        int position = i - (count - mAdapter.getCount());
                        if (isCheck_delete.get(i) != null && isCheck_delete.get(i)) {
                            isCheck_delete.remove(i);
                            mAdapter.removeData(position);
                        }
                    }
                    mBtn_select_all.setText("全选");
                    mAdapter.notifyDataSetChanged();
                    builder.dismiss();
                    cancelEdit();
                }
            });
            chooseCount = 0;
        }else{
            Log.d(TAG,"chooseCount = "+chooseCount);
            for (int key : isCheck_delete.keySet()) {   //通过获取key的值来得到保存的是哪个选中的position，再从数据库中删除该position
                if (isCheck_delete.get(key)){
                    //mDao.deleteList(mList.get(key).getTitle());
                    // TODO: 2018/1/18 删除文件
                    File file = new File(mFloderFiles.get(key).getPath());
                    if (file.exists()){
                        file.delete();
                    }
                }
            }

            int count = mAdapter.getCount();
            for (int i = 0; i < count; i++) {
                int position = i - (count - mAdapter.getCount());
                if (isCheck_delete.get(i) != null && isCheck_delete.get(i)) {
                    isCheck_delete.remove(i);
                    mAdapter.removeData(position);
                }
            }
            mBtn_select_all.setText("全选");
            mAdapter.notifyDataSetChanged();
            cancelEdit();
            chooseCount = 0;
        }
    }
    //----------------------
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
            if (isIntoCheck){
                viewHolder.mBox.setVisibility(View.VISIBLE);
            }else{
                viewHolder.mBox.setVisibility(View.GONE);
            }
            Glide.with(mContext).load(info.getPath()).error(R.mipmap.playlist).into(viewHolder.mImage);//.thumbnail(0.0001f)
            viewHolder.mName.setText(info.getTitle());
            viewHolder.mTime.setText(PlayerUtil.FormetFileSize(info.getSize())+"/"+ StringUtil.parseDuration(info.getDuration()));

            viewHolder.mBox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            // 用map集合保存
                            isCheck.put(position, isChecked);
                            listHandler.sendEmptyMessage(LIST_MSG); //发送更新删除按钮可否点击
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
}
