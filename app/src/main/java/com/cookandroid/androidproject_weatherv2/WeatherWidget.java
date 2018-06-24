package com.cookandroid.androidproject_weatherv2;

/**
 * Created by cjstk on 2017-11-21.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WeatherWidget extends AppWidgetProvider {

    Handler handler;

    Intent requestUpdate;
    Intent iUpdate;
    Intent iOpen;

    int weatherBmp;

    AppWidgetManager appWidgetManager;

    RemoteViews views;

    PendingIntent pOpen;
    PendingIntent pUpdate;

    String name = null;
    String weather = null;
    String weathericon = null;

    MainWeather main;

    public static String WIDGETUPDATE = "com.cookandroid.androidproject_weatherv2.widget.WIDGETUPDATE";
    public static String SETTING_WEATHER_INFO = "com.cookandroid.androidproject_weatherv2.widget.SETTING_WEATHER_INFO";
    public static String REQUEST_WEATHER_INFO = "com.cookandroid.androidproject_weatherv2.widget.REQUEST_WEATHER_INFO";


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {

        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {

        super.onEnabled(context);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);

        final String action = intent.getAction();

        Toast.makeText(context, action, Toast.LENGTH_SHORT).show();


        if (action.equals(WIDGETUPDATE)) {

            getweather(context);

        }
        if (action.equals(SETTING_WEATHER_INFO)) {

            myUpdate(context);

        }


    }

    private void getweather(Context context) {
        //Toast.makeText(context, "업데이트요청", Toast.LENGTH_SHORT).show();

        requestUpdate = new Intent();
        requestUpdate.setAction(REQUEST_WEATHER_INFO);

        context.sendBroadcast(requestUpdate);

    }

    private void myUpdate(Context context) {

        appWidgetManager = AppWidgetManager.getInstance(context);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appId = appWidgetIds[i];

            try {
                name = main.updateWidgetIntent.getExtras().getString("name");
                weather = main.updateWidgetIntent.getExtras().getString("weather");
                weathericon = main.updateWidgetIntent.getExtras().getString("weathericon");
            } catch (Exception e) {
                Log.d("widget", String.valueOf(e));
                getweather(context);
            }
            if (name != null & weather != null && weathericon != null) {
                views = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
                views.setTextViewText(R.id.widgetName, name);
                views.setTextViewText(R.id.widgetWeather, weather);

                weatherBmp = setIcon(weathericon);

                views.setImageViewResource(R.id.widgetImg, weatherBmp);

            }
            appWidgetManager.updateAppWidget(appId, views);
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);


        for (int i = 0; i < appWidgetIds.length; i++) {
            int appId = appWidgetIds[i];

            iOpen = new Intent(context, MainActivity.class);
            iUpdate = new Intent(WIDGETUPDATE);

            pOpen = PendingIntent.getActivity(context, 0, iOpen, 0);
            pUpdate = PendingIntent.getBroadcast(context, 0, iUpdate, 0);
            views = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);

            views.setOnClickPendingIntent(R.id.widgetOpen, pOpen);
            views.setOnClickPendingIntent(R.id.widgetUpdate, pUpdate);

            appWidgetManager.updateAppWidget(appId, views);


        }
    }


    public int setIcon(String data) {
        int imgId = 0;

        if (data.toString().equals("맑음"))
            imgId = R.drawable.clear;
        else if (data.toString().equals("흐림"))
            imgId = R.drawable.cloudy;
        else if (data.toString().equals("구름 많음"))
            imgId = R.drawable.mostly_cloudly;
        else if (data.toString().equals("구름 조금"))
            imgId = R.drawable.partly_cloudy;
        else if (data.toString().equals("눈"))
            imgId = R.drawable.snow;
        else if (data.toString().equals("비"))
            imgId = R.drawable.rain;
        else if (data.toString().equals("눈/비"))
            imgId = R.drawable.snow_rain;
        else
            imgId = R.drawable.weather;

        return imgId;


    }


}
