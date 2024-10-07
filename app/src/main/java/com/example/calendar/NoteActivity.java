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
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        // XML 레이아웃에서 EditText와 Button을 가져옵니다.
        noteEditText = findViewById(R.id.editTextNote);
        saveButton = findViewById(R.id.buttonSave);

        // 선택된 날짜를 인텐트에서 가져옵니다.
        selectedDate = getIntent().getStringExtra("selectedDate");

        // 저장된 메모가 있으면 불러옵니다.
        loadNote();

        // 저장 버튼 클릭 시 메모 저장
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    // 메모를 SharedPreferences에 저장하는 메서드
    private void saveNote() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyNotes", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 날짜별로 메모를 저장
        editor.putString(selectedDate, noteEditText.getText().toString());
        editor.apply();

        // 저장 후 액티비티 종료
        finish();
    }

    // 저장된 메모를 불러오는 메서드
    private void loadNote() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyNotes", MODE_PRIVATE);

        // 선택한 날짜의 메모가 있으면 불러와서 EditText에 설정
        String savedNote = sharedPreferences.getString(selectedDate, "");
        noteEditText.setText(savedNote);
    }
}
