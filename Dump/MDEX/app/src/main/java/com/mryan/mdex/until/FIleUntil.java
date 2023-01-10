package com.mryan.mdex.until;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @description： 文件操作类
 * @Author MRyan
 * @Date 2020/6/6 22:36
 * @Version 1.0
 */
public class FIleUntil {
    /**
     * 创建文件夹
     *
     * @param file
     */
    public static void mkdirs(File file) {
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }

    /**
     * 写数据
     *
     * @param dexdata
     * @param path
     */
    public static void writeByteFile(byte[] dexdata, String path) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(dexdata);
            fileOutputStream.close();
            /*   LLog.XLog("!!!Success:", "");*/
        } catch (Exception e) {
            /*      LLog.XLog("!!!ERROR:", e + "");*/
        }
    }

    /**
     * 读Internal中文件的方法
     *
     * @param filePathName 文件路径及文件名
     * @return 读出的字符串
     * @throws IOException
     */
    public static String readInternal(String filePathName) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();

        // 打开文件输入流
        FileInputStream fileInputStream = new FileInputStream(filePathName);

        byte[] buffer = new byte[1024];
        int len = fileInputStream.read(buffer);
        // 读取文件内容
        while (len > 0) {
            stringBuffer.append(new String(buffer, 0, len));
            // 继续将数据放到buffer中
            len = fileInputStream.read(buffer);
        }
        // 关闭输入流
        fileInputStream.close();
        return stringBuffer.toString();
    }

    /**
     * 写Internal中文件的方法
     *
     * @param filePathName 文件路径及文件名
     * @param content      要写进文件中的内容
     * @throws IOException
     */
    public static void writeInternal(String filePathName, String content) throws IOException {

        // 打开文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(filePathName);

        // 写数据到文件中
        fileOutputStream.write(content.getBytes());

        // 关闭输出流
        fileOutputStream.close();
    }

    /**
     * 从External文件目录下读取文件
     *
     * @param filePathName 要读取的文件的路径+文件名
     * @return
     * @throws IOException
     */
    public static String readExternal(String filePathName) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        // 打开文件输入流
        FileInputStream fileInputStream = new FileInputStream(filePathName);
        byte[] buffer = new byte[1024];
        int len = fileInputStream.read(buffer);
        // 读取文件内容
        while (len > 0) {
            stringBuffer.append(new String(buffer, 0, len));

            // 继续把数据存放在buffer中
            len = fileInputStream.read(buffer);
        }
        // 关闭输入流
        fileInputStream.close();

        return stringBuffer.toString();
    }

    /**
     * 向External文件目录下写入文件
     *
     * @param filePathName 要写入的的文件的路径+文件名
     * @param content      要写入的内容
     * @throws IOException
     */
    public static void writeExternal(String filePathName, String content) throws IOException {

        // 获取External的可用状态
        String storageState = Environment.getExternalStorageState();

        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            // 当External的可用状态为可用时

            // 打开输出流
            FileOutputStream fileOutputStream = new FileOutputStream(filePathName);

            // 写入文件内容
            fileOutputStream.write(content.getBytes());

            // 关闭输出流
            fileOutputStream.close();

        }
    }


    /**
     * 删除文件夹和文件夹里面的文件
     *
     * @param pPath
     */
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

}
