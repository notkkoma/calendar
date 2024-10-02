package com.example.calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider{
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // 위젯 레이아웃 정의
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // 버튼 클릭 시 실행할 인텐트 설정
            Intent intent = new Intent(context, MyWidgetProvider.class);
            intent.setAction("com.example.calendar.BUTTON_CLICK");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            // 버튼에 클릭 이벤트 연결
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

            // 위젯 업데이트
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // 버튼 클릭 이벤트 처리
        if (intent.getAction().equals("com.example.calendar.BUTTON_CLICK")) {
            // 여기에서 버튼 클릭 시 수행할 동작을 정의
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setTextViewText(R.id.widget_text, "Button Clicked!");

            // 위젯 업데이트
            ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
            appWidgetManager.updateAppWidget(thisWidget, views);
        }
    }
}
