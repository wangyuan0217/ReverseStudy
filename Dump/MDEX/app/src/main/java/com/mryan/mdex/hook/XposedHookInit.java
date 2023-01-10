package com.mryan.mdex.hook;

import com.mryan.mdex.until.CheckPackage;
import com.mryan.mdex.until.FIleUntil;
import com.mryan.mdex.until.PropertiesUtil;
import com.mryan.mdex.until.ReinforceType;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @description： XposedHookInit
 * @Author MRyan
 * @Date 2020/6/6 21:44
 * @Version 1.0
 */
public class XposedHookInit implements IXposedHookLoadPackage {
    private String dumppack;
    private String mainActivity;

    private static String ModuleActive_app = "com.mryan.mdex";
    private static String ModuleActive_packedname = "com.mryan.mdex.activity.MainActivity";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //检测自身是否激活
        if (loadPackageParam.packageName.equals(ModuleActive_app)) {
            XposedHelpers.findAndHookMethod(ModuleActive_packedname, loadPackageParam.classLoader, "isModuleActive", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    return true;
                }
            });
        }


       /* LLog.XLog("!!!!!!!!!!!!!!!!!This is MDEX, Now get ready to work", "!!!!!!!!!!!!!!!!!!!!!!!!!");*/
        try {
            //利用Properties读取app指定信息
            PropertiesUtil mProp = PropertiesUtil.getInstance().init();
            mProp.open();
            dumppack = mProp.readString("pkgName", "");
           /* LLog.XLog("!!!!!pkgName :", dumppack + "!!!!!");*/
            mainActivity = mProp.readString("mainactivity", "");
            /*LLog.XLog("!!!!!mainactivity :", mainActivity + "");*/
            String appName = mProp.readString("appName", "");
        } catch (Exception e) {
          /*  LLog.XLog("!!!!!PropertiesUtil is ERROR :", e + "!!!!!");*/
        }
        try {
            //如果不是固定加固类型 则不dump 否则才dump
            ReinforceType type = CheckPackage.ischeck(loadPackageParam);
          /*  LLog.XLog("!!!!!type :", type + "!!!!!");*/
            if (type != null) {
                final String packageName = loadPackageParam.packageName;
               /* LLog.XLog("!!!!!packageName :", packageName + "!!!!!");*/
                //待修改 可选定dump的包名
                String path = "/data/data/" + packageName + "/dump";
                File file = new File(path);
                //创建目录
                FIleUntil.mkdirs(file);
               /* LLog.XLog("!!!!!file:", file.getAbsolutePath() + "!!!!!");*/
                //脱壳
                //判断当前调用的类在不在目标包里 当application或acticity为当前app运行的类是才dump
                if (!loadPackageParam.packageName.startsWith(packageName)) {
                    return;
                }
                Dump.init(loadPackageParam, type, mainActivity);
            }
        } catch (Exception e) {
           /* LLog.XLog("!!!!!DUMP is ERROR :", "!!!!!");*/
        }

    }


}
