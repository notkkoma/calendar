package com.example.calendar;

import android.content.Context;
import android.content.SharedPreferences;
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

    private ArrayList<String> daysOfMonth;
    private OnItemListener onItemListener;
    private SharedPreferences sharedPreferences;
    private String selectedDate;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener, Context context) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.sharedPreferences = context.getSharedPreferences("MyNotes", Context.MODE_PRIVATE);
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
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0이 1월이므로 +1
        String date = daysOfMonth.get(position);
        selectedDate = year + "-" + month + "-" + date;
        holder.dayOfMonth.setText(date);

        // 날짜가 빈 셀("")이 아닐 때만 처리
        if (!date.isEmpty()) {
            // 날짜에 메모가 있는지 확인
            String note = sharedPreferences.getString(selectedDate, "");
            if (!note.isEmpty()) {
                Log.d("&&&CalendarAdapter Line47&&&", selectedDate + " " + note);
                holder.dayOfMonth.setText(date + " *");  // 메모가 있는 날짜에 * 표시
                holder.cardView.setCardBackgroundColor(Color.YELLOW);  // 배경색 변경
            } else {
                // 메모가 없으면 기본 색상으로 설정
                Log.d("&&&CalendarAdapter Line47&&&", selectedDate + " " + note);
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