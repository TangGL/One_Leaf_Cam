package com.example.ray.finalex;

import org.lasque.tusdk.core.TuSdkApplication;

/**
 * Created by 80518 on 2016/11/29.
 */

public class MainApplication extends TuSdkApplication {

    @Override
    public void onCreate() {
        //this.setEnableLog(true);
        this.initPreLoader(this.getApplicationContext(), "7359ae806d6ef558-01-gwz6q1");
    }
}
