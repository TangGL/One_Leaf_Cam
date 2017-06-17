package com.example.ray.finalex;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ray.finalex.DiaryPacket.Diary;
import com.example.ray.finalex.DiaryPacket.DiaryMainActivity;
import com.example.ray.finalex.DiaryPacket.MyDBHelper;

import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.core.secret.StatisticsManger;
import org.lasque.tusdk.core.seles.tusdk.FilterManager;
import org.lasque.tusdk.core.utils.hardware.InterfaceOrientation;
import org.lasque.tusdk.impl.activity.TuFragmentActivity;
import org.lasque.tusdk.modules.components.ComponentActType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends TuFragmentActivity {

    CameraComponent cameraComponent;
    RichEditComponet richEditComponet;
    DynamicReceiver dynamicReceiver;
    IntentFilter dynamic_filter;
    ShowNotificService myShowNotificService = new ShowNotificService();
    public static final int layoutId = R.layout.activity_main;
    int total;
    int lasttotal;
    public MainActivity() {}

    public static RelativeLayout main_bg;

    private SensorManager mSensorManager;
    private Sensor mAccelerrometerSensor;

    MyDBHelper myDBHelper;
    List<Diary> WDL;


    //绑定的时候发送了一个消息，这里接受，但是同时自己也不断发送信息，所以可以不断更新UI
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Message message = new Message();
            //处理消息就调用mRunnable
            myHandler.post(mRunnable);
            myHandler.sendMessage(message);
        }
    };
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
           //处理
            total = cameraComponent.getNum() + richEditComponet.getNum();
            if (total != lasttotal) {
                lasttotal = total;
                Log.v("我在处理哦", Integer.toString(total));
                Intent intent = new Intent("dynamic");
                Log.v("次数", Integer.toString(total));
                intent.putExtra("tran", Integer.toString(total));
                sendBroadcast(intent);
            }
        }
    };
    //////////////
    private ServiceConnection sc = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        myShowNotificService = ((ShowNotificService.MyBinder)(service)).getService();
        //服务一调用，发送message让handle接收器接受。
        Message message = new Message();
        myHandler.sendMessage(message);
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
        myShowNotificService = null;
    }
};
    /** 初始化控制器 */
    @Override
    protected void initActivity() {
        super.initActivity();
        this.setRootView(layoutId, 0);

        // 设置应用退出信息ID 一旦设置将触发连续点击两次退出应用事件
        this.setAppExitInfoId(R.string.lsq_exit_info);
        cameraComponent = new CameraComponent();
        richEditComponet = new RichEditComponet();
    }
    /** 相机按钮容器 */
    private View mCameraButtonView;
    /** 编辑器按钮容器 */
    private View mEditorButtonView;
    /** 日记按钮容器 */
    private View mDiaryButtonView;

    @Override
    public void onCreate(Bundle bundle) { //处理widget的点击跳转事件
        super.onCreate(bundle);
        int tur = this.getIntent().getIntExtra("tur", 0);
        if (tur == 1) {
            showCameraComponent();
        }
        else if (tur == 2) {
            showEditorComponent();
        }
        else if (tur == 3) {
            showDiary();
        }

        main_bg = (RelativeLayout) findViewById(R.id.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerrometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    /**
     * 初始化视图
     */

    @Override
    protected void initView() {

        super.initView();

        // 异步方式初始化滤镜管理器 (注意：如果需要一开启应用马上执行SDK组件，需要做该检测，否则可以忽略检测)
        // 需要等待滤镜管理器初始化完成，才能使用所有功能
        TuSdk.messageHub().setStatus(this, R.string.lsq_initing);
        TuSdk.checkFilterManager(mFilterManagerDelegate);

        mCameraButtonView = this.getViewById(R.id.entry_camera);
        mEditorButtonView = this.getViewById(R.id.entry_editor);
        mDiaryButtonView = this.getViewById(R.id.entry_diary);

        mCameraButtonView.setOnClickListener(mButtonClickListener);
        mEditorButtonView.setOnClickListener(mButtonClickListener);
        mDiaryButtonView.setOnClickListener(mButtonClickListener);

        /**
         * 动态广播
         */
        dynamicReceiver = new DynamicReceiver();
        dynamic_filter = new IntentFilter();
        dynamic_filter.addAction("dynamic");
        registerReceiver(dynamicReceiver, dynamic_filter);
        total = 0;
        lasttotal = 0;
        //
        Intent intent = new Intent(this, ShowNotificService.class);
        //调用服务
        startService(intent);
        //绑定服务
        bindService(intent, sc, BIND_AUTO_CREATE);
    }

    /** 滤镜管理器委托 */
    private FilterManager.FilterManagerDelegate mFilterManagerDelegate = new FilterManager.FilterManagerDelegate() {

        @Override
        public void onFilterManagerInited(FilterManager manager) {
            TuSdk.messageHub().showSuccess(MainActivity.this, R.string.lsq_inited);
        }
    };

    /** 按钮点击事件处理 */
    private View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            if (v == mCameraButtonView) {
                showCameraComponent();
            }
            else if (v == mEditorButtonView) {
                showEditorComponent();
            }
            else if (v == mDiaryButtonView) {
                showDiary();
            }
        }
    };

    /** 打开相机组件 */
    private void showCameraComponent() {
        /*int total = cameraComponent.getNum() + richEditComponet.getNum();
            registerReceiver(dynamicReceiver, dynamic_filter);
            Intent intent = new Intent("dynamic");

            //Log.v("次数", Integer.toString(total));
            intent.putExtra("tran", Integer.toString(total));
            sendBroadcast(intent);*/
        cameraComponent.show(this);
    }

    /** 打开多功能编辑组件 */
    private void showEditorComponent() {
        /*int total = cameraComponent.getNum() + richEditComponet.getNum();
            registerReceiver(dynamicReceiver, dynamic_filter);
            Intent intent = new Intent("dynamic");

            //Log.v("次数", Integer.toString(total));
            intent.putExtra("tran", Integer.toString(total));
            sendBroadcast(intent);*/
        richEditComponet.show(this);
    }

    /** 打开日记组件 */
    private void showDiary() {
        Intent intent = new Intent(MainActivity.this, DiaryMainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_bottom_new, R.anim.slide_bottom_last);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mAccelerrometerSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Log.v("stop", "success");
        unbindService(sc);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dynamicReceiver);
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        private static final int SHAKE_TIMEOUT = 1000;
        private float [] accValues = null;
        private long lastShakeTime = 0;
        private long curShakeTiem = 0;

        private float [] newAccValues = new float[3];

        @Override
        public void onSensorChanged(SensorEvent event) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    curShakeTiem = System.currentTimeMillis();
                    if (curShakeTiem - lastShakeTime > SHAKE_TIMEOUT) {
                        newAccValues[0] = event.values[0];
                        newAccValues[1] = event.values[1];
                        newAccValues[2] = event.values[2];

                        if (accValues == null) {
                            accValues = new float[3];
                            accValues[0] = newAccValues[0];
                            accValues[1] = newAccValues[1];
                            accValues[2] = newAccValues[2];
                        } else {
                            if (Math.abs(accValues[0] - newAccValues[0]) > 2 || Math.abs(accValues[1] - newAccValues[1]) > 2
                                    || Math.abs(accValues[2] - newAccValues[2]) > 2) {
                                accValues[0] = newAccValues[0];
                                accValues[1] = newAccValues[1];
                                accValues[2] = newAccValues[2];

                                WDL = queryData(MainActivity.this);
                                int dairy_size = WDL.size();
                                if (dairy_size == 0) {
                                    Toast.makeText(MainActivity.this, "当前没有可替换背景哦!", Toast.LENGTH_SHORT).show();
                                } else {
                                    long random = System.currentTimeMillis();
                                    int index = (int)(random % dairy_size);

                                    String srcPath = WDL.get(index).getPic();
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
                                    Bitmap bitmap = null;
                                    bitmap = BitmapFactory.decodeFile(srcPath, options);
                                    options.inPreferredConfig = Bitmap.Config.ARGB_4444;

                                    options.inPurgeable = true;
                                    options.inInputShareable = true;
                                    options.inJustDecodeBounds = false;
                                    bitmap = BitmapFactory.decodeFile(srcPath, options);

                                    Drawable drawable = new BitmapDrawable(bitmap);

                                    main_bg.setBackground(drawable);

                                }
                            }
                        }
                        lastShakeTime = curShakeTiem;
                    }
                    break;
                default:
                    break;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

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
