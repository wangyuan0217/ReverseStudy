package com.mryan.mdex.until;

import android.content.ContentResolver;
import android.content.Context;

/**
 * @description： ContentProviderUtils
 * @Author MRyan
 * @Date 2020/6/8 13:10
 * @Version 1.0
 */
public class ContentProviderUtils {
    private static ContentProviderUtils instance;
    private Context context;

    public ContentProviderUtils(Context paramContext) {
        this.context = paramContext;
    }

    public static ContentProviderUtils getInstance(Context paramContext) {
        if (null == instance) {
            synchronized (ContentProviderUtils.class) {
                if (null == instance) {
                    instance = new ContentProviderUtils(paramContext);
                }
            }
        }
        return instance;
    }

    public  ContentResolver getResolver(Context paramContext) {
        ContentResolver contentResolver = paramContext.getContentResolver();
        return contentResolver;
    }


}
