package com.mryan.mdex.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @description：TODO
 * @Author MRyan
 * @Date 2020/6/8 18:51
 * @Version 1.0
 */
public class MyReceiver extends BroadcastReceiver {
    public static final String action = "com.mryan.mdex.until.intent.action.MyReceiver";
    private String pkgName;
    private String mainactivity;
    private String appName;

    @Override
    public void onReceive(Context context, Intent intent) {
        pkgName = intent.getStringExtra("pkgName");
        mainactivity = intent.getStringExtra("mainactivity");
        appName = intent.getStringExtra("appName");
    }
}
