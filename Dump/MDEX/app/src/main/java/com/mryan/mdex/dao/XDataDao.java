package com.mryan.mdex.dao;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mryan.mdex.until.XdataDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @description： XDataDao
 * @Author MRyan
 * @Date 2020/6/8 10:21
 * @Version 1.0
 */
public class XDataDao implements IXDataDao {
    private XdataDatabaseHelper xdataDatabaseHelper;

    public XDataDao(Context context) {
        xdataDatabaseHelper = XdataDatabaseHelper.getInstance(context);
    }

    @Override
    public void addXData(String pkgName, String appName, String mainactivity) {
        SQLiteDatabase writableDatabase = xdataDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(XdataDatabaseHelper.TABLE_pkgName, pkgName);
        values.put(XdataDatabaseHelper.TABLE_appName, appName);
        values.put(XdataDatabaseHelper.TABLE_mainactivity, mainactivity);
        writableDatabase.
                insert(XdataDatabaseHelper.TABLE_name, null, values);
        writableDatabase.close();
    }

    @Override
    public void clearAll() {
        SQLiteDatabase sqLiteDatabase = xdataDatabaseHelper.getWritableDatabase();
        sqLiteDatabase.delete(XdataDatabaseHelper.TABLE_name, null, null);
    }

    @SuppressLint("Recycle")
    @Override
    public List<String> queryXData() {
        List<String> integers = new ArrayList<>();
        SQLiteDatabase writeableDatabase = xdataDatabaseHelper.getReadableDatabase();
        Cursor c1 = writeableDatabase.rawQuery("select * from hookmydb", null);
        int number = c1.getCount();
        if (number == 0) {

        } else {
            Cursor cursor;
            SQLiteDatabase db4 = xdataDatabaseHelper.getReadableDatabase();
            cursor = db4.query(XdataDatabaseHelper.TABLE_name, null, null, null, null, null, null);
            cursor.moveToNext();
            db4.close();
            integers.add(cursor.getString(0));
            integers.add(cursor.getString(1));
            integers.add(cursor.getString(2));
        }
        return integers;
    }
}
