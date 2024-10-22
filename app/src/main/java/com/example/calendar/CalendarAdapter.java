package com.example.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private int year;
    private int month;
    private ArrayList<String> daysOfMonth;
    private OnItemListener onItemListener;
    private String selectedDate;
    private CalendarDB db;

    public CalendarAdapter(int year, int month, ArrayList<String> daysOfMonth, OnItemListener onItemListener, Context context) {
        this.year = year;
        this.month = month;
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        db = new CalendarDB(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        return new ViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = daysOfMonth.get(position);
        //Log.d("CalendarActivity", "★ String date L45: " + date);

        // 화면에는 1, DB에는 01
        if(!date.isEmpty()){
            int day = Integer.parseInt(date); // date를 int로 변환
            @SuppressLint("DefaultLocale") String tmp = String.format("%02d", day);
            selectedDate = year + "-" + month + "-" + tmp;
            Log.d("CalendarActivity", "★ selectedDate L52: " + selectedDate);
        }

        holder.dayOfMonth.setText(date);

        // 날짜가 빈 셀("")이 아닐 때만 처리
        if (!date.isEmpty()) {
            // 날짜에 메모가 있는지 확인
            String note = db.loadNote(selectedDate);
            if (note != null && !note.isEmpty()) {
                // 메모가 있으면 특수 색상으로 설정
                // holder.dayOfMonth.setText(date + " *");  // 메모가 있는 날짜에 * 표시
                holder.cardView.setCardBackgroundColor(Color.YELLOW);  // 배경색 변경
            } else {
                // 메모가 없으면 기본 색상으로 설정
                holder.cardView.setCardBackgroundColor(Color.WHITE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView cardView;
        TextView dayOfMonth;
        OnItemListener onItemListener;

        public ViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            cardView = itemView.findViewById(R.id.cardView);  // CardView 참조 추가
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition(), dayOfMonth.getText().toString());
        }
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}