package com.mryan.mdex.constract;

import android.content.Context;

/**
 * @description：契约类
 * @Author MRyan
 * @Date 2020/6/7 13:22
 * @Version 1.0
 */
public interface MainConstract {
    interface MainView {
        //加载所有安装的app
        void onLoadAllApp(Context Context);

        //设置状态栏沉浸
        void onTRANSLUCENT();

        //下拉刷新获取最新消息
        void onRefreshAppInFo();
    }

    interface MainPresenter {
        void loadAllApp(Context Context);

        void TRANSLUCENT();
        void RefreshAppInFo();
    }
}
