package com.efrobot.programme.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.programme.bean.FaceAndActionEntity;
import com.efrobot.programme.db.DbHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zd on 2017/8/18.
 */
public class DataManager {

    private static DataManager instance;

    private static SQLiteDatabase db = null;

    /**
     * 动作表
     */
    private static String ACTION_TABLE = "faceandactionentity";

    public static final String ACRIONNUM = "actionNum";
    public static final String ACRIONNAME = "actionName";
    public static final String ACRIONTIME = "actionTime";

    public static Context mContext;


    public static DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager();
        }
        mContext = context;
        return instance;
    }

    /**
     * 动作和表情  1动作 2表情
     *
     * @param context
     * @param path
     */
    public void actionAndFace(Context context, String path, int type) {
        ArrayList<FaceAndActionEntity> beanList = new ArrayList<FaceAndActionEntity>();
        try {
            /**
             * 读取本地资源   <>读取assets的文件</>
             */
            InputStream in = context.getResources().getAssets().open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;


            while ((line = reader.readLine()) != null) {
                String item[] = line.split("##");
                int len = item.length;
                if (len > 2) {
                    beanList.add(new FaceAndActionEntity(item[0], item[1], item[2]));
                } else if (len > 1) {
                    beanList.add(new FaceAndActionEntity(item[0], item[1]));
                }
            }
            if (type == 1) {
                //插入动作
                deleteAction();
                insertAction(beanList);
            }
//            else if (type == 2) {
//                //插入表情
//                dao.insertFace(beanList);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除保存的动作数据
     */
    public void deleteAction() {
        if (db == null)
            db = new DbHelper(mContext).getWritableDatabase();
        db.delete(ACTION_TABLE, null, null);
        closDb(db);
    }

    /**
     * 插入动作
     *
     * @param beans 动作类
     */
    public void insertAction(ArrayList<FaceAndActionEntity> beans) {
        if (db == null)
            db = new DbHelper(mContext).getWritableDatabase();
        if (beans == null || beans.isEmpty())
            return;
        /**
         * 开启事务
         */
        db.beginTransaction();
        try {
            int len = beans.size();
            /**
             * 插入动作
             */
            for (int i = 0; i < len; i++) {
                ContentValues values = new ContentValues();
                values.put(ACRIONNUM, beans.get(i).index);
                values.put(ACRIONNAME, beans.get(i).content);
                values.put(ACRIONTIME, beans.get(i).time);
                db.insert(ACTION_TABLE, null, values);
            }

            /**
             * 设置批量插入成功
             */
            db.setTransactionSuccessful();
        } finally {
            /**
             * 结束事务
             */
            db.endTransaction();
            /**
             * 关闭数据库
             */
            closDb(db);
        }
    }

    /**
     * 查询所有的动作
     *
     * @return 返回动作数据集合
     */
    public ArrayList<FaceAndActionEntity> queryAllAction() {
        if (db == null)
            db = new DbHelper(mContext).getWritableDatabase();
        ArrayList<FaceAndActionEntity> beans = new ArrayList<FaceAndActionEntity>();
        Cursor c = null;
        try {
            c = db.query(ACTION_TABLE, null, null, null, null, null, null);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    beans.add(new FaceAndActionEntity(c));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
            closDb(db);
        }
        return beans;
    }


    private void closDb(SQLiteDatabase db) {
//        db.close();
    }

}
