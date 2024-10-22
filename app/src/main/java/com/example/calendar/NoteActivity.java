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
    private ToggleButton allButton;
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
        allButton = findViewById(R.id.buttonAll);
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
        allButton.setChecked(false);
        offButton.setChecked(false);
        String savedType = db.loadType(selectedDate);
        if (savedType != null) {
            switch (savedType) {
                case "주간":
                    dayButton.setChecked(true);
                    break;
                case "야간":
                    nightButton.setChecked(true);
                    break;
                case "전체":
                    allButton.setChecked(true);
                    break;
                case "비번":
                    offButton.setChecked(true);
                    break;
                default:
                    break;
            }
        }

        dayButton.setOnClickListener(v -> setSelectedButton(dayButton, "주간"));
        nightButton.setOnClickListener(v -> setSelectedButton(nightButton, "야간"));
        allButton.setOnClickListener(v -> setSelectedButton(allButton, "전체"));
        offButton.setOnClickListener(v -> setSelectedButton(offButton, "비번"));

        // 업데이트 및 저장
        saveButton.setOnClickListener(v -> {
            String note = noteEditText.getText().toString();
            db.addNote(selectedDate, note);
            //db.logAllNotes();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDate", selectedDate);
            //Log.d("NoteActivity", "★ Selected date set L47: " + selectedDate);
            setResult(RESULT_OK, resultIntent);
            finish(); // 저장 후 종료
        });

        // 삭제
        deleteButton.setOnClickListener(v -> {
            if (savedNote != null && !savedNote.isEmpty()) {
                db.delete(selectedDate);
                //db.logAllNotes();
            }
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedDate", selectedDate);
            //Log.d("NoteActivity", "★ Selected date set L60: " + selectedDate);
            setResult(RESULT_OK, resultIntent);
            finish(); // 삭제 후 종료
        });
    }

    // 선택된 버튼의 배경색을 변경하고 나머지 버튼의 선택을 해제하는 메소드
    private void setSelectedButton(ToggleButton selectedButton, String type) {
        dayButton.setChecked(selectedButton == dayButton);
        nightButton.setChecked(selectedButton == nightButton);
        allButton.setChecked(selectedButton == allButton);
        offButton.setChecked(selectedButton == offButton);

        // 각 버튼의 배경색을 초기화
        dayButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
        nightButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
        allButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
        offButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));

        // 선택된 버튼의 배경색을 변경
        if (selectedButton.isChecked()) {
            selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue));
            db.addType(selectedDate, type);
        } else {
            db.addType(selectedDate, null);
        }
    }
}
