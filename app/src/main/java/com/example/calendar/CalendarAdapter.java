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
import androidx.core.content.ContextCompat;
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

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = daysOfMonth.get(position);
        //Log.d("CalendarActivity", "★ String date L45: " + date);

        // 화면에는 1, DB에는 01
        if (!date.isEmpty()) {
            int day = Integer.parseInt(date); // date를 int로 변환
            @SuppressLint("DefaultLocale") String tmp = String.format("%02d", day);
            selectedDate = year + "-" + month + "-" + tmp;
            //Log.d("CalendarActivity", "★ selectedDate L52: " + selectedDate);
        }

        holder.dayOfMonth.setText(date);
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.white));

        // 날짜가 빈 셀("")이 아닐 때만 처리
        if (!date.isEmpty()) {
            // 날짜에 근무형태, 메모가 있는지 확인
            String type = db.loadType(selectedDate);
            String note = db.loadNote(selectedDate);
            // 1. 근무형태
            if (type != null && !type.isEmpty()) {
                // 근무형태가 있으면 특수 색상으로 설정: 주간, 야간, 전체
                switch (type) {
                    case "주간":
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.pink));
                        break;
                    case "야간":
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.grey));
                        break;
                    case "전체":
                        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.red));
                        break;
                }
            }
            // 2. 메모
            if (note != null && !note.isEmpty()) {
                // 메모가 있으면 메모 이모지 추가
                holder.memoIcon.setText("📝");
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
        TextView memoIcon;
        OnItemListener onItemListener;

        public ViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            cardView = itemView.findViewById(R.id.cardView); // CardView 참조 추가
            memoIcon = itemView.findViewById(R.id.memoIcon); // 메모 Icon 추가
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String dayText = dayOfMonth.getText().toString();

            // 빈 셀("")이 아닌 경우에만 클릭 이벤트 처리
            if (!dayText.isEmpty()) {
                onItemListener.onItemClick(getAdapterPosition(), dayText);
            }
        }
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}