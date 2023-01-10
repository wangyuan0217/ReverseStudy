package com.mryan.mdex.until;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

/**
 * @description： 工具类
 * @Author MRyan
 * @Date 2020/6/6 21:48
 * @Version 1.0
 */
public class LLog {
    public static void XLog(String title, String msg) {
        XposedBridge.log(title + msg);
    }

    public static void TLog(String msg) {
        Log.e("com.mryan.mdex.until", msg);
    }

}
