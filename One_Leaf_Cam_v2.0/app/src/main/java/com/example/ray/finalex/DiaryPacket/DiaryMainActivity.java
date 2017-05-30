package com.example.ray.finalex.DiaryPacket;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Path;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.Text;
import com.example.ray.finalex.DiaryPacket.DiaryAdapter;
import com.example.ray.finalex.MainActivity;
import com.example.ray.finalex.R;
import com.example.ray.finalex.onekeyshare.OnekeyShare;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.geev2.impl.components.TuAlbumComponentOption;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by 43cm on 2016/12/13.
 */

public class DiaryMainActivity extends AppCompatActivity {

    private TextView add_diary;
    private ObjectAnimator animator;
    private static final int ADD_MESSAGE = 1;
    private MyHandler mHandler = null;
    private MyDBHelper myDBHelper;
    private DiaryAdapter adapter;
    private RecyclerView diaryListView;
    private TextView hintView;
    private Toolbar toolbar;
    private View longClickView;
    private int selectedItemNum;
    private boolean selected = false;
    public static List<Diary> DiaryInfo;
    int onClickItem;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_main);
        mHandler = new MyHandler(DiaryMainActivity.this);
        initView();
        initDB();
        setListeners();

        //提示日记为空
        Cursor c = myDBHelper.query();
        c.moveToFirst();
        if (c.getCount() != 0) {
            hintView.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        diaryListView.setLayoutManager(layoutManager);

        //设置删除动画
        diaryListView.setItemAnimator(new MyItemAnimator());

        //搜索后返回特定item
        Intent intentFromSearch = getIntent();
        try {
            int from_search = intentFromSearch.getIntExtra("searchNum", -1);
            diaryListView.smoothScrollToPosition(from_search);
        } catch (Exception e) {
            e.printStackTrace();
        }

        diaryListView.setAdapter(adapter);
    }

    //设置工具栏隐藏和显示的按钮
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_main_menu, menu);
        if (selected) {
            menu.findItem(R.id.change_item).setVisible(true);
            menu.findItem(R.id.delete_item).setVisible(true);
            menu.findItem(R.id.share_item).setVisible(true);
            menu.findItem(R.id.search_title_item).setVisible(false);
        } else {
            menu.findItem(R.id.change_item).setVisible(false);
            menu.findItem(R.id.delete_item).setVisible(false);
            menu.findItem(R.id.share_item).setVisible(false);
            menu.findItem(R.id.search_title_item).setVisible(true);
        }
        return true;
    }

    //工具栏点击事件处理
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.change_item:
                    DiaryInfo = queryData();
                    Intent intentAdd = new Intent(DiaryMainActivity.this, AddDiary.class);
                    intentAdd.putExtra("selectNum", selectedItemNum);
                    startActivity(intentAdd);
                    //先删除再增加
                    String change_time = queryData().get(selectedItemNum).getTime();
                    myDBHelper.delete(change_time);
                    removeCover();
                    break;
                case R.id.delete_item:
                    String delete_time = queryData().get(selectedItemNum).getTime();
                    adapter.remove(selectedItemNum);
                    myDBHelper.delete(delete_time);

                    //全部日记删除后，出现日记为空提示
                    Cursor c = myDBHelper.query();
                    c.moveToFirst();
                    if (c.getCount() == 0) {
                        hintView.setVisibility(View.VISIBLE);
                    }

                    //日记条目过少无法滚动，一直显示加号按钮
                    if (c.getCount() <= 2 && (int)add_diary.getTag() == 0) {
                        TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                        mShowAction.setDuration(300);
                        add_diary.startAnimation(mShowAction);
                        add_diary.setVisibility(View.VISIBLE);
                        add_diary.setTag(1);
                    }

                    removeCover();
                    break;
                case R.id.share_item:
                    //分享功能实现在此处
                    String share_url = queryData().get(selectedItemNum).getPic();
                    showShare(share_url);
                    /**************
                     * 开始你的表演
                     */


                    removeCover();
                    break;
                case R.id.search_title_item:
                    Intent intentSearch = new Intent(DiaryMainActivity.this, SearchActivity.class);
                    startActivity(intentSearch);
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    //去除长按半透明遮盖层
    private void removeCover() {
        selected = false;
        invalidateOptionsMenu();
        longClickView.setVisibility(View.GONE);
        diaryListView.findViewHolderForPosition(selectedItemNum).itemView.findViewById(R.id.list_view_clickable).setBackgroundResource(R.drawable.card_background_selector);
        selectedItemNum = -1;
    }

    //初始化数据库
    private void initDB() {
        myDBHelper = new MyDBHelper(this);
        adapter = new DiaryAdapter(DiaryMainActivity.this, queryData());
    }

    //查询数据库中的数据，返回带有Diary信息的List
    private List<Diary> queryData() {
        List<Diary> list = new ArrayList<Diary>();
        myDBHelper = new MyDBHelper(this);
        Cursor c = myDBHelper.query();
        while (c.moveToNext()) {
            String time = c.getString(c.getColumnIndex("time"));
            String title = c.getString(c.getColumnIndex("title"));
            String pic = c.getString(c.getColumnIndex("pic"));
            String address = c.getString(c.getColumnIndex("address"));
            String detail = c.getString(c.getColumnIndex("detail"));
            Diary d = new Diary();
            d.setTime(time);
            d.setTitle(title);
            d.setPic(pic);
            d.setAddress(address);
            d.setDetail(detail);
            list.add(d);
        }
        return list;
    }

    //初始化视图
    private void initView() {
        add_diary = (TextView) findViewById(R.id.add_diary);
        diaryListView = (RecyclerView) findViewById(R.id.diary_list_view);
        hintView = (TextView) findViewById(R.id.hint_view);
        add_diary.setTag(1);

        toolbar = (Toolbar) findViewById(R.id.diary_main_toolbar);

        longClickView = (View) findViewById(R.id.long_click_view);
        longClickView.setVisibility(View.GONE);

        toolbar.setTitle("一叶日记");

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_home100);

        toolbar.setOnMenuItemClickListener(onMenuItemClick);

    }


    //处理圆形加号按钮旋转后跳转Activity
    private static class MyHandler extends Handler {
        private WeakReference<DiaryMainActivity> mReference;
        private DiaryMainActivity mActivity;
        public MyHandler(DiaryMainActivity activity) {
            mReference = new WeakReference<DiaryMainActivity>(activity);
            mActivity = mReference.get();
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ADD_MESSAGE) {
                mActivity.animator.cancel();
                Intent intent = new Intent(mActivity, AddDiary.class);
                intent.putExtra("selectNum", -1);
                mActivity.startActivity(intent);
                mActivity.overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_last);
            }
        }
    }

    //设置监听器
    private void setListeners() {

        //返回首页
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DiaryMainActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_top_new, R.anim.slide_top_last);
            }
        });

        //添加日记
        add_diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Hint", "click the plus icon");
                startAnimation();
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(400);
                            mHandler.obtainMessage(ADD_MESSAGE).sendToTarget();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        });

        //选择item
        adapter.setOnItemClickListener(new DiaryAdapter.OnItemClickListener() {

            //进入3D画廊
            @Override
            public void onItemClick(View view, int position) {
                if (!selected) {
                    DiaryInfo = queryData();
                    MyHorizontalScrollView.initialPicNum = position;
                    Intent intent = new Intent(DiaryMainActivity.this, GalleryMainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_out, R.anim.zoom_in);
                }
            }

            //长按选择修改、删除、分享
            @Override
            public void onItemLongClick(View view, int position) {
                onClickItem = position;
                selected = true;
                selectedItemNum = position;
                invalidateOptionsMenu();
                longClickView.setVisibility(View.VISIBLE);
                view.findViewById(R.id.list_view_clickable).setBackgroundResource(R.color.light_grey);
            }

        });

        //根据RecyclerView的上下滚动，决定显示或隐藏加号按钮
        diaryListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    if ((Integer)add_diary.getTag() == 0) {
                        TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                        mShowAction.setDuration(300);
                        add_diary.startAnimation(mShowAction);
                        add_diary.setVisibility(View.VISIBLE);
                        add_diary.setTag(1);
                    }
                } else if (dy > 0) {
                    if ((Integer)add_diary.getTag() == 1) {
                        TranslateAnimation mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
                        mHiddenAction.setDuration(300);
                        add_diary.startAnimation(mHiddenAction);
                        add_diary.setVisibility(View.GONE);
                        add_diary.setTag(0);
                    }
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        //点击遮盖层时提示
        longClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCover();
            }
        });

    }

    //圆形加号旋转效果
    private void startAnimation() {
        animator = ObjectAnimator.ofFloat(add_diary, "rotation", 0f, 45f, 0f);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    //返回主界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_top_new, R.anim.slide_top_last);
        }
        return super.onKeyDown(keyCode, event);
    }
    //
    private void showShare(String address) {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle("一叶相机");
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用，qq必须设置
        oks.setTitleUrl("http://www.baidu.com");
        // text是分享文本，所有平台都需要这个字段,QQ邮箱必须
        oks.setText("快来看看我拍的美照哦~");
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        //oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        //imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //String s  = Environment.getExternalStorageDirectory().getPath() + "/temp.png";
        oks.setImagePath(address);//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("ShareSDK");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        //oks.setSiteUrl("http://www.baidu.com");
        // 启动分享GUI
        oks.show(this);
    }

}
