package com.cookandroid.androidproject_weatherv2;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ?????????????????? ?????????????????? ???????????????? ?????????????? ????????????????
 * weather.xml?????? inflater????????
 *
 * @author Ans
 */
public class showWeather extends LinearLayout {

    TextView Tdate;
    TextView Ttime;
    TextView Ttemp;
    TextView Twind;
    TextView Thum;
    TextView Tweather;
    ImageView Iweather;


    public showWeather(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public showWeather(Context context) {
        super(context);
        init(context);

    }

    public void setDate(String data) {
        Tdate.setText(data + " ");            //????????


    }

    public void setTime(String data) {
        Ttime.setText(data + "시 ");            //????????


    }

    public void setTemp(String data) {
        Ttemp.setText(data + "도 ");            //????????


    }

    public void setWind(String data) {
        Twind.setText(data + "풍 ");            //????????


    }

    public void setHum(String data) {
        Thum.setText(data + "% ");            //????????????


    }

    public void setWeather(String data) {
        Tweather.setText(data);                //????????????
        //?????????????? ??????????????????????????????
        if (data.toString().equals("맑음")) {
            Iweather.setImageResource(R.drawable.clear);
        } else if (data.toString().equals("흐림")) {
            Iweather.setImageResource(R.drawable.cloudy);
        } else if (data.toString().equals("구름 많음")) {
            Iweather.setImageResource(R.drawable.mostly_cloudly);
        } else if (data.toString().equals("구름 조금")) {
            Iweather.setImageResource(R.drawable.partly_cloudy);
        } else if (data.toString().equals("눈")) {
            Iweather.setImageResource(R.drawable.snow);
        } else if (data.toString().equals("비")) {
            Iweather.setImageResource(R.drawable.rain);
        } else if (data.toString().equals("눈/비")) {
            Iweather.setImageResource(R.drawable.snow_rain);
        } else {
            Iweather.setImageResource(R.drawable.ic_launcher);
        }
        if (data.toString() == null) {
            Iweather.setImageResource(R.drawable.ic_launcher);
        }



    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.weather, this, true);

        Tdate = (TextView) findViewById(R.id.Tdate);
        Ttime = (TextView) findViewById(R.id.Ttime);
        Ttemp = (TextView) findViewById(R.id.Ttemp);
        Twind = (TextView) findViewById(R.id.Twind);

        Thum = (TextView) findViewById(R.id.Thum);

        Tweather = (TextView) findViewById(R.id.Tweather);
        Iweather = (ImageView) findViewById(R.id.Iweather);

    }

    public void Alam() {

    }
}
