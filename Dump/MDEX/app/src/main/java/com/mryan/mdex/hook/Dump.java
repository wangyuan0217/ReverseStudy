package com.mryan.mdex.hook;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.mryan.mdex.until.FIleUntil;
import com.mryan.mdex.until.ReinforceType;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @description： dump
 * @Author MRyan
 * @Date 2020/6/6 21:53
 * @Version 1.0
 */
public class Dump {

    private static Context context = null;

    /**
     * 执行脱壳入口
     *
     * @param loadPackageParam
     * @param type             加固类型
     * @param mainActivity     App启动类
     */
    public static void init(final XC_LoadPackage.LoadPackageParam loadPackageParam,
                            ReinforceType type,
                            final String mainActivity) {
      /*  LLog.XLog("!!!!!DUMP", loadPackageParam.packageName + "!!!!!");
        LLog.XLog("!!!!!DUMP", "!!!!!");
        LLog.XLog("!!!!!DUMP", "Instrumentation#newApplication" + "!!!!!");*/
        //Instrumentation 创建Application 当程序运行时脱壳
        XposedHelpers.findAndHookMethod("android.app.Instrumentation",
                loadPackageParam.classLoader,
                "newApplication",
                ClassLoader.class, String.class,
                Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        /*  LLog.XLog("!!!Application!!!", param.getResult() + "!!!!!");*/
                        @SuppressLint("SdCardPath") String path = "/data/data/" + loadPackageParam.packageName + "/dump";
                        //dump
                        dumpall(loadPackageParam, param);
                        hookToast("脱壳成功!!已复制到剪贴板!!请去指定路径获取！!路径：" + path, loadPackageParam, mainActivity, path);
                        /* LLog.XLog("!!!!!DUMP Success", "!!!!!");*/

                    }
                });
    }


    /**
     * dumpall
     *
     * @param loadPackageParam
     * @param param
     */
    private static void dumpall(XC_LoadPackage.LoadPackageParam loadPackageParam,
                                XC_MethodHook.MethodHookParam param) {
        //完整脱壳
        dump(loadPackageParam.packageName, param.getResult().getClass());
        dumpComplete(loadPackageParam, ((Application) param.getResult()));
    }


    /**
     * 根据ClassLoader.loadClass加载机制 再次脱壳
     *
     * @param loadPackageParam
     * @param application
     */
    private static void dumpComplete(final XC_LoadPackage.LoadPackageParam loadPackageParam,
                                     final Application application) {
        ClassLoader classLoader = application.getClassLoader();
        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                /*    LLog.XLog("!!!!loadPackageParam_loadClass:", param.args[0] + "");*/
                Class<?> result = (Class<?>) param.getResult();
                if (result != null) {
                    dump(loadPackageParam.packageName, result);
                }
            }
        });

        XposedHelpers.findAndHookMethod("java.lang.ClassLoader", classLoader, "loadClass", String.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                /*  LLog.XLog("!!!!java.lang.ClassLoader：", param.args[0] + "");*/
                Class<?> result = (Class<?>) param.getResult();
                if (result != null) {
                    dump(loadPackageParam.packageName, result);
                }
            }
        });

    }

    /**
     * 执行脱壳 写出文件 路径为 /data/data/" + packageName + "/dump
     *
     * @param packageName
     * @param getclass
     */
    private static void dump(String packageName, Class<?> getclass) {
        Object dexCache = XposedHelpers.getObjectField(getclass, "dexCache");
        /* LLog.XLog("!!!decCache:", dexCache + "");*/
        Object o = XposedHelpers.callMethod(dexCache, "getDex");
        byte[] datadex = (byte[]) XposedHelpers.callMethod(o, "getBytes");
        //修复dex
        byte[] restoredata = restoreDex(datadex);
        @SuppressLint("SdCardPath") String path = "/data/data/" + packageName + "/dump";
        File file = new File(path, "Dump_" + restoredata.length + ".dex");
        if (file.exists()) {
            /*  LLog.XLog("!!!!!exists", file.getName() + "!!!!!");*/
            return;
        }
        FIleUntil.writeByteFile(restoredata, file.getAbsolutePath());
    }

    /**
     * 修复DexHeader magic
     *
     * @param datadex
     * @return
     */
    public static byte[] restoreDex(byte[] datadex) {
        byte[] restoredata = datadex;
        restoredata[0] = (byte) 0x64;
        restoredata[1] = (byte) 0x65;
        restoredata[2] = (byte) 0x78;
        restoredata[3] = (byte) 0x0A;
        restoredata[4] = (byte) 0x30;
        restoredata[5] = (byte) 0x33;
        restoredata[6] = (byte) 0x35;
        restoredata[7] = (byte) 0x00;
        return restoredata;
    }


    /**
     * 脱壳成功 交互
     */
    private static void hookToast(final String text, XC_LoadPackage.LoadPackageParam loadPackageParam, String mainActivity, final String path) {
        //获取context类
        XposedHelpers.findAndHookMethod("android.content.ContextWrapper",
                loadPackageParam.classLoader,
                "getApplicationContext",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        context = (Context) param.getResult();
                        /*  XposedBridge.log("context:" + context.getPackageCodePath());*/
                    }
                });
        XposedHelpers.findAndHookMethod(mainActivity,
                loadPackageParam.classLoader,
                "onCreate",
                Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                        /*  LLog.XLog("!!!!!" + text, "!!!!!");*/
                    }
                });
    }


}