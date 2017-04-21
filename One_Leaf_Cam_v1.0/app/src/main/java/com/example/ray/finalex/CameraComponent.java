package com.example.ray.finalex;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import org.lasque.tusdk.core.TuSdkResult;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.utils.TuSdkWaterMarkOption;
import org.lasque.tusdk.core.utils.hardware.CameraHelper;
import org.lasque.tusdk.core.utils.image.BitmapHelper;
import org.lasque.tusdk.impl.activity.TuFragment;
import org.lasque.tusdk.impl.components.camera.TuCameraFragment;
import org.lasque.tusdk.impl.components.camera.TuCameraOption;
import org.lasque.tusdk.modules.components.TuSdkHelperComponent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 80518 on 2016/11/30.
 */

public class CameraComponent extends Base implements TuCameraFragment.TuCameraFragmentDelegate {

    private String loc = "Guangzhou";
    private String latitude = "23.0608";
    private String longitude = "113.3875";
    public static final int SHOW_LOCATION = 0;
    private int totalNum = 0;

    public CameraComponent() {
        super();
    }

    public void setLoc(String loc1) {
        loc = loc1;
    }

    /**
     * 获取拍摄的次数
     */
    public int getNum() {
        return totalNum;
    }

    /*private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case SHOW_LOCATION:
                    JavaBean javaBean = (JavaBean) msg.obj;

                    if (javaBean.getMsg().equals("查询成功")) {
                        loc = javaBean.getResult().get(0).getDistrictName() + " "  +
                                javaBean.getResult().get(1).getDistrictName() + " "  +
                                javaBean.getResult().get(2).getDistrictName();
                        Log.d("chaxun", loc);
                        //get_text.setText(loc);
                    }
                    break;
                default:
                    break;
            }
        }
    };*/

    @Override
    public void show(Activity activity) {
        //showLoc(latitude, longitude);

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
        //option.setWaterMarkOption(getWaterMarkOption(activity));

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

    /*public void showLoc(final String latitude, final String longitude) {
        Log.d("chaxuns", latitude);
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = null;
                String result = null;
                StringBuffer sbf = new StringBuffer();
                String httpUrl = "http://apis.baidu.com/wxlink/here/here";
                String httpArg = "lat=" + latitude + "&lng=" + longitude + "&cst=1";
                httpUrl = httpUrl + "?" + httpArg;
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(httpUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    // 填入apikey到HTTP header
                    connection.setRequestProperty("apikey",  "1917fef33a3970db4dc99a7296328052");
                    connection.connect();
                    InputStream is = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String strRead = null;
                    while ((strRead = reader.readLine()) != null) {
                        sbf.append(strRead);
                        sbf.append("\r\n");
                    }
                    reader.close();
                    result = sbf.toString();
                    Gson gson = new Gson();
                    JavaBean javaBean = gson.fromJson(result, JavaBean.class);
                    Log.d("chaxuna", javaBean.getResult().get(0).getDistrictName());
                    Message message = new Message();
                    message.what = SHOW_LOCATION;
                    message.obj = javaBean;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }*/

    /**
     * 水印设置
     *
     * @return
     */
    public TuSdkWaterMarkOption getWaterMarkOption(Activity activity) {
        TuSdkWaterMarkOption option = new TuSdkWaterMarkOption();
        Log.d("qwertyui", loc);

        // 文字或者图片需要至少设置一个
        // 设置水印文字, 支持图文混排、图片或文字
        option.setMarkText(loc);

        // 设置文字颜色 (默认:#FFFFFF)
        option.setMarkTextColor("#FFFFFF");

        // 文字大小 (默认: 24 SP)
        option.setMarkTextSize(26);

        // 文字阴影颜色 (默认:#000000)
        option.setMarkTextShadowColor("#000000");

        // 设置水印图片, 支持图文混排、图片或文字
        //option.setMarkImage(BitmapHelper.getRawBitmap(activity, R.raw.sample_watermark));

        // 文字和图片顺序 (仅当图片和文字都非空时生效，默认: 文字在右)
        //option.setMarkTextPosition(TuSdkWaterMarkOption.TextPosition.Right);

        // 设置水印位置 (默认: WaterMarkPosition.BottomRight)
        option.setMarkPosition(TuSdkWaterMarkOption.WaterMarkPosition.BottomRight);

        // 设置水印距离图片边距 (默认: 6dp)
        option.setMarkMargin(20);

        // 文字和图片间距 (默认: 2dp)
        option.setMarkTextPadding(20);

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
        //tuCameraFragment.setDisplayAlbumPoster(true);
    }

    @Override
    public void onComponentError(TuFragment tuFragment, TuSdkResult tuSdkResult, Error error) {
        TLog.d("onComponentError: fragment - %s, result - %s, error - %s", tuFragment, tuSdkResult, error);
    }
}
