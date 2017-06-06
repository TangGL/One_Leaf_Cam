package com.example.ray.finalex;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.ray.finalex.onekeyshare.OnekeyShare;

import static com.example.ray.finalex.DiaryPacket.DiaryAdapter.calculateInSampleSize;

/**
 * Created by lwj on 2016/12/25.
 */

public class ShareNotific extends AppCompatActivity {
    private final static int SELECT_ORIGINAL_PIC = 2;
    private String pic_path = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guodu);
        //
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, SELECT_ORIGINAL_PIC);
    }
    private void showShare(String address) {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle("一叶相机");
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用，qq必须设置
        oks.setTitleUrl("http://www.baidu.com");
        // text是分享文本，所有平台都需要这个字段,QQ邮箱必须
        oks.setText("快来看看我拍的美照哦~");
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        //oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        //imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //String s  = Environment.getExternalStorageDirectory().getPath() + "/temp.png";
        oks.setImagePath(address);//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("ShareSDK");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        //oks.setSiteUrl("http://www.baidu.com");
        // 启动分享GUI
        oks.show(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_ORIGINAL_PIC) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    pic_path = cursor.getString(columnIndex);  //获取照片路径
                    cursor.close();
                    showShare(pic_path);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
