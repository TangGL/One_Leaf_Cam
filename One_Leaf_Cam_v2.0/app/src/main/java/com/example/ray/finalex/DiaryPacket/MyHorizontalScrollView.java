package com.example.ray.finalex.DiaryPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ray.finalex.R;
import com.nineoldandroids.view.ViewHelper;

import static android.support.v7.appcompat.R.attr.alpha;
import static com.example.ray.finalex.DiaryPacket.DiaryAdapter.calculateInSampleSize;

/**
 * Created by 43cm on 2016/12/19.
 */

public class MyHorizontalScrollView extends HorizontalScrollView {

    private Context mContext;

    /** The item data to display on screen */
    private List<Diary> mDatas;

    /** The item parent layout */
    private LinearLayout mContainer;

    /** The screent center of HorizontalScrollView */
    private int mScrollViewCenter;

    /** The width of item */
    private int mChildWidth;

    /** The index of the initial centered displayed item */
    private int mInitialDisplayedItemIndex;

    /** Record the initial scrollX of the HorizontalScrollView */
    private int mInitialScrollX;

    /** A flag indicate which direction the HorizontalScrollView moved */
    private boolean mIsScrollRight = false;

    public static int initialPicNum;

    public MyHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mDatas = DiaryMainActivity.DiaryInfo;

    }

    /**
     * Initialize the display item data
     */
    public void initDatas() {
        mDatas = DiaryMainActivity.DiaryInfo;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int count = mDatas.size();

//        mContainer = (LinearLayout) getChildAt(0);
        mContainer = (LinearLayout) findViewById(R.id.container);

        for (int i = 0; i < count; i++) {

            View gallery_item_layout = inflater.inflate(R.layout.gallery_item, mContainer, false);

//            ImageView convertView = (ImageView) inflater.inflate(R.layout.gallery_item, null, false);

            TextView titleView = (TextView) gallery_item_layout.findViewById(R.id.id_index_gallery_item_title);
            ImageView imageView = (ImageView) gallery_item_layout.findViewById(R.id.id_index_gallery_item_image);
            TextView detailView = (TextView) gallery_item_layout.findViewById(R.id.id_index_gallery_item_detail);

            String pic_path = mDatas.get(i).getPic();
            String title = mDatas.get(i).getTitle();
            String detail = mDatas.get(i).getDetail();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeFile(pic_path, options);
            options.inSampleSize = calculateInSampleSize(options,600,600);
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            /* 下面两个字段需要组合使用 */
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(pic_path, options);

            imageView.setImageBitmap(createReflectedImage(bitmap));

            titleView.setText(title);
            detailView.setText(detail);

            mContainer.addView(gallery_item_layout);
        }
    }

    public static Bitmap createReflectedImage(Bitmap originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Matrix matrix = new Matrix();
        // 实现图片翻转90度
        matrix.preScale(1, -1);
        // 创建倒影图片（是原始图片的一半大小）
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height / 2, width, height / 2, matrix, false);
        // 创建总图片（原图片 + 倒影图片）
        Bitmap finalReflection = Bitmap.createBitmap(width, (height + height / 2), Bitmap.Config.ARGB_8888);
        // 创建画布
        Canvas canvas = new Canvas(finalReflection);
        canvas.drawBitmap(originalImage, 0, 0, null);
        //把倒影图片画到画布上
        canvas.drawBitmap(reflectionImage, 0, height + 1, null);
        Paint shaderPaint = new Paint();
        //创建线性渐变LinearGradient对象
        LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0, finalReflection.getHeight() + 1, 0x70ffffff,
                0x00ffffff, Shader.TileMode.MIRROR);
        shaderPaint.setShader(shader);
        shaderPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //画布画出反转图片大小区域，然后把渐变效果加到其中，就出现了图片的倒影效果。
        canvas.drawRect(0, height + 1, width, finalReflection.getHeight(), shaderPaint);
        return finalReflection;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            mChildWidth = mContainer.getChildAt(0).getMeasuredWidth();
            mScrollViewCenter = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();

            //Find which item is most closed to the screen center
            int itemCountOnScreen = Math.round((float)getWidth() / mChildWidth);
            int centerToScroll = 0;
            int centerDistance = Integer.MAX_VALUE;
            for (int j = 0; j < itemCountOnScreen; j++) {
                View child = mContainer.getChildAt(j);
                int childCenter = getCenterOfChildView(child);
                int delta = Math.abs(childCenter - mScrollViewCenter);
                if (delta < centerDistance) {
                    centerDistance = delta;
                    centerToScroll = childCenter - mScrollViewCenter;
                    mInitialDisplayedItemIndex = j;
                }
            }

            //Initial the location of the HorizontalScrollView
            this.scrollTo(centerToScroll+ initialPicNum * mChildWidth, 0);
            mInitialScrollX = centerToScroll > 0 ? centerToScroll : 0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                //Force to move to the center of an item.
                boundToMoveToCenteredItem();
                return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mIsScrollRight = l > oldl;
        startTransform();
    }

    /**
     * Transform the child view
     */
    private void startTransform() {
        int count = mContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = mContainer.getChildAt(i);
            int childCenter = getCenterOfChildView(child);
            int delta = Math.abs(childCenter - mScrollViewCenter);
            float scaleFactor = 1.0f;
            if (delta > mChildWidth) {
                scaleFactor = 0.3f;
            } else {
                scaleFactor = 0.3f + 0.7f * ((float)(mChildWidth - delta)/mChildWidth);
            }

            /**
             * 此处必须使用属性动画Property-Animation，视图动画View-Animation无法实现连续滑动的3D效果，会出现闪屏！
             *
             * 1. View-Animation(ScaleAnimation, TranslateAnimation, RotateAnimation)：
             *    改变的是View的绘制效果，View的属性没有改变，其位置与大小都不变。
             * 2. Property-Animation(ValueAnimator, ObjectAnimator)
             *    改变是View的属性，View的属性变化的时候，属性动画会自动刷新屏幕，属性动画改变的是对象的真实属性。
             *
             * Here we use android third party property animation sdk: nineoldandroids-2.4.0.jar
             * This library also includes support for animating rotation, translation, alpha,
             * and scale on platforms prior to Android 3.0(API-11)
             * Reference to:
             * http://nineoldandroids.com/
             * https://github.com/JakeWharton/NineOldAndroids
             */
            ViewHelper.setPivotX(child, child.getWidth() / 2);
            ViewHelper.setPivotY(child, child.getHeight() / 2);
            ViewHelper.setScaleX(child, scaleFactor);
            ViewHelper.setScaleY(child, scaleFactor);
            ViewHelper.setAlpha(child, alpha);
        }
    }

    /**
     * Get the center of child view
     */
    private int getCenterOfChildView(View view) {
        int left = view.getLeft();
        int scrollX = this.getScrollX() ;
        return left - scrollX + view.getWidth() / 2;
    }

    /**
     * Move to the center of an item according to the direction and distance.
     */
    private void boundToMoveToCenteredItem() {
        int centerToScroll = 0;
        int centerDistance = Integer.MAX_VALUE;
        int scrollX = this.getScrollX() - mInitialScrollX;
        int current = mInitialDisplayedItemIndex + scrollX / mChildWidth;
        int newCenterItemIndex = mInitialDisplayedItemIndex;

        //Find which item is most closed to the screen center
        for (int i = current - 1; i <= current + 1; i++) {
            if (i >= 0 && i < mContainer.getChildCount()) {
                View child = mContainer.getChildAt(i);
                int childCenter = getCenterOfChildView(child);
                int delta = Math.abs(childCenter - mScrollViewCenter);
                if (delta < centerDistance) {
                    centerDistance = delta;
                    centerToScroll = childCenter - mScrollViewCenter;
                    newCenterItemIndex = i;
                }
            }
        }

        if (centerToScroll < 0) {
            //The item that most closed to mScrollViewCenter is located at left of mScrollViewCenter
            if (mIsScrollRight) {
                if (newCenterItemIndex + 1 < mContainer.getChildCount() - 1) {
                    //Move to the next item(index = newCenterItemIndex + 1)
                    this.smoothScrollBy(mChildWidth + centerToScroll, 0);
                } else {
                    //Move to the next item(index = newCenterItemIndex)
                    this.smoothScrollBy(centerToScroll, 0);
                }
            } else {
                //Move to the next item(index = newCenterItemIndex)
                this.smoothScrollBy(centerToScroll, 0);
            }
        } else {
            //The item that most closed to mScrollViewCenter is located at right of mScrollViewCenter
            if (mIsScrollRight) {
                //Move to the next item(index = newCenterItemIndex)
                this.smoothScrollBy(centerToScroll, 0);
            } else {
                if (newCenterItemIndex - 1 > 0) {
                    //Move to the previous item(index = newCenterItemIndex - 1)
                    this.smoothScrollBy(centerToScroll - mChildWidth, 0);
                } else {
                    //Move to the next item(index = newCenterItemIndex)
                    this.smoothScrollBy(centerToScroll, 0);
                }
            }
        }
    }
}