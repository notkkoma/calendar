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
                "type TEXT," + // 근무형태
                "note TEXT, " + // 메모
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

        // 저장된 근무형태 불러오기
        String type = null;  // 근무형태 초기화

        // 쿼리 실행
        Cursor cursor = db.query("calendar", new String[] { "type" }, "date = ?",
                new String[] { date }, null, null, null);

        // 커서가 null이 아니고, 첫 번째 요소로 이동 가능한 경우
        if (cursor != null && cursor.moveToFirst()) {
            type = cursor.getString(0);  // 근무형태 가져오기
        }

        // 리소스 정리
        if (cursor != null) {
            cursor.close();  // 커서 닫기
        }

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("type", type);
        values.put("note", note);
        values.put("holiday", Boolean.FALSE);

        // date가 중복되면 REPLACE (즉, update)
        db.insertWithOnConflict("calendar", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public void addType(String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 저장된 노트 불러오기
        String note = null;  // 근무형태 초기화

        // 쿼리 실행
        Cursor cursor = db.query("calendar", new String[] { "note" }, "date = ?",
                new String[] { date }, null, null, null);

        // 커서가 null이 아니고, 첫 번째 요소로 이동 가능한 경우
        if (cursor != null && cursor.moveToFirst()) {
            note = cursor.getString(0);  // 근무형태 가져오기
        }

        // 리소스 정리
        if (cursor != null) {
            cursor.close();  // 커서 닫기
        }

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("type", type);
        values.put("note", note);
        values.put("holiday", Boolean.FALSE);

        // date가 중복되면 REPLACE (즉, update)
        db.insertWithOnConflict("calendar", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public void delete(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("calendar", "date = ?", new String[] { date });
        db.close();
    }

    public String loadType(String date) {
        // date가 null인 경우 처리
        if (date == null) {
            return null;  // 또는 적절한 에러 메시지 또는 기본값 반환
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String type = null;  // 근무형태 초기화

        // 쿼리 실행
        Cursor cursor = db.query("calendar", new String[] { "type" }, "date = ?",
                new String[] { date }, null, null, null);

        // 커서가 null이 아니고, 첫 번째 요소로 이동 가능한 경우
        if (cursor != null && cursor.moveToFirst()) {
            type = cursor.getString(0);  // 근무형태 가져오기
        }

        // 리소스 정리
        if (cursor != null) {
            cursor.close();  // 커서 닫기
        }
        db.close();  // 데이터베이스 닫기

        return type;  // 메모 반환
    }

    public String loadNote(String date) {
        // date가 null인 경우 처리
        if (date == null) {
            return null;  // 또는 적절한 에러 메시지 또는 기본값 반환
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String note = null;  // 메모 초기화

        // 쿼리 실행
        Cursor cursor = db.query("calendar", new String[] { "note" }, "date = ?",
                new String[] { date }, null, null, null);

        // 커서가 null이 아니고, 첫 번째 요소로 이동 가능한 경우
        if (cursor != null && cursor.moveToFirst()) {
            note = cursor.getString(0);  // 메모 가져오기
        }

        // 리소스 정리
        if (cursor != null) {
            cursor.close();  // 커서 닫기
        }
        db.close();  // 데이터베이스 닫기

        return note;  // 메모 반환
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