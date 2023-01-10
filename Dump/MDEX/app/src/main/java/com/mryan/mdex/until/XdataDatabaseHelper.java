package com.mryan.mdex.until;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * @description： sql数据库
 * @Author MRyan
 * @Date 2020/6/8 10:14
 * @Version 1.0
 */
public class XdataDatabaseHelper extends SQLiteOpenHelper {

    public static final String LOG = "XdataDatabaseHelper";
    public static final String TABLE_name = "hookmydb";
    public static final String ID = "id";
    public static final String TABLE_pkgName = "pkgName";
    public static final String TABLE_mainactivity = "mainactivity";
    public static final String TABLE_appName = "appName";

    private static XdataDatabaseHelper intanse;


    public static synchronized XdataDatabaseHelper getInstance(Context context) {
        if (intanse == null) {
            intanse = new XdataDatabaseHelper(context);
        }
        return intanse;
    }


    public XdataDatabaseHelper(Context context) {
        super(context, "XdataDatabaseHelper.db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE " + TABLE_name + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                TABLE_pkgName + " varchar(50) ," +
                TABLE_mainactivity + " varchar(50) ," +
                TABLE_appName + " varchar(50) " +
                ")");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}