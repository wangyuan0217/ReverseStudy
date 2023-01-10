package com.mryan.mdex.until;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * @description：TODO
 * @Author MRyan
 * @Date 2020/6/7 14:03
 * @Version 1.0
 */
public class AppInfo {
    private String appName;//应用程序名称
    private Drawable appIcon;  //应用程序图像
    private Intent intent;     //启动应用程序的Intent ，一般是Action为Main和Category为Lancher的Activity
    private String pkgName;    //应用程序所对应的包名

    public AppInfo() {
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getAppName() {
        return appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getPkgName() {
        return pkgName;
    }
}
