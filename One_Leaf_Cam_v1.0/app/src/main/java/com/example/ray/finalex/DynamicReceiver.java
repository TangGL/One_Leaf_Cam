package com.example.ray.finalex;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

/**
 * Created by lwj on 2016/12/13.
 */
public class DynamicReceiver extends BroadcastReceiver {
    private static final String DYNAMICACTION = "dynamic";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DYNAMICACTION)) {
            Bitmap image = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                image = ((BitmapDrawable)context.getDrawable(R.mipmap.icon3)).getBitmap();
            }

            Bundle bundle = intent.getExtras();

            Notification.Builder builder = new Notification.Builder(context);
            builder.setContentTitle("一叶相机")
                    .setContentText("美化" + bundle.getString("tran") + "张图啦~分享一下啦~")
                    .setTicker("分享一下啦~")
                    .setAutoCancel(true)
                    .setLargeIcon(image)
                    .setSmallIcon(R.mipmap.icon3);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent mIntent = new Intent(context, MainActivity.class);
            PendingIntent mPending = PendingIntent.getActivity(context, 0, mIntent, 0);
            builder.setContentIntent(mPending);


            Notification notify = builder.build();

            manager.notify(0, notify);
        }
    }
}
