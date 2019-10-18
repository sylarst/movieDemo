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
import com.lw.movieplayer.db.PlayerDao;
import com.lw.movieplayer.utils.Constants;
import com.lw.movieplayer.utils.PlayerUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luow on 2017/12/27.
 */

public class PlayListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    String TAG = "com.xiaolajiao.videoplayer.activity.PlayListActivity";
    public static final int REQUEST_LIST = 1;
    private TextView mTitle;
    private ImageView mIv_pen;
    private ImageView mIv_delete;
    private Button mBtn_select_all;
    private Button mBtn_cancle;
    //private TextView mTv_back;
    private LinearLayout mRl_delete;
   // private ImageView mIv_delete;
    private ImageView mBack;
    private AlertDialog mBuilder;
    private PlayerDao mDao;
    private boolean isIntoCheck;
    private PlayListAdapter mAdapter;
    private List<VideoInfo> mList;
    private ListView mListView;
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
                    if (count==mList.size()){
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
    private TextView mTvDelete;

    @Override
    protected void initListener() {
        mIv_pen.setOnClickListener(this);
        mBtn_select_all.setOnClickListener(this);
       // mIv_delete.setOnClickListener(this);
        mRl_delete.setOnClickListener(this);
        mBtn_cancle.setOnClickListener(this);
        mListView.setOnItemClickListener(this);

    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_play_list;
    }

    @Override
    protected void initData() {
        mDao = new PlayerDao(getApplicationContext());
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.play_list);
        mTvDelete.setTextColor(getResources().getColor(R.color.no_select));
        mIv_delete.setImageResource(R.mipmap.icon_delete_no);
        mBtn_cancle.setVisibility(View.GONE);
        mIv_pen.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        //mIv_pen.setImageResource(R.mipmap.icon_nav_edit);

        boolean state = PlayerUtil.getState(getApplicationContext(), Constants.PLAYBACK);
        if (!state){
            mDao.deleteAllList();
        }
    }

    @Override
    protected void onResume() {
        mList = mDao.getAllPlayList();
        mAdapter = new PlayListAdapter(getApplicationContext());
        mAdapter.setData(mList);
        mListView.setAdapter(mAdapter);
        super.onResume();
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
        mTvDelete = (TextView) findViewById(R.id.tv_delete);
        mListView = (ListView) findViewById(R.id.listview_list);
        // mCheckBox = (CheckBox)findViewById(R.id.choose);
        mBtn_select_all = (Button) findViewById(R.id.btn_select_all);
        mBtn_cancle = (Button) findViewById(R.id.btn_cancle);
        mIv_pen = (ImageView) findViewById(R.id.iv_pen);
        mIv_delete = (ImageView) findViewById(R.id.iv_delete);
        mBack = (ImageView) findViewById(R.id.back);
        //mTv_back = (TextView) findViewById(R.id.tv_back);
        //mTv_cancle_edit = (TextView) findViewById(R.id.tv_cancle_edit);
        mRl_delete = (LinearLayout) findViewById(R.id.ll_delete);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_pen:
                intoCheck();    //进入编辑模式
                break;
            case R.id.btn_cancle:
                cancelEdit();    //取消编辑模式
                break;
            case R.id.btn_select_all:
                selectAll();    //全选和反选
                break;
            case R.id.ll_delete:
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
    private int chooseCount = 0;
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


                if (chooseCount==mList.size()){
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
                        for (int key : isCheck_delete.keySet()) {   //通过获取key的值来得到保存的是哪个选中的position，再从数据库中删除该position
                            if (isCheck_delete.get(key)){
                                mDao.deleteList(mList.get(key).getTitle());
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
                        mDao.deleteList(mList.get(key).getTitle());
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

    /*删除逻辑*/
    private void deleteList() {
        // 拿到所有数据
        Map<Integer, Boolean> isCheck_delete = mAdapter.getMap();
        if (isCheck_delete.size()<=0){
            mRl_delete.setEnabled(false);
        }else{

            mRl_delete.setEnabled(true);
            Log.d(TAG,"isCheck_delete.size() = "+isCheck_delete.size());
            for (int key : isCheck_delete.keySet()) {   //通过获取key的值来得到保存的是哪个选中的position，再从数据库中删除该position
                if (isCheck_delete.get(key)){
                    mDao.deleteList(mList.get(key).getTitle());
                }
            }

// 获取到条目数量，map.size = list.size,所以
        int count = mAdapter.getCount();
        System.out.println("----isCheck_deleteSize = "+isCheck_delete.size()+"---Count = "+count+"---mList.size()"+mList.size());

        for (int i = 0; i < count; i++) {
            // 删除有两个map和list都要删除 ,计算方式
            int position = i - (count - mAdapter.getCount());
            // 判断状态 true为删除
            if (isCheck_delete.get(i) != null && isCheck_delete.get(i)) {
            // listview删除数据
                isCheck_delete.remove(i);
                mAdapter.removeData(position);
            }
        }
            mBtn_select_all.setText("全选");
        mAdapter.notifyDataSetChanged();

        cancelEdit();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isIntoCheck){
            if (view.getTag() instanceof PlayListAdapter.ViewHolder){
                PlayListAdapter.ViewHolder holder = (PlayListAdapter.ViewHolder)view.getTag();
                holder.mBox.toggle();
            }
        }else{
            System.out.println("-------position1 = "+position);
            String path = mList.get(position).getPath();
            File file = new File(path);
            if (file.exists()){
            Intent intent = new Intent(PlayListActivity.this,VideoPlayActivity.class);//
            intent.putExtra("list", (Serializable) mList);
            intent.putExtra("position",position);
            //startActivity(intent);
                startActivityForResult(intent,REQUEST_LIST);
            }else{
                Toast.makeText(getApplicationContext(),"文件不存在",Toast.LENGTH_SHORT).show();
                for (int i = 0; i < mList.size(); i++) {
                    String path1 = mList.get(i).getPath();
                    File f = new File(path1);
                    if (!f.exists()){
                        mDao.deleteList(mList.get(i).getTitle());   //失效的记录进行删除
                    }
                }
                mList.clear();//一定要先清空所有列表，再重新查询数据库记录，否则崩溃
                //重新设置数据
                mList = mDao.getAllPlayList();
                mAdapter.setData(mList);
                mListView.setAdapter(mAdapter);
                //adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_LIST){
            switch (resultCode){
                case VideoPlayActivity.RESULT_SUCCESS:
                    getAllData();
                    break;
                    default:
            }
        }

    }

    private void getAllData() {
        System.out.println("----getAllData");
        if (mList.size()>0) {
            System.out.println("----getAllData");
            mListView.removeAllViewsInLayout();
            mList.clear();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(800);
                        mList = mDao.getAllPlayList();
                        // mAdapter = new PlayListAdapter(getApplicationContext());
                        mAdapter.setData(mList);
                        listHandler.sendEmptyMessage(UPDATE_LIST);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

           // mAdapter.notifyDataSetChanged();
        }
    }

    public class PlayListAdapter extends BaseAdapter {

        private List<VideoInfo> list = new ArrayList<VideoInfo>();
        private Context mContext;
        // 存储勾选框状态的map集合
        private Map<Integer, Boolean> isCheck = new HashMap<Integer, Boolean>();

        public PlayListAdapter(Context mContext) {
            super();
            this.mContext = mContext;
// 默认为不选中
            initCheck(false);
        }

        // 初始化map集合
        public void initCheck(boolean flag) {
        // map集合的数量和list的数量是一致的
            for (int i = 0; i < list.size(); i++) {
        // 设置默认的显示
                isCheck.put(i, flag);
            }
        }

        // 设置数据
        public void setData(List<VideoInfo> data) {
            this.list = data;
        }

        // 添加数据
        public void addData(VideoInfo bean) {
        // 下标 数据
            list.add(0, bean);
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_playlist, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            VideoInfo info = list.get(position);
            if (isIntoCheck){
                viewHolder.mBox.setVisibility(View.VISIBLE);
            }else{
                viewHolder.mBox.setVisibility(View.GONE);
            }
            String path = info.getPath();
            String[] split = path.split("/");
            viewHolder.mName.setText(split[split.length-1]);

            Glide.with(mContext).load(list.get(position).getPath()).error(R.mipmap.playlist).centerCrop().into(viewHolder.mImage);
            viewHolder.mTime.setText(info.getComplition());
            // 勾选框的点击事件
            viewHolder.mBox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            // 用map集合保存
                            isCheck.put(position, isChecked);
                            listHandler.sendEmptyMessage(LIST_MSG); //发送更新删除按钮可否点击
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

        class ViewHolder {
            private final TextView mTime;
            private final TextView mName;
            private final ImageView mImage;
            private final CheckBox mBox;


            public ViewHolder(View itemView) {
                mTime = (TextView) itemView.findViewById(R.id.video_time);
                mName = (TextView) itemView.findViewById(R.id.video_name);
                mImage = (ImageView) itemView.findViewById(R.id.video_image);
                mBox = itemView.findViewById(R.id.box);

            }
        }
        // 全选按钮获取状态
        public Map<Integer, Boolean> getMap() {
// 返回状态
            return isCheck;
        }

        // 删除一个数据
        public void removeData(int position) {
            list.remove(position);
        }
    }
}