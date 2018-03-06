package com.efrobot.programme.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.programme.bean.ExecuteModule;
import com.efrobot.programme.bean.MainProject;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by zd on 2018/3/2.
 */

public class DbHelper extends OrmLiteSqliteOpenHelper {

    public final String TAG = this.getClass().getSimpleName();
    private Context context;
    private static int version = 1;
    private static String DB_NAME = "GUESTS";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, version);
        this.context = context;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, MainProject.class);
            TableUtils.createTable(connectionSource, ExecuteModule.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }
}
