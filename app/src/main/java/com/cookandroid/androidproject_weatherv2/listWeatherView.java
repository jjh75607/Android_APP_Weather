package com.cookandroid.androidproject_weatherv2;

/**
 * Created by cjstk on 2017-11-21.
 */

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


class listWeatherView extends BaseAdapter {

    MainWeather main;
    String[] day, time, temp, wind, hum, weather;
    Context mContext;
    String temp_data[] = new String[15];    //nullpointexception 방지

    public listWeatherView(Context context) {
        mContext = context;
    }

    public void setDay(String[] data) {
        day = data;
    }

    public void setTime(String[] data) {
        time = data;
    }

    public void setTemp(String[] data) {
        temp = data;
    }

    public void setWind(String[] data) {
        wind = data;
    }

    public void setHum(String[] data) {
        hum = data;
    }

    public void setWeather(String[] data) {
        weather = data;
    }

    @Override
    public int getCount() {
        return temp_data.length;        //리스트뷰의 갯수
    }

    @Override
    public Object getItem(int position) {
        return temp_data[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parents) {


        showWeather layout = null;


        if (convertView != null) {    //스크롤로 넘어간 뷰를 버리지 않고 재사용
            layout = (showWeather) convertView;
        } else {
            layout = new showWeather(mContext.getApplicationContext());    //레이아웃 설정

        }

        if (main.updated) {    //날씨정보를 받아왔으면
            try {
                layout.setDate(day[position]);    //레이아웃으로 뿌려줌
                layout.setTime(time[position]);
                layout.setTemp(temp[position]);
                layout.setWind(wind[position]);
                layout.setHum(hum[position]);
                layout.setWeather(weather[position]);
            } catch (Exception e) {
                Log.d("getview", String.valueOf(e));

            }
        }

        return layout;
    }

}
