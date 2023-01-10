package com.mryan.mdex.until;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @description：TODO
 * @Author MRyan
 * @Date 2020/6/9 0:06
 * @Version 1.0
 */
public class MyAppInfoListView extends ListView {
    public MyAppInfoListView(Context context) {
        super(context);
    }

    public MyAppInfoListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyAppInfoListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyAppInfoListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
