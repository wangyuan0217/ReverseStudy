package com.mryan.mdex.until;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @description： 检测是否为加固类型
 * @Author MRyan
 * @Date 2020/6/6 22:09
 * @Version 1.0
 */
public class CheckPackage {

    private static List<String> mPackageName = new ArrayList<>();
    private static Map<String, ReinforceType> mMap = new HashMap<>();


    private static final String[] QIHOO = {"com.stub.StubApp"};
    private static final String[] AIJIAMI = {"s.h.e.l.l.S"};
    private static final String[] BANGBANG = {"com.secneo.apkwrapper.ApplicationWrapper"};
    private static final String[] TENCENT = {"com.tencent.StubShell.TxAppEntry"};
    private static final String[] BAIDU = {"com.baidu.protect.StubApplication"};


    public static ReinforceType ischeck(final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            for (String pack : mPackageName) {
                Class clazz = XposedHelpers.findClass(pack, lpparam.classLoader);
                if (clazz != null) {
                   /* LLog.XLog("!!!!!findclass:", pack);
                    LLog.XLog("!!!!!find packerType :", mMap.get(pack).getType());*/
                    return mMap.get(pack);
                }
            }
        } catch (Exception e) {
         /*   LLog.XLog("!!!!!ClassNotFoundException :", "");*/
        }
       /* LLog.XLog("!!!!!NotfindClass", "");*/
        return null;
    }


    static {
        mPackageName.addAll(Arrays.asList(QIHOO));
        mPackageName.addAll(Arrays.asList(AIJIAMI));
        mPackageName.addAll(Arrays.asList(BANGBANG));
        mPackageName.addAll(Arrays.asList(TENCENT));
        mPackageName.addAll(Arrays.asList(BAIDU));

        for (String s : QIHOO) {
            mMap.put(s, ReinforceType.QIHOO);
        }
        for (String s : AIJIAMI) {
            mMap.put(s, ReinforceType.AIJIAMI);
        }
        for (String s : BANGBANG) {
            mMap.put(s, ReinforceType.BANGBANG);
        }
        for (String s : TENCENT) {
            mMap.put(s, ReinforceType.TENCENT);
        }
        for (String s : BAIDU) {
            mMap.put(s, ReinforceType.BAIDU);
        }
    }


}
