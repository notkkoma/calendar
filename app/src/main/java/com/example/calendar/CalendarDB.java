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
import java.util.concurrent.atomic.AtomicInteger;

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

    public void add(String date, String note, String type, boolean isHoliday) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("type", type);
        values.put("note", note);
        values.put("holiday", isHoliday);  // 공휴일 상태 저장

        db.insertWithOnConflict("calendar", null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
    }

    public void isHoliday(String date, HolidayCallback callback) {
        // 날짜가 주말인지 확인하는 로직 추가
        boolean isWeekend = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = sdf.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                isWeekend = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (isWeekend) {
            callback.onHolidayChecked(true);
            return;
        }

        // 기존 공휴일 API 호출 로직 유지
        String solYear = date.substring(0, 4);
        String solMonth = date.substring(5, 7);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        HolidayAPI holidayApi = retrofit.create(HolidayAPI.class);

        Call<HolidayResponse> call = holidayApi.getHolidays(
                "service_key",
                solYear, solMonth);

        call.enqueue(new Callback<HolidayResponse>() {
            @Override
            public void onResponse(Call<HolidayResponse> call, Response<HolidayResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HolidayResponse holidayResponse = response.body();
                    List<HolidayResponse.Item> holidays = holidayResponse.getBody().getItems().getItem();

                    boolean isHoliday = false;
                    for (HolidayResponse.Item holiday : holidays) {
                        if ("Y".equals(holiday.getIsHoliday())) {
                            String locdate = holiday.getLocdate();
                            String formattedDate = locdate.substring(0, 4) + "-" + locdate.substring(4, 6) + "-" + locdate.substring(6);
                            if (formattedDate.equals(date)) {
                                isHoliday = true;
                                break;
                            }
                        }
                    }

                    callback.onHolidayChecked(isHoliday);
                } else {
                    Log.e("API_ERROR", "Response unsuccessful: " + response.message());
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<HolidayResponse> call, Throwable t) {
                Log.e("API_ERROR", "API 호출 실패: " + t.getMessage());
                callback.onFailure();
            }
        });
    }

    public interface HolidayCallback {
        void onHolidayChecked(boolean isHoliday);
        void onFailure();
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

    public Boolean loadHoliday(String date) {
        // date가 null인 경우 처리
        if (date == null) {
            return null;  // 또는 적절한 에러 메시지 또는 기본값 반환
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Boolean isHoliday = null;  // 공휴일 여부 초기화

        // 쿼리 실행 (holiday 컬럼에서 값을 가져옴)
        Cursor cursor = db.query("calendar", new String[]{"holiday"}, "date = ?",
                new String[]{date}, null, null, null);

        // 커서가 null이 아니고, 첫 번째 요소로 이동 가능한 경우
        if (cursor != null && cursor.moveToFirst()) {
            int holidayValue = cursor.getInt(0);  // 'holiday' 값 가져오기 (0 또는 1)
            isHoliday = (holidayValue == 1);      // '1'이면 true, '0'이면 false로 변환
        }

        // 리소스 정리
        if (cursor != null) {
            cursor.close();  // 커서 닫기
        }
        db.close();  // 데이터베이스 닫기

        return isHoliday;  // 공휴일 여부 반환
    }

    public interface RepeatCallback {
        void onRepeatCompleted();
    }

    @SuppressLint("Range")
    public void repeat(CalendarDB.RepeatCallback callback) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            String startDate = null;
            String endDate = null;

            Cursor cursorStart = db.query("calendar", new String[]{"date"}, "type IS NOT NULL", null, null, null, "date ASC", "1");
            if (cursorStart != null && cursorStart.moveToFirst()) {
                startDate = cursorStart.getString(cursorStart.getColumnIndex("date"));
            }
            cursorStart.close();

            Cursor cursorEnd = db.query("calendar", new String[]{"date"}, "type IS NOT NULL", null, null, null, "date DESC", "1");
            if (cursorEnd != null && cursorEnd.moveToFirst()) {
                endDate = cursorEnd.getString(cursorEnd.getColumnIndex("date"));
            }
            cursorEnd.close();

            if (startDate == null || endDate == null) {
                Log.d("CalendarDB", "★ L208: No schedule to repeat.");
                db.close();
                if (callback != null) callback.onRepeatCompleted(); // 콜백 호출
                return;
            }

            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            Cursor patternCursor = db.query("calendar", new String[]{"date", "type"}, "date BETWEEN ? AND ? AND type IS NOT NULL",
                    new String[]{startDate, endDate}, null, null, "date ASC");

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

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            AtomicInteger remainingCallbacks = new AtomicInteger(3 * patternDates.size());

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < patternDates.size(); j++) {
                    String newDate = sdf.format(calendar.getTime());
                    String type = patternTypes.get(j);

                    isHoliday(newDate, new HolidayCallback() {
                        @Override
                        public void onHolidayChecked(boolean isHoliday) {
                            ContentValues values = new ContentValues();
                            values.put("date", newDate);
                            values.put("type", type);
                            values.put("note", "");
                            values.put("holiday", isHoliday);

                            db.insertWithOnConflict("calendar", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                            // 모든 콜백 완료 체크
                            if (remainingCallbacks.decrementAndGet() == 0) {
                                db.close(); // 모든 콜백 완료 후 DB 닫기
                                if (callback != null) callback.onRepeatCompleted(); // 콜백 호출
                            }
                        }

                        @Override
                        public void onFailure() {
                            Log.e("CalendarDB", "Failed to determine holiday status for date: " + newDate);

                            // 모든 콜백 완료 체크
                            if (remainingCallbacks.decrementAndGet() == 0) {
                                db.close(); // 모든 콜백 완료 후 DB 닫기
                                if (callback != null) callback.onRepeatCompleted(); // 콜백 호출
                            }
                        }
                    });
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            // 앞선 반복문에서 마지막에 하나 더해주는데, 이때 비동기적으로 처리돼서 type이 계산됨. 이를 방지해주기 위한 코드
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            Log.d("CalendarDB", "★ L208: Schedule repeated 3 times.");

        } catch (ParseException e) {
            e.printStackTrace();
            db.close(); // 예외 발생 시 DB 닫기
            if (callback != null) callback.onRepeatCompleted(); // 콜백 호출
        }
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
                Log.d("CalendarDB", "★ " + "Date: " + date + ", Type: " + type + ", Note: " + note + ", Holiday: " + holiday);
            }
            cursor.close(); // 커서 닫기
        }
        db.close(); // 데이터베이스 닫기
    }
}