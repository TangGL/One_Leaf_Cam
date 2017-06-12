package com.example.ray.finalex.DiaryPacket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.example.ray.finalex.MainActivity;
import com.example.ray.finalex.R;

import java.util.ArrayList;

/**
 * Created by 43cm on 2016/12/19.
 */

public class GalleryMainActivity extends AppCompatActivity {

    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gallery_layout);

        mContext = getApplicationContext();

        MyHorizontalScrollView myScrollView = (MyHorizontalScrollView) findViewById(R.id.my_scroll_view);
        try {
            myScrollView.initDatas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}