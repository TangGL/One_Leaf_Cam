package com.example.ray.finalex.DiaryPacket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ray.finalex.DiaryPacket.Diary;
import com.example.ray.finalex.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 43cm on 2016/12/14.
 */

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private List<Diary> diary_list;
    private LayoutInflater mInflater;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public DiaryAdapter(Context context,List<Diary> items) {
        super();
        diary_list = items;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.diary_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        holder.time = (TextView)view.findViewById(R.id.diary_time_view);
        holder.title =(TextView)view.findViewById(R.id.diary_title_view);
        holder.location = (TextView)view.findViewById(R.id.diary_location_text);
        holder.pic = (ImageView)view.findViewById(R.id.diary_pic_view);
        holder.layout = (LinearLayout)view.findViewById(R.id.list_view_clickable);
        return holder;
    }

    @Override
    public void onBindViewHolder( final ViewHolder viewHolder, final int i) {
        viewHolder.time.setText(diary_list.get(i).getTime());
        viewHolder.title.setText( diary_list.get(i).getTitle());
        viewHolder.location.setText( diary_list.get(i).getAddress());

        String srcPath = diary_list.get(i).getPic();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeFile(srcPath, options);
        options.inSampleSize = calculateInSampleSize(options,512,512);
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            /* 下面两个字段需要组合使用 */
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(srcPath, options);

        viewHolder.pic.setImageBitmap(bitmap);

        if ( mOnItemClickListener != null) {
            viewHolder.layout.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto- - generated method stub
                    mOnItemClickListener.onItemClick(viewHolder.itemView, i);
                }
            });

            viewHolder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnItemClickListener.onItemLongClick(viewHolder.itemView, i);
                    return false;
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return diary_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
        public TextView time;
        public TextView title;
        public ImageView pic;
        public TextView location;
        public LinearLayout layout;
    }

    public void refresh(List<Diary> list) {
        diary_list = list;
        notifyDataSetChanged();
    }

    public void remove(int position) {
        diary_list.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}

