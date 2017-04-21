package com.example.ray.finalex;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by 80518 on 2016/12/13.
 */

public class GetLocActivity extends Activity {

    private LocationManager locationManager;
    private String provider = null;
    public static final int SHOW_LOCATION = 0;
    private String latitude = "23.0608";
    private String longitude = "113.3875";
    private Location location = null;
    private String loc = "guangzhou";
    private Button get_button;
    private TextView get_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getloc);

        get_button = (Button) findViewById(R.id.get_button);
        get_text = (TextView) findViewById(R.id.entry_text);

        //获取地理位置管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
        } else {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location != null){
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
        }
        Log.d("chaxuna", longitude);
        showLoc(latitude, longitude);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            //移除监听器
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.removeUpdates(locationListener);
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            GetLocActivity.this.location = location;
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLatitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    public void showLoc(final String latitude, final String longitude) {
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
                    Message message = new Message();
                    message.what = SHOW_LOCATION;
                    message.obj = javaBean;
                    handler.sendMessage(message);

                    if (javaBean.getMsg().equals("查询成功")) {
                        loc = javaBean.getResult().get(0).getDistrictName() + " "  +
                                javaBean.getResult().get(1).getDistrictName() + " "  +
                                javaBean.getResult().get(2).getDistrictName();
                    }
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
    }

    private Handler handler = new Handler(){
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
    };

    public String getLoc() {
        //Log.d("qwertyu", loc);
        //Toast.makeText(this, loc, Toast.LENGTH_SHORT).show();
        return loc;
    }
}
