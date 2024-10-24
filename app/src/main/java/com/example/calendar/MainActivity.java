package com.example.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private ActivityResultLauncher<Intent> noteActivityLauncher;
    private RecyclerView calendarRecyclerView;
    private ArrayList<String> daysOfMonth;
    private Calendar calendar;
    Button prevButton;
    Button nextButton;
    TextView monthText;
    Button deleteAllButton;
    Button repeatButton;
    private CalendarDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prevButton = findViewById(R.id.prevButton);
        monthText = findViewById(R.id.current_month);
        nextButton = findViewById(R.id.nextButton);
        deleteAllButton = findViewById(R.id.buttonDeleteAll);
        repeatButton = findViewById(R.id.buttonRepeat);

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);

        calendar = Calendar.getInstance();

        db = new CalendarDB(this);

        noteActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // NoteActivity에서 선택한 날짜 가져오기
                        String selectedDate = result.getData().getStringExtra("selectedDate");
                        //Log.d("MainActivity", "★ Selected date set L53: " + selectedDate);
                        if (selectedDate != null) {
                            // selectedDate를 Calendar 객체로 파싱
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Date date = sdf.parse(selectedDate);
                                //Log.d("MainActivity", "★ Selected date set L59: " + selectedDate);
                                if (date != null) {
                                    calendar.setTime(date);  // Calendar에 설정
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        updateCalendar();  // UI 업데이트
                    }
                });

        prevButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            //Log.d("MainActivity", "★ Selected date set L71: " + calendar.getTime().toString());
            updateCalendar();
        });

        nextButton.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            //Log.d("MainActivity", "★ Selected date set L77: " + calendar.getTime().toString());
            updateCalendar();
        });

        // 전체 DB 내용 삭제
        deleteAllButton.setOnClickListener(v -> {
            db.deleteAll(); // DB에서 모든 내용 삭제
            updateCalendar(); // UI 업데이트
        });

        // 현재 근무 패턴 3회 반복
        repeatButton.setOnClickListener(v -> {
            db.repeatSchedule(); // 반복 함수 호출
            updateCalendar(); // UI 업데이트
        });


        //Log.d("MainActivity", "★ Selected date set L81: " + calendar.getTime().toString());
        updateCalendar();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        String day = null;
        if (dayText.length() > 0) {
            int dayInt = Integer.parseInt(dayText);
            day = String.valueOf(dayInt);  // 첫 번째 문자 추출
        }

        // 날짜 클릭 시 NoteActivity로 이동
        String selectedDate = getSelectedDateString(day);  // 선택된 날짜를 생성
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        //Log.d("MainActivity", "★ Selected date set L97: " + selectedDate);
        intent.putExtra("selectedDate", selectedDate);  // 선택된 날짜 전달

        // NoteActivity 시작
        noteActivityLauncher.launch(intent);
    }

    // prev, next Button 클릭 시 캘린더 갱신
    private void updateCalendar() {
        // TextView의 텍스트를 갱신
        String monthKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.getTime());
        monthText.setText(monthKey);

        // Calendar 객체에서 year와 month를 가져오기
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0이 1월이므로 +1

        daysOfMonth = getDaysOfMonthArray();

        CalendarAdapter calendarAdapter = new CalendarAdapter(year, month, daysOfMonth, this, this);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));  // 7열 그리드
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    // yyyy-mm-dd 형태의 결과 리턴
    private String getSelectedDateString(String dayText) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0이 1월이므로 +1
        int day = Integer.parseInt(dayText); // dayText를 int로 변환
        @SuppressLint("DefaultLocale") String tmp = String.format("%02d", day);
        return year + "-" + month + "-" + tmp;
    }

    // 해당 월의 ArrayList 계산 (비어있는 경우 빈셀)
    private ArrayList<String> getDaysOfMonthArray() {
        ArrayList<String> daysList = new ArrayList<>();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // 해당 달의 시작 요일 구하기 (예: 일요일 = 1, 월요일 = 2 ...)
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // 달의 첫날 전까지 빈 셀 추가
        for (int i = 1; i < firstDayOfWeek; i++) {
            daysList.add("");  // 빈 셀 추가
        }

        // 해당 월의 날짜 추가 (1 ~ maxDaysInMonth)
        int maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= maxDaysInMonth; day++) {
            daysList.add(String.valueOf(day));
        }

        return daysList;
    }

}