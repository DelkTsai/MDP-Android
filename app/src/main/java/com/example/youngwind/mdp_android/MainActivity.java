package com.example.youngwind.mdp_android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.youngwind.mdp_android.model.ComponentVersionList;
import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.lingala.zip4j.core.ZipFile;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private String appFilesPath = "/data/data/com.example.youngwind.mdp_android/files/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getComponentVersions("http://172.20.200.11:9999/release/all/newest");
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
     *
     * @param zFileName   压缩文件名称,如:test.zip
     * @param destDirName 解压目录: 如: test
     */
    public void unzip(String zFileName, String destDirName) {
        // 暂时不知道怎么用程序来获得这个路径
        // 很多方法比如 getAbsolutePath这些拿到的都是/data/user/0/com.example.youngwind.mdp_android/files
        // 这是个软连接,不能通过这个路径读写文件
        String path = appFilesPath;

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


    /**
     * 获取各个组件包的最新版本信息
     *
     * @param url 获取各个组件包的版本信息接口地址
     */
    private void getComponentVersions(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);

                Gson gson = new Gson();
                ComponentVersionList componentVersionList = gson.fromJson(response, ComponentVersionList.class);
                createOrUpdateComponent(componentVersionList.componentVersions);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                Log.d("fail", new String(responseBody));
            }
        });
    }

    /**
     * 查询数据库,对比组件包的版本是否有更新
     *
     * @param componentVersions 组件包实例
     */
    public void createOrUpdateComponent(ComponentVersionList.componentVersion[] componentVersions) {
        dbHelper = new MyDatabaseHelper(this, "Component.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = null;
        String version;
        int component_id;
        for (int index = 0; index < componentVersions.length; index++) {
            component_id = componentVersions[index].component_id;
            cursor = db.query("Component", null, "component_id = ?", new String[]{String.valueOf(component_id)}, null, null, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                version = cursor.getString(cursor.getColumnIndex("component_version"));
                if (!version.equals(componentVersions[index].component_version)) {
                    Log.d("version", version);
                    Log.d("version", componentVersions[index].component_version);
                    getComponentByVersion(componentVersions[index], "UPDATE");
                }

            } else {

                getComponentByVersion(componentVersions[index], "INSERT");
            }
        }

        cursor.close();

    }

    /**
     * 从cdn上获取组件压缩包
     * 解压压缩包替换原先的文件夹
     * 更新数据库
     *
     * @param component 组件包实例
     * @param type      动作类型.  区分数据库有没有初始化  INSERT -> 第一次请求组件包, 插入数据  UPDATE -> 以前请求过组件包,更新数据
     */
    public void getComponentByVersion(ComponentVersionList.componentVersion component, final String type) {
        String cdn = "http://oag5n2hvg.bkt.clouddn.com/";
        final int component_id = component.component_id;
        final String componentName = component.name;
        final String componentVersion = component.component_version;
        final String componentUpdatedAt = component.updatedAt;
        final String fileName = componentName + "-" + componentVersion + ".zip";
        Ion.with(this).load(cdn + fileName).write(new File(appFilesPath + fileName)).setCallback(new FutureCallback<File>() {
            @Override
            public void onCompleted(Exception e, File result) {
                Log.d("test", "下载成功");
                Log.d("test", fileName);

                File dest = new File(appFilesPath + componentName);
                if (dest.isDirectory() && dest.exists()) {
                    dest.delete();
                }
                unzip(fileName, componentName);


                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();

                values.put("name", componentName);
                values.put("component_version", componentVersion);
                values.put("updatedAt", componentUpdatedAt);

                if (type.equals("INSERT")) {
                    values.put("component_id", component_id);
                    db.insert("Component", null, values);
                } else if (type.equals("UPDATE")) {
                    db.update("Component", values, "component_id = ?", new String[]{String.valueOf(component_id)});
                }


            }
        });
    }

}
