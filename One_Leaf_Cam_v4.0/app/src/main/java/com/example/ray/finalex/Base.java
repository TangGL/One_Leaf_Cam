package com.example.ray.finalex;

import android.app.Activity;

import org.lasque.tusdk.modules.components.TuSdkHelperComponent;

/**
 * Created by 80518 on 2016/12/7.
 */

public abstract class Base {

    public TuSdkHelperComponent componentHelper;

    public abstract void show(Activity activity);
}
