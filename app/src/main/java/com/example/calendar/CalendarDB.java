package com.example.calendar;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CalendarDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "calendar.db";
    private static final int DATABASE_VERSION = 1;
    private static CalendarDB instance;

    public static synchronized CalendarDB getInstance(Context context) {
        if(instance == null){
            instance = new CalendarDB(context.getApplicationContext());
        }
        return instance;
    }

    public CalendarDB(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 테이블 생성
        String CREATE_TABLE = "CREATE TABLE calendar ( " +
                "date TEXT PRIMARY KEY, " + // 날짜 (YYYY-MM-DD)
                "note TEXT, " + // 근무형태 및 노트 (근무 형태 사용자 추가 가능하도록)
                "holiday BOOLEAN)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스 스키마가 변경될 때 테이블을 다시 생성
        db.execSQL("DROP TABLE IF EXISTS calendar");
        onCreate(db);
    }

    public void addNote(String date, String note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("note", note);
        values.put("holiday", Boolean.FALSE);

        // date가 중복되면 REPLACE (즉, update)
        db.insertWithOnConflict("calendar", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public String loadNote(String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("calendar", new String[] { "note" }, "date = ?",
                new String[] { date }, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String note = cursor.getString(0);
            cursor.close();
            db.close();
            return note;
        }
        db.close();
        return null;
    }

    public void deleteNote(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("calendar", "date = ?", new String[] { date });
        db.close();
    }

    public void logAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + "calendar", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String note = cursor.getString(cursor.getColumnIndex("note"));
                @SuppressLint("Range") String holiday = cursor.getString(cursor.getColumnIndex("holiday"));
                Log.d("CalendarDB", "★ " + ", Date: " + date + ", Note: " + note + ", Holiday: " + holiday);
            }
            cursor.close(); // 커서 닫기
        }
        db.close(); // 데이터베이스 닫기
    }
}