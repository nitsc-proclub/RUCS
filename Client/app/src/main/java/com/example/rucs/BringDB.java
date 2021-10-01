package com.example.rucs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BringDB extends SQLiteOpenHelper {

    // データーベースのバージョン
    private static final int DATABASE_VERSION = 1;
    //1;試作

    // データーベース名
    private static final String DATABASE_NAME = "TestDB.db";


    private static final String TABLE_NAME_SUBJECT = "subject_table";
    private static final String TABLE_NAME_BRING = "bring_table";
    private static final String TABLE_NAME_LINK = "link_table";


    //データベース作成

    //教科のテーブル
    private static final String SQL_CREATE_SUBJECT =
            "CREATE TABLE " + "subject_table" + " (" +  //テーブル名
                    "class_id" + " TEXT not null, " + //名前　型　制約
                    "subject_id" + " TEXT not null PRIMARY KEY," +
                    "subject_name" + " TEXT not null," +
                    "subject_color" + " TEXT not null)";

    //持ち物のテーブル
    private static final String SQL_CREATE_BRING =
            "CREATE TABLE " + "bring_table" + " (" +  //テーブル名
                    "subject_id" + " TEXT not null," +
                    "bring_id" + " TEXT not null PRIMARY KEY," +
                    "subject_name" + " TEXT not null," +
                    "bring_name" + " TEXT not null)";

    //linkのテーブル
    private static final String SQL_CREATE_LINK =
            "CREATE TABLE " + "link_table" + "(" +  //テーブル名
                    "bring_id" + " TEXT not null," +
                    "link_id" + " TEXT not null PRIMARY KEY)";


    //データベース削除
    private static final String SQL_DELETE_SUBJECT = "DROP TABLE IF EXISTS " + TABLE_NAME_SUBJECT;

    private static final String SQL_DELETE_BRING = "DROP TABLE IF EXISTS " + TABLE_NAME_BRING;

    private static final String SQL_DELETE_LINK = "DROP TABLE IF EXISTS " + TABLE_NAME_LINK;


    BringDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // テーブル作成
        db.execSQL(SQL_CREATE_SUBJECT);
        db.execSQL(SQL_CREATE_BRING);
        db.execSQL(SQL_CREATE_LINK);

        Log.d("debug", "onCreate(SQLiteDatabase db)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(SQL_DELETE_SUBJECT);
        db.execSQL(SQL_DELETE_BRING);
        db.execSQL(SQL_DELETE_LINK);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
