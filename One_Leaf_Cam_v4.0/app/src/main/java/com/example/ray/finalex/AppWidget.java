package com.example.ray.finalex;

import android.app.Dialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.ray.finalex.DiaryPacket.Diary;
import com.example.ray.finalex.DiaryPacket.DiaryMainActivity;
import com.example.ray.finalex.DiaryPacket.GalleryMainActivity;
import com.example.ray.finalex.DiaryPacket.MyDBHelper;
import com.example.ray.finalex.DiaryPacket.MyHorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 * Created by WangLe on 2017/05/10.
 */
public class AppWidget extends AppWidgetProvider {

    public static int wid_num = 0; //静态变量，记录日记位置
    public static int layi = 1;
    List<Diary> WDL;
    MyDBHelper myDBHelper;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //为按钮设置跳转事件,用Extra表示不同跳转关系
        Intent clickInt = new Intent(context, MainActivity.class);
        clickInt.putExtra("tur", (int) 1);
        PendingIntent pi = PendingIntent.getActivity(context, 0, clickInt, 0);
        //为按钮设置点击事件
        Intent BInt = new Intent("widget_pre_action");
        PendingIntent Bpi = PendingIntent.getBroadcast(context, 0, BInt, 0);
        Intent BInt1 = new Intent("widget_next_action");
        PendingIntent Bpi1 = PendingIntent.getBroadcast(context, 0, BInt1, 0);
        //图片点击事件
        Intent BIntImg = new Intent("widget_Img_action");
        PendingIntent BpiImg = PendingIntent.getBroadcast(context, 0, BIntImg, 0);


        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        rv.setOnClickPendingIntent(R.id.widget_camera, pi);
        clickInt.putExtra("tur", (int) 2);
        //设置不同的requestCode来区分跳转相同Activity的PendingIntent
        PendingIntent pi1 = PendingIntent.getActivity(context, 1, clickInt, 0);
        rv.setOnClickPendingIntent(R.id.widget_editor, pi1);
        clickInt.putExtra("tur", (int) 3);
        PendingIntent pi2 = PendingIntent.getActivity(context, 2, clickInt, 0);
        rv.setOnClickPendingIntent(R.id.widget_diary, pi2);

        rv.setOnClickPendingIntent(R.id.widget_pre, Bpi);
        rv.setOnClickPendingIntent(R.id.widget_next, Bpi1);

        rv.setOnClickPendingIntent(R.id.widget_lin, BpiImg);
        appWidgetManager.updateAppWidget(appWidgetIds, rv);

        updateWidget(context, wid_num);
    }

    @Override
    public void onEnabled(Context context) {
        updateWidget(context, wid_num);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    //处理按钮点击事件
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        WDL = queryData(context);
        if (intent.getAction().equals("widget_pre_action")) {
            if (wid_num > 0)
                wid_num--;
            if (wid_num >= WDL.size())
                wid_num = 0;
            updateWidget(context, wid_num);
        }//切换日记
        if (intent.getAction().equals("widget_next_action")) {
            if (wid_num < (WDL.size() - 1))
                wid_num++;
            if (wid_num >= WDL.size())
                wid_num = 0;
            updateWidget(context, wid_num);
        }
        if (intent.getAction().equals("widget_Img_action")) {
            if (wid_num >= WDL.size())
                wid_num = 0;
            if (WDL.size() != 0) {
                Intent inte = new Intent(context, DiaryMainActivity.class);
                inte.putExtra("tur", wid_num);
                context.startActivity(inte);
            }
            updateWidget(context, wid_num);
        }//进入画廊

    }

    //由数据库日记信息和按钮点击事件更新Widget
    public void updateWidget(Context context, int num) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        WDL = queryData(context);

        if (WDL.size() == 0) {
            rv.setTextViewText(R.id.widget_title, "");
            rv.setTextViewText(R.id.widget_tim, "");
            rv.setTextViewText(R.id.widget_location, "");
            rv.setViewVisibility(R.id.widget_img, View.GONE);
            rv.setViewVisibility(R.id.widget_icon, View.GONE);
            rv.setViewVisibility(R.id.widget_null, View.VISIBLE);
        } else {
            rv.setTextViewText(R.id.widget_title, WDL.get(num).getTitle());
            rv.setTextViewText(R.id.widget_tim, WDL.get(num).getTime());
            rv.setTextViewText(R.id.widget_location, WDL.get(num).getAddress());

            String srcPath = WDL.get(num).getPic();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeFile(srcPath, options);
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            /* 下面两个字段需要组合使用 */
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(srcPath, options);

            rv.setViewVisibility(R.id.widget_img, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_icon, View.VISIBLE);
            rv.setViewVisibility(R.id.widget_null, View.GONE);
            //为图片切换设置动画
            RemoteViews subViews = new RemoteViews(context.getPackageName(), R.layout.img1);
            rv.removeAllViews(R.id.widget_lin);
            rv.addView(R.id.widget_lin, subViews);
            rv.setImageViewBitmap(R.id.widget_img, bitmap);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(
                context.getApplicationContext());
        ComponentName componentName = new ComponentName(context.getApplicationContext(),
                AppWidget.class);
        appWidgetManager.updateAppWidget(componentName, rv);
    }

    //获取数据库日记信息
    public List<Diary> queryData(Context context) {
        List<Diary> list = new ArrayList<Diary>();
        myDBHelper = new MyDBHelper(context);
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


}

