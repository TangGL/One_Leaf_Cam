package com.example.ray.finalex.DiaryPacket;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ray.finalex.MainActivity;
import com.example.ray.finalex.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 43cm on 2016/12/21.
 */

public class SearchActivity extends AppCompatActivity {

    private MyDBHelper myDBHelper;
    private DiaryAdapter adapter;
    private RecyclerView diaryListView;
    private TextView hintView;
    private SearchView searchView;
    private Toolbar toolbar;
    private List<Diary> SearchInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_search_layout);

        myDBHelper = new MyDBHelper(SearchActivity.this);

        initView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchInfo = getSearchList(query);
                adapter = new DiaryAdapter(SearchActivity.this, SearchInfo);
                setAdapterListener();
                diaryListView.setAdapter(adapter);
                if (SearchInfo.size() == 0) {
                    hintView.setVisibility(View.VISIBLE);
                } else {
                    hintView.setVisibility(View.GONE);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, DiaryMainActivity.class);
                startActivity(intent);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        diaryListView.setLayoutManager(layoutManager);
    }

    //初始化视图
    private void initView() {
        diaryListView = (RecyclerView) findViewById(R.id.diary_search_list);
        hintView = (TextView) findViewById(R.id.hint_search_view);

        toolbar = (Toolbar) findViewById(R.id.diary_search_toolbar);

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        searchView = (SearchView) findViewById(R.id.searchView);

        //设置我们的ToolBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //设置我们的SearchView
        searchView.setIconifiedByDefault(true);//设置展开后图标的样式,这里只有两种,一种图标在搜索框外,一种在搜索框内
        searchView.onActionViewExpanded();// 写上此句后searchView初始是可以点击输入的状态，如果不写，那么就需要点击下放大镜，才能出现输入框,也就是设置为ToolBar的ActionView，默认展开
        searchView.requestFocus();//输入焦点
        searchView.setSubmitButtonEnabled(true);//添加提交按钮，监听在OnQueryTextListener的onQueryTextSubmit响应
        searchView.setFocusable(true);//将控件设置成可获取焦点状态,默认是无法获取焦点的,只有设置成true,才能获取控件的点击事件
        searchView.setIconified(false);//输入框内icon不显示
        searchView.requestFocusFromTouch();//模拟焦点点击事件

        searchView.setFocusable(false);//禁止弹出输入法，在某些情况下有需要
        searchView.clearFocus();//禁止弹出输入法，在某些情况下有需要
    }

    //搜索标题任意位置带有keyword的item
    private List<Diary> getSearchList(String keywork) {
        List<Diary> list = new ArrayList<Diary>();
        myDBHelper = new MyDBHelper(this);
        Cursor c = myDBHelper.querySearch(keywork);
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

    //设置监听器
    private void setAdapterListener() {
        //选择item
        adapter.setOnItemClickListener(new DiaryAdapter.OnItemClickListener() {

            //进入3D画廊
            @Override
            public void onItemClick(View view, int position) {
                //设置浏览特定相册
                DiaryMainActivity.DiaryInfo = SearchInfo;
                MyHorizontalScrollView.initialPicNum = position;
                Intent intent = new Intent(SearchActivity.this, GalleryMainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_out, R.anim.zoom_in);
            }

            //长按选择修改、删除、分享
            @Override
            public void onItemLongClick(View view, int position) {
                Intent intentToMain = new Intent(SearchActivity.this, DiaryMainActivity.class);
                MyDBHelper myDBHelper = new MyDBHelper(SearchActivity.this);
                TextView timeText = (TextView) view.findViewById(R.id.diary_time_view);

                int index = 0;
                Cursor c = myDBHelper.query();
                while (c.moveToNext() && !timeText.getText().toString().equals(c.getString(c.getColumnIndex("time")))) {
                    index++;
                }

                intentToMain.putExtra("searchNum", index);
                startActivity(intentToMain);
            }

        });

    }


    //返回日记主界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        return super.onKeyDown(keyCode, event);
    }

}
