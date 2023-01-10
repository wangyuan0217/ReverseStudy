package com.mryan.mdex.dao;

import java.util.List;

/**
 * @description：IXDataDao
 * @Author MRyan
 * @Date 2020/6/8 10:19
 * @Version 1.0
 */
public interface IXDataDao {
    void addXData(String pkgName, String appName, String mainactivity);

    void clearAll();

    List<String> queryXData();
}
