package com.brioal.wordquerylib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * email:brioal@foxmail.com
 * github:https://github.com/Brioal
 * Created by brioa on 2018/3/7.
 */

public class QueryDBHelper extends SQLiteOpenHelper {
    private static QueryDBHelper sHelper;

    public static SQLiteDatabase getDB(Context context) {
        if (sHelper == null) {
            sHelper = new QueryDBHelper(context, "query.db3", null, 1);
        }
        return sHelper.getWritableDatabase();
    }

    // 创建单词记录表 单词 翻译字 , 音节 , 短语 , 近义词 , 词根 , 例句 , 时间 , 查询次数 , 是否是中文
    private String SQL_WORD = "create table word ( key text UNIQUE , tran text , pron text , phrase text , similar text , root text , sentence text , time long , count int , cn int);";
    public QueryDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_WORD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
