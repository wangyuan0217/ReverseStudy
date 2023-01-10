package com.mryan.mdex.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.mryan.mdex.until.LLog;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

public class MyService extends Service {

    private boolean isRunning = false;
    private List<String> xdataList;
    private MyReceiver myReceiver;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Bind();
    }

    public class Bind extends Binder {

    }

    public MyService getService() {
        return MyService.this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (isRunning) {
                    xdataList = getXdataList(MyService.this);
                    String pkgName = xdataList.get(1);
                    String appName = xdataList.get(3);
                    String mainactivity = xdataList.get(2);
                    //注册广播 发送广播
                    Intent intent = new Intent(MyReceiver.action);
                    Bundle bundle = new Bundle();
                    bundle.putString("pkgName", pkgName);
                    bundle.putString("appName", appName);
                    bundle.putString("mainactivity", mainactivity);
                    if (myReceiver == null) {
                        myReceiver = new MyReceiver();
                        registerReceiver(myReceiver, new IntentFilter(MyReceiver.action));
                    }
                    intent.putExtras(bundle);
                    sendBroadcast(intent);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 接口回调
     */
    public interface CallBack {
        void onGetList(List<String> list);
    }

    public CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public CallBack getCallBack() {
        return callBack;
    }

    /**
     * ContentResolver共享sqlite数据
     *
     * @param context
     * @return
     */
    private List<String> getXdataList(Context context) {
        //获取sqlite数据
        List<String> list = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://com.mryan.mdex");
        Cursor cursor = resolver.query(uri, null, null, null, null);
        String[] columnNames = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            for (String columnName : columnNames) {
                String value = cursor.getString(cursor.getColumnIndex(columnName));
                list.add(value);
            }
        }
        cursor.close();
        return list;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        unregisterReceiver(myReceiver);
    }
}
