package com.example.ray.finalex.DiaryPacket;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.example.ray.finalex.DiaryPacket.DiaryMainActivity;
import com.example.ray.finalex.DiaryPacket.MyDBHelper;
import com.example.ray.finalex.MainActivity;
import com.example.ray.finalex.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Exchanger;

import rx.functions.Action1;

import static android.R.attr.calendarTextColor;
import static android.R.attr.format;
import static android.R.attr.track;
import static android.R.attr.y;
import static com.example.ray.finalex.DiaryPacket.DiaryAdapter.calculateInSampleSize;


public class AddDiary extends AppCompatActivity {

    private EditText titleText;
    private EditText detailText;
    private ImageView pic;
    private ImageButton add_pic;
    private TextView cancelBtn;
    private TextView conformBtn;
    private ToggleButton add_location_btn;
    private TextView locationText;
    private TextView timeSelect;
    private final static int SELECT_ORIGINAL_PIC = 1;
    private String pic_path = "";
    private String address = "";
    private boolean permitted = false;
    private BDLocation location;
    private LocationClient mLocationClient;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日\n\t\t\tHH:mm");
    private static String time = "";
    private MyDBHelper myDBHelper;
    private int getChanged;
    private Toast mytoast = null;
    public void Toastulit(CharSequence s) {
        if (mytoast != null) {
            mytoast.cancel();
        }
        mytoast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        mytoast.show();
    }
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_diary);
        myDBHelper = new MyDBHelper(this);
        initView();
        setListeners();

        //finish();
    }

    private void initView() {
        titleText = (EditText) findViewById(R.id.edit_diary_title);
        detailText = (EditText) findViewById(R.id.edit_diary_detail);
        pic = (ImageView) findViewById(R.id.diary_pic);
        add_pic = (ImageButton) findViewById(R.id.add_diary_pic);
        cancelBtn = (TextView) findViewById(R.id.cancel_diary_btn);
        conformBtn = (TextView) findViewById(R.id.conform_diary_btn);
        add_location_btn = (ToggleButton) findViewById(R.id.add_location_icon);
        locationText = (TextView) findViewById(R.id.add_location_TextView);
        timeSelect = (TextView) findViewById(R.id.select_time_view);
        pic.setVisibility(View.GONE);

        time = getCurrentTime();

        Intent intent = getIntent();
        getChanged = intent.getIntExtra("selectNum", -1);
        if (getChanged != -1) {
            Diary changedInfo = DiaryMainActivity.DiaryInfo.get(getChanged);
            titleText.setText(changedInfo.getTitle());
            detailText.setText(changedInfo.getDetail());
            locationText.setText(changedInfo.getAddress());

            pic_path = changedInfo.getPic();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeFile(pic_path, options);
            options.inSampleSize = calculateInSampleSize(options, 512, 512);
            options.inPreferredConfig = Bitmap.Config.ARGB_4444;
            /* 下面两个字段需要组合使用 */
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(pic_path, options);

            pic.setImageBitmap(bitmap);
            pic.setVisibility(View.VISIBLE);

            time = changedInfo.getTime();
        }

        try {
            Date date = format.parse(AddDiary.time);
            calendar.setTime(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        timeSelect.setText(time);

    }

    private void setListeners() {

        timeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater factory = LayoutInflater.from(AddDiary.this);
                View new_view = factory.inflate(R.layout.time_pick_layout, null);

                DatePicker datePicker = (DatePicker) new_view.findViewById(R.id.dpPicker);
                final TimePicker timePicker = (TimePicker) new_view.findViewById(R.id.tpPicker);

                datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), new DatePicker.OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 获取一个日历对象，并初始化为当前选中的时间
                        calendar.set(year, monthOfYear, dayOfMonth);
                    }
                });

                timePicker.setIs24HourView(true);
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), hourOfDay, minute);
                    }
                });


                AlertDialog.Builder builder = new AlertDialog.Builder(AddDiary.this, R.style.MyAlertDialogStyle);
                AlertDialog alert = builder.setView(new_view)
                        .setTitle("选择时间")
                        .setNegativeButton("放弃修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("保存修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //update方法
                                AddDiary.time = format.format(calendar.getTime());
                                try {
                                    Date date = format.parse(AddDiary.time);
                                    calendar.setTime(date);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                timeSelect.setText(AddDiary.time);
                                dialog.dismiss();
                            }
                        }).create();
                alert.show();

            }
        });

        add_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                startActivityForResult(intent, SELECT_ORIGINAL_PIC);
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, SELECT_ORIGINAL_PIC);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getChanged != -1) {
                    addPreInfo();
                }
                Intent intent = new Intent(AddDiary.this, DiaryMainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_last);
            }
        });

        conformBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String diary_tile = titleText.getText().toString();
                String diary_detail = detailText.getText().toString();
                if (diary_tile.equals("")) {
                    //Toast.makeText(AddDiary.this, "标题不能为空", Toast.LENGTH_SHORT).show();
                    Toastulit("标题不能为空");
                } else if (pic_path.equals("")) {
                    //Toast.makeText(AddDiary.this, "请选择一张图片", Toast.LENGTH_SHORT).show();
                    Toastulit("请选择一张图片");
                } else {
                    MyDBHelper mDBHelper = new MyDBHelper(AddDiary.this);
                    time = timeSelect.getText().toString();
                    if (!mDBHelper.checkTime(AddDiary.time)) {
                        mDBHelper.insert2DB(AddDiary.time, diary_tile, pic_path, diary_detail, address);
                        Intent intent = new Intent(AddDiary.this, DiaryMainActivity.class);
                        startActivity(intent);
                    } else {
//                        Toast.makeText(AddDiary.this, "时间冲突了", Toast.LENGTH_SHORT).show();
                        Toastulit("时间冲突了");
                    }
                }
            }
        });

        add_location_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //定位当前位置
                    if (MyCheckPermissions()) {
                        getLocation();
                    }
                } else {
                    locationText.setText("");
                    address = "";
                }

            }
        });

    }

    private void addPreInfo() {
        MyDBHelper mDBHelper = new MyDBHelper(AddDiary.this);
        Diary preInfo = DiaryMainActivity.DiaryInfo.get(getChanged);
        AddDiary.time = preInfo.getTime();
        String diary_tile = preInfo.getTitle();
        pic_path = preInfo.getPic();
        String diary_detail = preInfo.getDetail();
        address = preInfo.getAddress();
        mDBHelper.insert2DB(AddDiary.time, diary_tile, pic_path, diary_detail, address);
    }

    public String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日\n\t\t\tHH:mm");
        String curTime = format.format(calendar.getTime());
        return curTime;
    }

    private void getLocation() {
        SDKInitializer.initialize(getApplicationContext());
        mLocationClient = new LocationClient(getApplicationContext());
        initLocation();
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span); //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true); //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true); //可选，默认false,设置是否使用gps
        option.setLocationNotify(true); //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true); //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true); //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false); //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false); //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false); //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    private BDLocationListener myLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            location = bdLocation;

            if (bdLocation != null) {
                updateView();
            }
        }
    };

    private void updateView() {
        address = location.getAddrStr();
        locationText.setText(address);
        mLocationClient.unRegisterLocationListener(myLocationListener);
        mLocationClient.stop();
    }

    private boolean MyCheckPermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            permitted = true;
                        } else {
                            //Toast.makeText(AddDiary.this, "Cannot get the position", Toast.LENGTH_SHORT).show();
                            Toastulit("Cannot get the position");
                            add_location_btn.setChecked(false);
                            permitted = false;
                        }
                    }
                });
        return permitted;
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

                    BitmapFactory.Options newOpts = new BitmapFactory.Options();
                    newOpts.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(pic_path, newOpts);
                    newOpts.inJustDecodeBounds = false;
                    int w = newOpts.outWidth;
                    int h = newOpts.outHeight;
                    //计算出取样率
                    int be = calculateInSampleSize(newOpts, w, h);
                    newOpts.inSampleSize = be;
                    bitmap = BitmapFactory.decodeFile(pic_path, newOpts);
                    pic.setImageBitmap(bitmap);
                    pic.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getChanged != -1) {
                addPreInfo();
            }
            Intent intent = new Intent(this, DiaryMainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_last);
        }
        return super.onKeyDown(keyCode, event);
    }

}

