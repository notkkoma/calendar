package com.example.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarWidgetProvider extends AppWidgetProvider {

    private Calendar calendar;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // 현재 날짜 가져오기
            calendar = Calendar.getInstance();
            // 위젯 업데이트
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_calendar_layout);

        // 현재 달 표시
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMMM", Locale.getDefault());
        views.setTextViewText(R.id.current_month_text, sdf.format(calendar.getTime()));

        // 이전 달 버튼 클릭 처리
        //Intent prevIntent = new Intent(context, CalendarWidgetProvider.class);
        //prevIntent.setAction("PREV_MONTH");
        //PendingIntent prevPendingIntent = PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //views.setOnClickPendingIntent(R.id.prev_month_button, prevPendingIntent);

        // 다음 달 버튼 클릭 처리
        //Intent nextIntent = new Intent(context, CalendarWidgetProvider.class);
        //nextIntent.setAction("NEXT_MONTH");
        //PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //views.setOnClickPendingIntent(R.id.next_month_button, nextPendingIntent);

        // 달력 날짜 업데이트
        updateCalendarGrid(context, appWidgetManager, appWidgetId, views, calendar);
    }

    private void updateCalendarGrid(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews views, Calendar calendar) {
        // 달력의 첫 번째 날짜 계산
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);

        // 달력 첫 번째 주의 첫 번째 요일 계산
        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        tempCalendar.add(Calendar.DAY_OF_MONTH, - (firstDayOfWeek - 1));

        // 35개의 셀 (5주 * 7일)을 반복하면서 날짜를 채워넣음
        for (int i = 0; i < 35; i++) {
            // TextView ID를 day1부터 day35까지 참조
            String dayId = "day" + (i + 1); // day1, day2, ..., day35
            int dayResId = context.getResources().getIdentifier(dayId, "id", context.getPackageName());

            // 날짜 설정
            views.setTextViewText(dayResId, String.valueOf(tempCalendar.get(Calendar.DAY_OF_MONTH)));

            // 다음 날짜로 이동
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 위젯 강제 업데이트
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals("PREV_MONTH")) {
            calendar.add(Calendar.MONTH, -1);
        } else if (intent.getAction().equals("NEXT_MONTH")) {
            calendar.add(Calendar.MONTH, 1);
        }

        // 위젯 다시 업데이트
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widget = new ComponentName(context, CalendarWidgetProvider.class);
        onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(widget));
    }
}
