package com.example.ray.finalex;

import android.animation.Animator;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.core.secret.StatisticsManger;
import org.lasque.tusdk.core.seles.tusdk.FilterManager;
import org.lasque.tusdk.impl.activity.TuFragmentActivity;
import org.lasque.tusdk.modules.components.ComponentActType;

public class MainActivity extends TuFragmentActivity {

    public static final int layoutId = R.layout.activity_main;
    CameraComponent cameraComponent;
    RichEditComponet richEditComponet;
    DynamicReceiver dynamicReceiver;
    IntentFilter dynamic_filter;

    public MainActivity() {}

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

    /** 退出按钮容器 */
    private View mQuitButtonView;

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
        mQuitButtonView = this.getViewById(R.id.entry_quit);

        mCameraButtonView.setOnClickListener(mButtonClickListener);
        mEditorButtonView.setOnClickListener(mButtonClickListener);
        mQuitButtonView.setOnClickListener(mButtonClickListener);

        /**
         * 动态广播
         */
        dynamicReceiver = new DynamicReceiver();
        dynamic_filter = new IntentFilter();
        dynamic_filter.addAction("dynamic");
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
            else if (v == mQuitButtonView) {
                showDiary();
            }
        }
    };

    /** 打开相机组件 */
    private void showCameraComponent() {
        int total = cameraComponent.getNum() + richEditComponet.getNum();
        if (total > 0) {
            registerReceiver(dynamicReceiver, dynamic_filter);
            Intent intent = new Intent("dynamic");

            Log.v("次数", Integer.toString(total));
            intent.putExtra("tran", Integer.toString(total));
            sendBroadcast(intent);
        }
        cameraComponent.show(this);
    }

    /** 打开多功能编辑组件 */
    private void showEditorComponent() {
        int total = cameraComponent.getNum() + richEditComponet.getNum();
        if (total > 0) {
            registerReceiver(dynamicReceiver, dynamic_filter);
            Intent intent = new Intent("dynamic");

            Log.v("次数", Integer.toString(total));
            intent.putExtra("tran", Integer.toString(total));
            sendBroadcast(intent);
        }
        richEditComponet.show(this);
    }

    /** 打开日记组件 */
    private void showDiary() {
    }
}
