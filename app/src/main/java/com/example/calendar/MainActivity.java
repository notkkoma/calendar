package com.example.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private RecyclerView calendarRecyclerView;
    private ArrayList<String> daysOfMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);

        daysOfMonth = getDaysOfMonthArray();

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysOfMonth, this, this);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));  // 7열 그리드
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    @Override
    public void onItemClick(int position, String dayText) {
        // 날짜 클릭 시 NoteActivity로 이동
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        String selectedDate = getSelectedDateString(dayText);
        intent.putExtra("selectedDate", selectedDate);
        Log.d("&&&MainActivity Line40&&&", selectedDate);
        startActivity(intent);
    }

    private String getSelectedDateString(String dayText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0이 1월이므로 +1
        return year + "-" + month + "-" + dayText;
    }

    private ArrayList<String> getDaysOfMonthArray() {
        ArrayList<String> daysList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
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