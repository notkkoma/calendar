package com.example.calendar;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class CalendarDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "calendar.db";
    private static final int DATABASE_VERSION = 1;
    private static CalendarDB instance;

    public static synchronized CalendarDB getInstance(Context context) {
        if (instance == null) {
            instance = new CalendarDB(context.getApplicationContext());
        }
        return instance;
    }

    public CalendarDB(Context context) {
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

        // 날짜 포맷에서 연도와 월을 추출
        String solYear = date.substring(0, 4);
        String solMonth = date.substring(5, 7);

        // 저장된 근무형태 불러오기
        String type = null;  // 근무형태 초기화

        // 쿼리 실행
        Cursor cursor = db.query("calendar", new String[]{"type"}, "date = ?",
                new String[]{date}, null, null, null);

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

        // 공휴일 업데이트 함수 호출
        updateHolidayStatus(solYear, solMonth);

        db.close();
    }

    public void addType(String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 날짜 포맷에서 연도와 월을 추출
        String solYear = date.substring(0, 4);
        String solMonth = date.substring(5, 7);

        // 저장된 노트 불러오기
        String note = null;  // 메모 초기화

        // 쿼리 실행
        Cursor cursor = db.query("calendar", new String[]{"note"}, "date = ?",
                new String[]{date}, null, null, null);

        // 커서가 null이 아니고, 첫 번째 요소로 이동 가능한 경우
        if (cursor != null && cursor.moveToFirst()) {
            note = cursor.getString(0);  // 메모 가져오기
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

        // 공휴일 업데이트 함수 호출
        updateHolidayStatus(solYear, solMonth);

        db.close();
    }

    public void delete(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("calendar", "date = ?", new String[]{date});
        db.close();
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("calendar", null, null); // 모든 행을 삭제
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
        Cursor cursor = db.query("calendar", new String[]{"type"}, "date = ?",
                new String[]{date}, null, null, null);

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
        Cursor cursor = db.query("calendar", new String[]{"note"}, "date = ?",
                new String[]{date}, null, null, null);

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

    @SuppressLint("Range")
    public void repeat() {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            // 1. 저장된 근무형태의 첫째 날과 마지막 날을 가져오기
            String startDate = null;
            String endDate = null;

            // 첫 번째 날짜 가져오기 (오름차순 정렬 후 첫 번째)
            Cursor cursorStart = db.query("calendar", new String[]{"date"}, "type IS NOT NULL", null, null, null, "date ASC", "1");
            if (cursorStart != null && cursorStart.moveToFirst()) {
                startDate = cursorStart.getString(cursorStart.getColumnIndex("date"));
            }
            cursorStart.close();

            // 마지막 날짜 가져오기 (내림차순 정렬 후 첫 번째)
            Cursor cursorEnd = db.query("calendar", new String[]{"date"}, "type IS NOT NULL", null, null, null, "date DESC", "1");
            if (cursorEnd != null && cursorEnd.moveToFirst()) {
                endDate = cursorEnd.getString(cursorEnd.getColumnIndex("date"));
            }
            cursorEnd.close();

            // 저장된 패턴을 가져오기 위해 두 날짜가 유효한지 확인
            if (startDate == null || endDate == null) {
                Log.d("CalendarDB", "★ L208: No schedule to repeat.");
                return; // 패턴이 없으면 함수 종료
            }

            // 2. 첫째 날부터 마지막 날까지의 패턴을 반복
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // 날짜 범위 내의 패턴을 가져옴
            Cursor patternCursor = db.query("calendar", new String[]{"date", "type"}, "date BETWEEN ? AND ? AND type IS NOT NULL",
                    new String[]{startDate, endDate}, null, null, "date ASC");

            // 패턴을 리스트로 저장
            List<String> patternDates = new ArrayList<>();
            List<String> patternTypes = new ArrayList<>();

            if (patternCursor != null) {
                while (patternCursor.moveToNext()) {
                    String date = patternCursor.getString(patternCursor.getColumnIndex("date"));
                    String type = patternCursor.getString(patternCursor.getColumnIndex("type"));
                    patternDates.add(date);
                    patternTypes.add(type);
                }
                patternCursor.close();
            }

            // 3. 마지막 날짜 다음날부터 패턴을 3회 반복하여 저장
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);  // 마지막 날로 설정
            calendar.add(Calendar.DAY_OF_MONTH, 1); // 마지막 날의 다음 날로 이동

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < patternDates.size(); j++) {
                    String newDate = sdf.format(calendar.getTime()); // 새로운 날짜
                    String type = patternTypes.get(j);  // 해당 날짜의 근무형태

                    // 새로운 날짜와 패턴을 DB에 저장
                    ContentValues values = new ContentValues();
                    values.put("date", newDate);
                    values.put("type", type);
                    values.put("note", "");  // 메모는 공백으로 저장
                    values.put("holiday", Boolean.FALSE);

                    db.insertWithOnConflict("calendar", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                    calendar.add(Calendar.DAY_OF_MONTH, 1); // 하루씩 증가
                }
            }

            Log.d("CalendarDB", "★ L208: Schedule repeated 3 times.");

        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            db.close(); // 데이터베이스 닫기
        }
    }

    public void updateHolidayStatus(String solYear, String solMonth) {
        // Retrofit 인스턴스 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/")
                .addConverterFactory(SimpleXmlConverterFactory.create()) // API가 XML을 반환하므로 XML 파서 필요
                .build();

        // HolidayApi 인터페이스 구현
        HolidayAPI holidayApi = retrofit.create(HolidayAPI.class);

        // ★ 이부분 github에 넘어가지 않게 유의!
        // API 호출 (serviceKey, 연도, 월 전달)
        Call<HolidayResponse> call = holidayApi.getHolidays(
                "your_service_key",
                solYear, solMonth);

        // 비동기 API 호출
        call.enqueue(new Callback<HolidayResponse>() {
            @Override
            public void onResponse(Call<HolidayResponse> call, Response<HolidayResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API 응답 처리
                    HolidayResponse holidayResponse = response.body();
                    List<HolidayResponse.Item> holidays = holidayResponse.getBody().getItems().getItem(); // Body에서 items를 가져옵니다.

                    SQLiteDatabase db = getWritableDatabase();
                    for (HolidayResponse.Item holiday : holidays) {
                        if ("Y".equals(holiday.getIsHoliday())) {
                            String locdate = holiday.getLocdate(); // 공휴일 날짜
                            String formattedDate = locdate.substring(0, 4) + "-" + locdate.substring(4, 6) + "-" + locdate.substring(6);

                            // 해당 날짜의 holiday 필드를 true로 업데이트
                            ContentValues values = new ContentValues();
                            values.put("holiday", true);
                            db.update("calendar", values, "date = ?", new String[]{formattedDate});
                        }
                    }
                    db.close();
                } else {
                    Log.e("API_ERROR", "Response unsuccessful: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<HolidayResponse> call, Throwable t) {
                // 실패 처리
                Log.e("API_ERROR", "★ CalendarDB.java L330 API 호출 실패: " + t.getMessage());
            }
        });
    }

    public void logAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + "calendar", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex("type"));
                @SuppressLint("Range") String note = cursor.getString(cursor.getColumnIndex("note"));
                @SuppressLint("Range") String holiday = cursor.getString(cursor.getColumnIndex("holiday"));
                Log.d("CalendarDB", "★ " + ", Date: " + date + ", Type: " + type + ", Note: " + note + ", Holiday: " + holiday);
            }
            cursor.close(); // 커서 닫기
        }
        db.close(); // 데이터베이스 닫기
    }
}