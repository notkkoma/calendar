package com.example.calendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class NoteActivity extends AppCompatActivity {

    private EditText noteEditText;
    private ToggleButton dayButton;
    private ToggleButton nightButton;
    private ToggleButton morningButton;
    private ToggleButton offButton;
    private Button saveButton;
    private Button deleteButton;
    private String selectedDate;
    private CalendarDB db;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // XML 레이아웃에서 EditText와 Button을 가져옵니다.
        noteEditText = findViewById(R.id.editTextNote);
        dayButton = findViewById(R.id.buttonDay);
        nightButton = findViewById(R.id.buttonNight);
        morningButton = findViewById(R.id.buttonMorning);
        offButton = findViewById(R.id.buttonOff);
        saveButton = findViewById(R.id.buttonSave);
        deleteButton = findViewById(R.id.buttonDelete);

        // 선택된 날짜를 인텐트에서 가져옵니다.
        selectedDate = getIntent().getStringExtra("selectedDate");

        db = new CalendarDB(this);

        // 불러오기
        String savedNote = db.loadNote(selectedDate);
        noteEditText.setText(savedNote);
        //db.logAllNotes();

        // 불러오기
        dayButton.setChecked(false);
        nightButton.setChecked(false);
        morningButton.setChecked(false);
        offButton.setChecked(false);

        // 각 버튼의 배경색을 초기화
        dayButton.setBackgroundResource(R.drawable.toggle_off);
        nightButton.setBackgroundResource(R.drawable.toggle_off);
        morningButton.setBackgroundResource(R.drawable.toggle_off);
        offButton.setBackgroundResource(R.drawable.toggle_off);
        String savedType = db.loadType(selectedDate);
        if (savedType != null) {
            switch (savedType) {
                case "주":
                    dayButton.setChecked(true);
                    dayButton.setBackgroundResource(R.drawable.toggle_on);
                    break;
                case "야":
                    nightButton.setChecked(true);
                    nightButton.setBackgroundResource(R.drawable.toggle_on);
                    break;
                case "조":
                    morningButton.setChecked(true);
                    morningButton.setBackgroundResource(R.drawable.toggle_on);
                    break;
                case "비":
                    offButton.setChecked(true);
                    offButton.setBackgroundResource(R.drawable.toggle_on);
                    break;
                default:
                    break;
            }
        }

        dayButton.setOnClickListener(v -> setSelectedButton(dayButton, "주"));
        nightButton.setOnClickListener(v -> setSelectedButton(nightButton, "야"));
        morningButton.setOnClickListener(v -> setSelectedButton(morningButton, "조"));
        offButton.setOnClickListener(v -> setSelectedButton(offButton, "비"));

        // 저장 버튼 클릭 시
        saveButton.setOnClickListener(v -> {
            String note = noteEditText.getText().toString();
            String type = null;

            if (dayButton.isChecked()) {
                type = "주";
            } else if (nightButton.isChecked()) {
                type = "야";
            } else if (morningButton.isChecked()) {
                type = "조";
            } else if (offButton.isChecked()) {
                type = "비";
            }

            // 공휴일 여부 확인 후 메모 및 근무형태 저장
            String finalType = type;
            db.isHoliday(selectedDate, new CalendarDB.HolidayCallback() {
                @Override
                public void onHolidayChecked(boolean isHoliday) {
                    // 공휴일 정보 확인 후 메모 및 근무형태 저장
                    db.add(selectedDate, note, finalType, isHoliday);

                    // 결과 전달 및 액티비티 종료
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("selectedDate", selectedDate);
                    setResult(RESULT_OK, resultIntent);

                    db.logAll();  // 로그 출력
                    finish();     // 저장 후 종료
                }

                @Override
                public void onFailure() {
                    Log.e("SAVE_ERROR", "공휴일 업데이트 실패");
                }
            });
        });

        // 삭제
        deleteButton.setOnClickListener(v -> {
            // 1. 메모 내용이 있는 경우
            if (savedNote != null && !savedNote.isEmpty()) {
                db.delete(selectedDate);
            }
            // 2. 근무형태가 있는 경우
            if (savedType != null && !savedType.isEmpty()) {
                db.delete(selectedDate);
            }

            db.logAll();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDate", selectedDate);
            setResult(RESULT_OK, resultIntent);
            finish(); // 삭제 후 종료
        });
    }

    // 선택된 버튼의 배경색을 변경하고 나머지 버튼의 선택을 해제하는 메소드
    private void setSelectedButton(ToggleButton selectedButton, String type) {
        dayButton.setChecked(selectedButton == dayButton);
        nightButton.setChecked(selectedButton == nightButton);
        morningButton.setChecked(selectedButton == morningButton);
        offButton.setChecked(selectedButton == offButton);

        // 각 버튼의 배경색을 초기화
        dayButton.setBackgroundResource(R.drawable.toggle_off);
        nightButton.setBackgroundResource(R.drawable.toggle_off);
        morningButton.setBackgroundResource(R.drawable.toggle_off);
        offButton.setBackgroundResource(R.drawable.toggle_off);

        // 선택된 버튼의 배경색을 변경
        if (selectedButton.isChecked()) {
            selectedButton.setChecked(true);
            selectedButton.setBackgroundResource(R.drawable.toggle_on);
        } else {
            selectedButton.setChecked(false);
            selectedButton.setBackgroundResource(R.drawable.toggle_off);
        }
    }
}
