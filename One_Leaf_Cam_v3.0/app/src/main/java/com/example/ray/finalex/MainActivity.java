package com.example.ray.finalex;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import com.example.ray.finalex.DiaryPacket.DiaryMainActivity;

import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.core.secret.StatisticsManger;
import org.lasque.tusdk.core.seles.tusdk.FilterManager;
import org.lasque.tusdk.core.utils.hardware.InterfaceOrientation;
import org.lasque.tusdk.impl.activity.TuFragmentActivity;
import org.lasque.tusdk.modules.components.ComponentActType;

import java.util.Date;

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
    public void onStop() {
        super.onStop();
        //Log.v("stop", "success");
        unbindService(sc);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dynamicReceiver);
    }
}
