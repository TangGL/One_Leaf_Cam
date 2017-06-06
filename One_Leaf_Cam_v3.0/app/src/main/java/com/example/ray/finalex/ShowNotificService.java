package com.example.ray.finalex;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * Created by lwj on 2016/12/25.
 */

public class ShowNotificService extends Service {
    @Nullable
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public final IBinder binder = new MyBinder();
    public  class MyBinder extends Binder {
        ShowNotificService getService() {
            return ShowNotificService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }









    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
