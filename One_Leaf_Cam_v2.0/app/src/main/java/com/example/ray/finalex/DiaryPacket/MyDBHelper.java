package com.example.ray.finalex.DiaryPacket;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 43cm on 2016/12/13.
 */

public class MyDBHelper extends SQLiteOpenHelper {

    private final static String TABLE_NAME = "diary_table";
    private static String DATABASE_NAME = "diary_database.db";
    private static int VERSION = 1;
    private SQLiteDatabase db;

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MyDBHelper(Context context) {
        this(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        db = sqLiteDatabase;
        String CREATE_TABLE = "CREATE TABLE if not exists " + TABLE_NAME + " (_id INTEGER PRIMARY KEY, time TEXT, pic TEXT, title TEXT, detail TEXT, address TEXT)";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int il) {

    }

    public boolean checkTime(String time) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from diary_table where time = ?", new String[]{time});
        if (cursor.getCount() == 0) {
            db.close();
            return false;
        } else {
            cursor.close();
            db.close();
            return true;
        }
    }

    public void insert2DB(String time, String title, String pic, String detail, String address) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("time", time);
        cv.put("title", title);
        cv.put("pic", pic);
        cv.put("detail", detail);
        cv.put("address", address);
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    public Cursor query() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, "time desc", null);
        return c;
    }

    public Cursor querySearch(String keyword) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, "title like " + "'%"  + keyword + "%'" , null, null, null, "time desc", null);
        return c;
    }

    public void delete(String time) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "time=?", new String[]{time});
    }

    public void update(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public void close() {
        if (db != null) {
            db.close();
        }
    }

}
