package com.example.youngwind.mdp_android;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.lingala.zip4j.core.ZipFile;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        saveFile("youngwind.txt", "awesome guy!\n");
//        unzip("test.zip", "youngwind");
    }

    /**
     * 保存内容到文件
     *
     * @param fileName
     * @param fileContent
     */
    public void saveFile(String fileName, String fileContent) {
        FileOutputStream out = null;
        BufferedWriter writer = null;

        try {
            out = openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 解压文件
     * @param zFileName
     * @param destDirName
     */
    public void unzip(String zFileName, String destDirName) {
        // 暂时不知道怎么用程序来获得这个路径
        // 很多方法比如 getAbsolutePath这些拿到的都是/data/user/0/com.example.youngwind.mdp_android/files
        // 这是个软连接,不能通过这个路径读写文件
        String path = "/data/data/com.example.youngwind.mdp_android/files/";

        // 坑!在java里面,File对象居然只是文件的路径,而不是文件本身.....哭瞎!
        File dir = new File(path);
        File zipFile = new File(path + zFileName);
        String dest = path + destDirName;
        ZipFile zFile = null;
        try {
            zFile = new ZipFile(zipFile);
        } catch (net.lingala.zip4j.exception.ZipException e) {
            e.printStackTrace();
        }
        File destDir = new File(dest);
        if (destDir.isDirectory() && !destDir.exists()) {
            destDir.mkdir();
        }
        try {
            zFile.extractAll(dest);
        } catch (net.lingala.zip4j.exception.ZipException e) {
            e.printStackTrace();
        }
    }

}
