package com.cookandroid.androidproject_weatherv2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity {

    private static int ONE_MINUTE = 5626;

    Button start_btn, finish_btn;
    TextView now_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start_btn = (Button) findViewById(R.id.start_btn);
        finish_btn = (Button) findViewById(R.id.exit_btn);

        now_date = (TextView) findViewById(R.id.dateT);

        now_date.setText(getDateString());

        new AlarmHATT(getApplicationContext()).Alarm();

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent weather = new Intent(getApplicationContext(), MainWeather.class);
                startActivity(weather);

                finish();
            }
        });

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);

                dlg.setTitle("어플종료");
                dlg.setMessage("정말로 어플을 종료하시겠습니까?");
                dlg.setNegativeButton("취소", null);

                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dlg.show();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.N)
    public String getDateString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA);
        String str_date = df.format(new Date());

        return str_date;
    }

    public class AlarmHATT {
        private Context context;
        public AlarmHATT(Context context) {
            this.context=context;
        }
        public void Alarm() {
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(MainActivity.this, BroadcastD.class);

            PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();

            //알람시간 calendar에 set해주기
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 10, 53, 0);

            //알람 예약
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        }
    }
}