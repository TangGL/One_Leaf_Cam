package com.example.ray.finalex;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import org.lasque.tusdk.core.TuSdkResult;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.utils.TuSdkWaterMarkOption;
import org.lasque.tusdk.core.utils.hardware.CameraHelper;
import org.lasque.tusdk.core.utils.image.BitmapHelper;
import org.lasque.tusdk.impl.activity.TuFragment;
import org.lasque.tusdk.impl.activity.TuFragmentActivity;
import org.lasque.tusdk.impl.components.camera.TuCameraFragment;
import org.lasque.tusdk.impl.components.camera.TuCameraOption;
import org.lasque.tusdk.modules.components.TuSdkHelperComponent;

/**
 * Created by 80518 on 2016/11/30.
 */

public class CameraComponent extends Base implements TuCameraFragment.TuCameraFragmentDelegate {

    private int totalNum = 0;

    public CameraComponent() {
        super();
    }

    @Override
    public void show(Activity activity) {
        if (activity == null) return;
        // 如果不支持摄像头显示警告信息
        if (CameraHelper.showAlertIfNotSupportCamera(activity)) return;
        // 组件选项配置
        // @see-http://tusdk.com/docs/android/api/org/lasque/tusdk/impl/components/camera/TuCameraOption.html
        TuCameraOption option = new TuCameraOption();

        // 保存到系统相册 (默认不保存, 当设置为true时, TuSdkResult.sqlInfo, 处理完成后将自动清理原始图片)
        option.setSaveToAlbum(true);

        // 是否开启滤镜支持 (默认: 关闭)
        option.setEnableFilters(true);

        // 默认是否显示滤镜视图 (默认: 不显示, 如果mEnableFilters = false, mShowFilterDefault将失效)
        option.setShowFilterDefault(true);

        // 开启滤镜配置选项
        option.setEnableFilterConfig(false);

        // 是否保存最后一次使用的滤镜
        option.setSaveLastFilter(true);

        // 自动选择分组滤镜指定的默认滤镜
        option.setAutoSelectGroupDefaultFilter(true);

        // 开启用户滤镜历史记录
        option.setEnableFiltersHistory(true);

        // 开启在线滤镜
        option.setEnableOnlineFilter(true);

        // 显示滤镜标题视图
        option.setDisplayFiltersSubtitles(true);

        // 是否开启音量键拍照功能，默认关闭
        option.setEnableCaptureWithVolumeKeys(true);

        // 开启长按拍摄 (默认：false)
        option.setEnableLongTouchCapture(true);

        //显示相册入口 (默认不显示)
        //option.setDisplayAlbumPoster(true);

        // 是否直接输出图片数据 (默认:false，输出已经处理好的图片Bitmap)
        // 设置为true都需使用 TuSdkResult.imageData获取一个byte[]数组
        //option.setOutputImageData(true);

        // 预览视图实时缩放比例 (默认:0.75f, 实时预览时，缩小到全屏大小比例，提升预览效率， 0 < mPreviewEffectScale
        // <= 1)
        option.setPreviewEffectScale(0.5f);

        // 设置水印选项 (默认为空，如果设置不为空，则输出的图片上将带有水印)
        option.setWaterMarkOption(getWaterMarkOption(activity));

        TuCameraFragment fragment = option.fragment();
        fragment.setDelegate(this);

        // see-http://tusdk.com/docs/android/api/org/lasque/tusdk/impl/components/base/TuSdkHelperComponent.html
        this.componentHelper = new TuSdkHelperComponent(activity);
        // 开启相机
        this.componentHelper.presentModalNavigationActivity(fragment, true);
    }

    /**
     * 获取一个拍摄结果。
     *
     * 相机的拍摄结果是TuSdkResult对象，依照设置，输出结果可能是 Bitmap、File或者ImageSqlInfo。
     * 在本例中，拍摄结束后直接关闭了相机界面，依照需求，还可以将拍摄结果作为输入源传给编辑组件，从而实现拍摄编辑一体操作。
     * 欢迎访问文档中心 http://tusdk.com/doc 查看更多示例。
     *
     * @param tuCameraFragment
     *            默认相机视图控制器
     * @param tuSdkResult
     *            拍摄结果
     */
    @Override
    public void onTuCameraFragmentCaptured(TuCameraFragment tuCameraFragment, TuSdkResult tuSdkResult) {
        tuCameraFragment.hubDismissRightNow();
        tuCameraFragment.dismissActivityWithAnim();
        TLog.d("onTuCameraFragmentCaptured: %s", tuSdkResult);
        totalNum++;
        // 默认输出为 Bitmap  -> result.image

        // 如果保存到临时文件 (默认不保存, 当设置为true时, TuSdkResult.imageFile, 处理完成后将自动清理原始图片)
        // option.setSaveToTemp(true);  ->  result.imageFile

        // 保存到系统相册 (默认不保存, 当设置为true时, TuSdkResult.sqlInfo, 处理完成后将自动清理原始图片)
        // option.setSaveToAlbum(true);  -> result.image
    }

    /**
     * 水印设置
     *
     * @return
     */
    public TuSdkWaterMarkOption getWaterMarkOption(Activity activity) {
        TuSdkWaterMarkOption option = new TuSdkWaterMarkOption();

        // 文字或者图片需要至少设置一个
        // 设置水印文字, 支持图文混排、图片或文字
        //option.setMarkText(loc);

        // 设置文字颜色 (默认:#FFFFFF)
        option.setMarkTextColor("#FFFFFF");

        // 文字大小 (默认: 24 SP)
        option.setMarkTextSize(50);

        // 文字阴影颜色 (默认:#000000)
        option.setMarkTextShadowColor("#000000");

        // 设置水印图片, 支持图文混排、图片或文字
        //option.setMarkImage(BitmapHelper.getRawBitmap(activity, R.raw.sample_watermark));

        // 文字和图片顺序 (仅当图片和文字都非空时生效，默认: 文字在右)
        //option.setMarkTextPosition(TuSdkWaterMarkOption.TextPosition.Right);

        // 设置水印位置 (默认: WaterMarkPosition.BottomRight)
        option.setMarkPosition(TuSdkWaterMarkOption.WaterMarkPosition.BottomRight);

        // 设置水印距离图片边距 (默认: 6dp)
        option.setMarkMargin(6);

        // 文字和图片间距 (默认: 2dp)
        option.setMarkTextPadding(5);

        return option;
    }

    /**
     * 获取一个拍摄结果 (异步方法)
     *
     * @param tuCameraFragment
     *            默认相机视图控制器
     * @param tuSdkResult
     *            拍摄结果
     * @return 是否截断默认处理逻辑 (默认: false, 设置为True时使用自定义处理逻辑)
     */
    @Override
    public boolean onTuCameraFragmentCapturedAsync(TuCameraFragment tuCameraFragment, TuSdkResult tuSdkResult) {
        TLog.d("onTuCameraFragmentCapturedAsync: %s", tuSdkResult);
        return false;
    }

    /**
     * 请求从相机界面跳转到相册界面。只有 设置mDisplayAlbumPoster为true (默认:false) 才会发生该事件
     *
     * @param tuCameraFragment
     *            系统相册控制器
     */
    @Override
    public void onTuAlbumDemand(TuCameraFragment tuCameraFragment) {
        tuCameraFragment.setDisplayAlbumPoster(true);
    }

    @Override
    public void onComponentError(TuFragment tuFragment, TuSdkResult tuSdkResult, Error error) {
        TLog.d("onComponentError: fragment - %s, result - %s, error - %s", tuFragment, tuSdkResult, error);
    }

    public int getNum() {
        return totalNum;
    }
}
