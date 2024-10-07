package com.example.calendar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class NoteActivity extends AppCompatActivity {

    private EditText noteEditText;
    private Button saveButton;
    private Button deleteButton;
    private String selectedDate;
    private CalendarDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // XML 레이아웃에서 EditText와 Button을 가져옵니다.
        noteEditText = findViewById(R.id.editTextNote);
        saveButton = findViewById(R.id.buttonSave);
        deleteButton = findViewById(R.id.buttonDelete);

        // 선택된 날짜를 인텐트에서 가져옵니다.
        selectedDate = getIntent().getStringExtra("selectedDate");

        db = new CalendarDB(this);

        // 불러오기
        String savedNote = db.loadNote(selectedDate);
        noteEditText.setText(savedNote);
        db.logAllNotes();

        // 업데이트 및 저장
        saveButton.setOnClickListener(v -> {
            String note = noteEditText.getText().toString();
            if (savedNote != null && !savedNote.isEmpty()) {
                db.updateNote(selectedDate, note);
                db.logAllNotes();
            }
            else{
                db.addNote(selectedDate, note);
                db.logAllNotes();
            }
            finish(); // 저장 후 종료
        });

        // 삭제
        deleteButton.setOnClickListener(v -> {
            if (savedNote != null && !savedNote.isEmpty()) {
                db.deleteNote(selectedDate);
                db.logAllNotes();
            }
            finish(); // 삭제 후 종료
        });
    }
}
