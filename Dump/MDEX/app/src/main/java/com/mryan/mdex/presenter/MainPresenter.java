package com.mryan.mdex.presenter;

import android.content.Context;

import com.mryan.mdex.constract.MainConstract;

/**
 * @description：TODO
 * @Author MRyan
 * @Date 2020/6/7 13:23
 * @Version 1.0
 */
public class MainPresenter implements MainConstract.MainPresenter {
    private MainConstract.MainView mainView;

    public MainPresenter(MainConstract.MainView mainView) {
        this.mainView = mainView;
    }

    @Override
    public void loadAllApp(Context context) {
        mainView.onLoadAllApp(context);
    }

    @Override
    public void TRANSLUCENT() {
        mainView.onTRANSLUCENT();
    }

    @Override
    public void RefreshAppInFo() {
        mainView.onRefreshAppInFo();
    }
}
