package com.example.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyWidgetProvider extends AppWidgetProvider {

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // 위젯 업데이트
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // 현재 달 표시
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        views.setTextViewText(R.id.current_month_text, sdf.format(calendar.getTime()));

        // 이전 달 버튼 클릭 처리
        Intent prevIntent = new Intent(context, MyWidgetProvider.class);
        prevIntent.setAction("PREV_MONTH");
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.prev_month_button, prevPendingIntent);

        // 다음 달 버튼 클릭 처리
        Intent nextIntent = new Intent(context, MyWidgetProvider.class);
        nextIntent.setAction("NEXT_MONTH");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.next_month_button, nextPendingIntent);

        // 달력 날짜 업데이트
        updateCalendarGrid(context, views);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void updateCalendarGrid(Context context, RemoteViews views) {
        // 달력 그리드에 날짜 설정
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        tempCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek + 1);

        for (int i = 0; i < 42; i++) {
            int dayResId = context.getResources().getIdentifier("day" + i, "id", context.getPackageName());
            views.setTextViewText(dayResId, String.valueOf(tempCalendar.get(Calendar.DAY_OF_MONTH)));
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
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
        ComponentName widget = new ComponentName(context, MyWidgetProvider.class);
        onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(widget));
    }
}
