package com.example.youngwind.mdp_android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by youngwind on 16/7/23.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_COMPONENT = "create table Component (" +
            "id integer primary key autoincrement, " +
            "component_id integer, " +
            "name text, " +
            "component_version text, " +
            "updatedAt text)";

    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_COMPONENT);
        Toast.makeText(mContext, "创建组件包版本数据库成功!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
