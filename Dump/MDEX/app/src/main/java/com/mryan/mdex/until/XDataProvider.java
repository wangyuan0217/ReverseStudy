package com.mryan.mdex.until;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @description：TODO
 * @Author MRyan
 * @Date 2020/6/8 10:37
 * @Version 1.0
 */
public class XDataProvider extends ContentProvider {

    //定义一个uri的匹配器，UriMatcher.NO_MATCH，不匹配时的返回值
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int USER_ALL = 200;

    private XdataDatabaseHelper xdataDatabaseHelper;

    static {
        //com.sunofbeaches.provider,跟我们注册的地方一致即可，这样子才可以找到这里来嘛
        //path：路径，一般来说指的是表名，这里我们只有一张表，可以null，后面我们去学习查询联系人的时候，就有多张表了。
        //code:返回码，规则匹配上的结果码
        sUriMatcher.addURI("com.mryan.mdex", null, USER_ALL);
    }

    @Override
    public boolean onCreate() {
        xdataDatabaseHelper = XdataDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //匹配规则
        int result = sUriMatcher.match(uri);
        if (result == USER_ALL) {
            //匹配成功
            SQLiteDatabase db = xdataDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query(XdataDatabaseHelper.TABLE_name, projection, selection, selectionArgs, null, null, sortOrder);
            return cursor;
        } else {
            throw new IllegalArgumentException("参数非法.");
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int deleteInt = 0;
        //匹配规则
        int result = sUriMatcher.match(uri);
        if (result == USER_ALL) {
            //匹配成功
            SQLiteDatabase db = xdataDatabaseHelper.getReadableDatabase();
            deleteInt = db.delete("hookmydb", null, null);

        } else {
            throw new IllegalArgumentException("参数非法.");
        }
        return deleteInt;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
