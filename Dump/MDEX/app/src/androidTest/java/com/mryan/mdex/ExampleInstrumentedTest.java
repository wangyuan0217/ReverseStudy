package com.mryan.mdex;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mryan.mdex.dao.XDataDao;
import com.mryan.mdex.until.LLog;
import com.mryan.mdex.until.PropertiesUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private XDataDao xDataDao;
    private Context appContext;

    @Before
    public void prepare() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        xDataDao = new XDataDao(appContext);
    }

    @Test
    public void useAppContext() {
       /* List<String> list=new ArrayList<>();
        xDataDao.clearAll();
        xDataDao.addXData("123", "asd", "asd");
        ContentResolver contentResolver = appContext.getContentResolver();
        Uri uri = Uri.parse("content://com.mryan.mdex");
        contentResolver.delete(uri, null, null);*/
        //利用Properties读取app指定信息
        PropertiesUtil mProp = PropertiesUtil.getInstance().init();
        mProp.clear();

/*
        PropertiesUtil mProp = PropertiesUtil.getInstance().init();
        mProp.clear();*/
      /*  PropertiesUtil mProp = PropertiesUtil.getInstance(appContext).init();
        mProp.open();
        String name = mProp.readString("name", "");
        int age = mProp.readInt("age", 0);
        boolean married = mProp.readBoolean("married", false);
        double weight = mProp.readDouble("weight", 0f);*/

    }
}