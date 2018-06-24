package com.cookandroid.androidproject_weatherv2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by cjstk on 2017-11-28.
 */
public class MainWeather extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    getaddress getaddressThread;
    static Intent updateWidgetIntent;
    static ProgressDialog progress;
    getaddress geocode;
    MyLocationListener listener;
    LocationManager manager;
    static MyLocationListener listener2;
    static LocationManager manager2;
    listWeatherView listadapter;        //날씨정보를 뿌려주는 리스트뷰용 어댑터
    ArrayAdapter<String> sidoAdapter, gugunAdapter, dongAdapter;    //시도,구군,동면 정보를 뿌려주는 스피너용 어댑터
    Spinner sidoSpinner, gugunSpinner, dongSpinner;    //시도스피너,구군스피너,동면스피너
    Button getBtn, gpsBtn;        //날씨 가져오는 버튼,gps 버튼
    TextView text;        //날씨지역및 발표시각정보
    ListView listView1;    //날씨정보를 뿌려줄 리스트뷰

    String tempDong = "4215025000";    //기본dongcode
    String sCategory, sTm;    //동네,발표시각
    String[] sHour, sDay, sTemp, sWdKor, sReh, sWfKor;    //예보시간,날짜,온도,풍향,습도,날씨

    //DB용 변수
    String[] sidonum, Nsidonum, sidoname;    //시도 코드,구군 table의 시도 코드,시도 이름
    String[] gugunnum, Ngugunnum, gugunname;    //구군 코드,동네 table의 구군 코드,구군 이름
    String[] dongnum, dongname;    //동 코드,동 이름
    String[] gridx, gridy, id;    //x좌표,y좌표,id값


    static double latitude, longitutde;    //위도와 경도를 저장할 변수
    static SQLiteDatabase db;    //디비

    int data = 0;            //이건 파싱해서 array로 넣을때 번지
    static boolean updated;    //이건 날씨정보 뿌리기위한 플래그
    boolean bCategory, bTm, bHour, bDay, bTemp, bWdKor, bReh, bItem, bWfKor;    //여긴 저장을 위한 플래그들
    boolean tCategory, tTm, tItem;    //이건 text로 뿌리기위한 플래그
    boolean parserEnd;   //지오코딩용 파서 끝 flag
    //static boolean startUpdate=false;
    Handler handler, handler2;    //날씨저장 핸들러,지오코딩파서용 핸들러

    String dbFile = "weather1.db3";
    //    String dbFolder = "/data/data/com.answerofgod.weather/datebases/";
    String dbFolder = "/data/data/com.cookandroid.androidproject_weatherv2/datebases/";
    //    String dbFolder=this.getFilesDir().getPath()+"/data/com.cookandroid.androidproject_weatherv2/datebases/";
    String numDong, numSido, numGugun;    //최종적으로 가져다 붙일 동네코드,시도 코드,구군 코드가 저장되는 변수
    static String sSido_name, sDong_name, sGugun_name;    //gps로 지오코딩후 주소를 파서해서 저장할 변수

    final int tableSido = 1, tableGugun = 2, tableDong = 3; //이건 switch case문에서 쓸려고 만든 변수

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainweather);
        checkDB();
        init();
        queryData(tableSido);    //시도 DB
//        progress = ProgressDialog.show(this, "Wait", "날씨 데이터 가져오기!");
        getMyLocation();

    }

    public class MainReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Toast.makeText(context, action, Toast.LENGTH_SHORT).show();


            if (action.equals(WeatherWidget.REQUEST_WEATHER_INFO)) {
                try {
                    //Toast.makeText(context, "브로드캐스팅을 수신함", Toast.LENGTH_SHORT).show();
                    long minTime = 60000;        //every 60sec
                    float minDistance = 1;    //if moved over 1miter

                    //manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime ,minDistance, listener);//GPS
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                    manager2.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener2);//기지국

                } catch (Exception e) {
                    Log.d("receiver", String.valueOf(e));
                    updateWidgetIntent = new Intent(context, WeatherWidget.class);
                    updateWidgetIntent.putExtra("name", "동네정보가 없어요");
                    updateWidgetIntent.putExtra("weathericon", "날씨정보가 없어요");
                    updateWidgetIntent.putExtra("weather", "날씨정보가 없어요");
                    updateWidgetIntent.setAction(WeatherWidget.SETTING_WEATHER_INFO);
                    context.sendBroadcast(updateWidgetIntent);
                }

            }


        }

    }


    /**
     * DB가 있는지 체크
     *
     * @author Ans
     */
    private void checkDB() {

        try {
            boolean bResult = isCheckDB(getBaseContext());    // DB가 있는지?

            if (!bResult) {    // DB가 없으면
                copyDB(getBaseContext());    //bd복사
                Toast.makeText(getApplicationContext(), "DB를 만들어요", Toast.LENGTH_SHORT).show();
            } else {            //DB가 있으면
                Toast.makeText(getApplicationContext(), "이미 DB가있어요", Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e) {    //예외발생용

            Toast.makeText(getApplicationContext(), "예외가 발생했어요", Toast.LENGTH_SHORT).show();
        }

    }


    public void init() {
        handler = new Handler();    //스레드&핸들러처리
        handler2 = new Handler();    //스레드&핸들러처리

        sidoSpinner = (Spinner) findViewById(R.id.sidospinner);    //시도용 스피너
        gugunSpinner = (Spinner) findViewById(R.id.gugunspinner);    //구군용 스피너
        dongSpinner = (Spinner) findViewById(R.id.dongspinner);    //동면용 스피너
        listView1 = (ListView) findViewById(R.id.listView1);        //날씨정보 리스트뷰

        bCategory = bTm = bHour = bTemp = bWdKor = bReh = bDay = bWfKor = tCategory = tTm = tItem = false;    //부울상수는 false로 초기화해주자
        listadapter = new listWeatherView(getApplicationContext());    //리스트뷰를 만들어주자
        listView1.setAdapter(listadapter);                    //어댑터와 리스트뷰를 연결
        text = (TextView) findViewById(R.id.textView1);    //텍스트 객체생성
        getBtn = (Button) findViewById(R.id.getBtn);        //버튼 객체생성
        gpsBtn = (Button) findViewById(R.id.gpsBtn);        //버튼 객체생성


        sidoSpinner.setOnItemSelectedListener(this);
        gugunSpinner.setOnItemSelectedListener(this);
        dongSpinner.setOnItemSelectedListener(this);

        getBtn.setOnClickListener(this);
        gpsBtn.setOnClickListener(this);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    //GPS 서비스
        listener = new MyLocationListener();    //리스너 만들자
        listener2 = new MyLocationListener();    //리스너 만들자
        manager2 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    //GPS 서비스
        getaddressThread = new getaddress();        //지오코딩할 스레드
        getaddressThread.start();
    }


    /**
     * DB를 가져오는 부분
     * 시도, 구군, 동면 모두 테이블명과 레코드가 다르기때문에 case문을 썼는데
     * 코드가 너무 길어짐;;
     *
     * @param table
     * @author Ans
     */
    private void queryData(final int table) {

        openDatabase(dbFolder + dbFile);    //db가 저장된 폴더에서 db를 가지고 온다
        String sql = null;                //sql명령어를 저장할 변수
        Cursor cur = null;                //db가져올 커서
        int Count;                        //db갯수 셀 변수
        Log.e("table", table + "");
        switch (table) {

            case tableSido:
                sql = "select sido_num, sido_name from t_sido";    //시도 테이블에선 시도코드와 시도이름
                cur = db.rawQuery(sql, null);                        //커서에 넣자
                break;
            case tableGugun:                                    //구군 테이블에선 시도에서 선택된 시도의 구군정보만
                sql = "select sido_num, gugun_num, gugun_name from t_gugun where sido_num = " + numSido;
                cur = db.rawQuery(sql, null);
                break;
            case tableDong:                                        //동면 테이블도 선택된 구군코드와 비교해서
                sql = "select gugun_num, dong_num, dong_name, gridx, gridy, _id from t_dong where gugun_num = " + numGugun;
                cur = db.rawQuery(sql, null);
                break;
            default:
                break;
        }

        Count = cur.getCount();    //db의 갯수를 세고

        switch (table) {

            case tableSido:

                sidoname = new String[Count];    //갯수만큼 배열을 만든다
                sidonum = new String[Count];

                if (cur != null) {    //이부분이 커서로 데이터를 읽어와서 변수에 저장하는 부분
                    cur.moveToFirst();
                    startManagingCursor(cur);
                    for (int i = 0; i < Count; i++) {
                        sidonum[i] = cur.getString(0);
                        sidoname[i] = cur.getString(1);
                        cur.moveToNext();
                    }
                    //변수에 저장이 되었으니 스피너를 만들어 뿌려주자
                    sidoAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, sidoname);    //어댑터를 통해 스피너에 donglist 넣어줌
                    sidoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    //dropdown형식
                    sidoSpinner.setAdapter(sidoAdapter);    //스피너 생성

                }
                break;
            case tableGugun:    //구군도 같은작업

                Nsidonum = new String[Count];
                gugunnum = new String[Count];
                gugunname = new String[Count];
                if (cur != null) {
                    cur.moveToFirst();
                    startManagingCursor(cur);
                    for (int i = 0; i < Count; i++) {
                        Nsidonum[i] = cur.getString(0);
                        gugunnum[i] = cur.getString(1);
                        gugunname[i] = cur.getString(2);
                        cur.moveToNext();
                    }
                    gugunAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, gugunname);    //어댑터를 통해 스피너에 donglist 넣어줌
                    gugunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    //dropdown형식
                    gugunSpinner.setAdapter(gugunAdapter);

                }
                break;

            case tableDong:        //동면도 같은작업

                Ngugunnum = new String[Count];
                dongnum = new String[Count];
                dongname = new String[Count];
                gridx = new String[Count];
                gridy = new String[Count];
                id = new String[Count];
                if (cur != null) {
                    cur.moveToFirst();
                    startManagingCursor(cur);
                    for (int i = 0; i < Count; i++) {
                        Ngugunnum[i] = cur.getString(0);
                        dongnum[i] = cur.getString(1);
                        dongname[i] = cur.getString(2);
                        gridx[i] = cur.getString(3);
                        gridy[i] = cur.getString(4);
                        id[i] = cur.getString(5);
                        cur.moveToNext();
                    }
                    //cur.close();
                    dongAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, dongname);    //어댑터를 통해 스피너에 donglist 넣어줌
                    dongAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);    //dropdown형식
                    dongSpinner.setAdapter(dongAdapter);

                }
                break;

            default:
                break;


        }

    }

    /**
     * 이부분이 db를 열어주는 부분
     *
     * @param databaseFile
     * @author Ans
     */
    public static void openDatabase(String databaseFile) {


        try {
            db = SQLiteDatabase.openDatabase(databaseFile, null, SQLiteDatabase.OPEN_READWRITE);

        } catch (SQLiteException ex) {
            ex.printStackTrace();
        }
    }

    public static void closeDatabase() {
        try {
            // close database
            db.close();
        } catch (Exception ext) {
            ext.printStackTrace();

        }
    }


    // DB가 있나 체크하기
    public boolean isCheckDB(Context mContext) {

        String filePath = dbFolder + dbFile;
        File file = new File(filePath);

        if (file.exists()) {    //db폴더에 파일이 있으면 true
            return true;
        }

        return false;        //아님 default로 false를 반환

    }

    // DB를 복사하기
    // assets의 /db/xxxx.db 파일을 설치된 프로그램의 내부 DB공간으로 복사하기
    public void copyDB(Context mContext) {    //만약 db가 없는 경우 복사를 해야된다.

        AssetManager manager = mContext.getAssets();    //asserts 폴더에서 파일을 읽기위해 쓴단다.아직 잘
        String folderPath = dbFolder;    //db폴더			//일단 DB를 이 폴더에 저장을 하였으니 써야겠지?
        String filePath = dbFolder + dbFile; //db폴더와 파일경로
        File folder = new File(folderPath);
        File file = new File(filePath);

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            InputStream is = manager.open("db/" + dbFile);    //일던 asserts폴더밑 db폴더에서 db를 가져오자
            BufferedInputStream bis = new BufferedInputStream(is);

            if (folder.exists()) {            //우리가 복사하려는 db폴더가 이미 있으면 넘어가고
            } else {
                folder.mkdirs();                //없을경우 폴더를 만들자
            }


            if (file.exists()) {                //파일이 있다면
                file.delete();                //일단 지우고
                file.createNewFile();        //새 파일을 만들자
            }

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            int read = -1;
            byte[] buffer = new byte[1024];    //buffer는 1024byte니깐 1k로 지정해주고
            while ((read = bis.read(buffer, 0, 1024)) != -1) {    //db파일을 읽어서 buffer에 넣고
                bos.write(buffer, 0, read);                            //buffer에서 새로 만든 파일에 쓴다
            }                                                    //대충이해는 되는데 어렵네;;

            bos.flush();
            bos.close();
            fos.close();
            bis.close();
            is.close();

        } catch (IOException e) {

        }
    }


    /**
     * 기상청을 연결하여 정보받고 뿌려주는 스레드
     *
     * @author Ans
     */
    class network_thread extends Thread {    //기상청 연결을 위한 스레드
        /**
         * 기상청을 연결하는 스레드
         * 이곳에서 풀파서를 이용하여 기상청에서 정보를 받아와 각각의 array변수에 넣어줌
         *
         * @author Ans
         */
        public void run() {


            try {
                updated = false;
                sHour = new String[100];    //예보시간(사실 15개밖에 안들어오지만 넉넉하게 20개로 잡아놓음)
                sDay = new String[100];    //날짜
                sTemp = new String[100];    //현재온도
                sWdKor = new String[100];    //풍향
                sReh = new String[100];    //습도
                sWfKor = new String[100];    //날씨
                data = 0;
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();    //이곳이 풀파서를 사용하게 하는곳
                factory.setNamespaceAware(true);                                    //이름에 공백도 인식
                XmlPullParser xpp = factory.newPullParser();                            //풀파서 xpp라는 객체 생성

                String weatherUrl = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=" + numDong;    //이곳이 기상청URL
                URL url = new URL(weatherUrl);        //URL객체생성
                Log.e("url", weatherUrl);
                InputStream is = url.openStream();    //연결할 url을 inputstream에 넣어 연결을 하게된다.
                xpp.setInput(is, "UTF-8");            //이렇게 하면 연결이 된다. 포맷형식은 utf-8로

                int eventType = xpp.getEventType();    //풀파서에서 태그정보를 가져온다.

                while (eventType != XmlPullParser.END_DOCUMENT) {    //문서의 끝이 아닐때

                    switch (eventType) {
                        case XmlPullParser.START_TAG:    //'<'시작태그를 만났을때

                            if (xpp.getName().equals("category")) {    //태그안의 이름이 카테고리일떄 (이건 동네이름이 나온다)
                                bCategory = true;

                            }
                            if (xpp.getName().equals("pubDate")) {        //발표시각정보
                                bTm = true;

                            }
                            if (xpp.getName().equals("hour")) {        //예보시간
                                bHour = true;

                            }
                            if (xpp.getName().equals("day")) {        //예보날(오늘 내일 모레)
                                bDay = true;

                            }
                            if (xpp.getName().equals("temp")) {        //예보시간기준 현재온도
                                bTemp = true;

                            }
                            if (xpp.getName().equals("wdKor")) {    //풍향정보
                                bWdKor = true;

                            }
                            if (xpp.getName().equals("reh")) {        //습도정보
                                bReh = true;

                            }
                            if (xpp.getName().equals("wfKor")) {    //날씨정보(맑음, 구름조금, 구름많음, 흐림, 비, 눈/비, 눈)
                                bWfKor = true;

                            }

                            break;

                        case XmlPullParser.TEXT:    //텍스트를 만났을때
                            //앞서 시작태그에서 얻을정보를 만나면 플래그를 true로 했는데 여기서 플래그를 보고
                            //변수에 정보를 넣어준 후엔 플래그를 false로~
                            if (bCategory) {                //동네이름
                                sCategory = xpp.getText();
                                bCategory = false;
                            }
                            if (bTm) {                    //발표시각
                                sTm = xpp.getText();
                                bTm = false;
                            }
                            if (bHour) {                //예보시각
                                sHour[data] = xpp.getText();
                                bHour = false;
                            }
                            if (bDay) {                //예보날짜
                                sDay[data] = xpp.getText();
                                bDay = false;
                            }
                            if (bTemp) {                //현재온도
                                sTemp[data] = xpp.getText();
                                bTemp = false;
                            }
                            if (bWdKor) {                //풍향
                                sWdKor[data] = xpp.getText();
                                bWdKor = false;
                            }
                            if (bReh) {                //습도
                                sReh[data] = xpp.getText();
                                bReh = false;
                            }
                            if (bWfKor) {                //날씨
                                sWfKor[data] = xpp.getText();
                                bWfKor = false;
                            }
                            break;

                        case XmlPullParser.END_TAG:        //'</' 엔드태그를 만나면 (이부분이 중요)

                            if (xpp.getName().equals("item")) {    //태그가 끝나느 시점의 태그이름이 item이면(이건 거의 문서의 끝
                                tItem = true;                        //따라서 이때 모든 정보를 화면에 뿌려주면 된다.
                                view_text();                    //뿌려주는 곳~
                            }
                            if (xpp.getName().equals("pubDate")) {    //이건 발표시각정보니까 1번만나오므로 바로 뿌려주자
                                tTm = true;
                                view_text();
                            }
                            if (xpp.getName().equals("category")) {    //이것도 동네정보라 바로 뿌려주면 됨
                                tCategory = true;
                                view_text();
                            }
                            if (xpp.getName().equals("data")) {    //data태그는 예보시각기준 예보정보가 하나씩이다.
                                data++;                            //즉 data태그 == 예보 개수 그러므로 이때 array를 증가해주자
                            }
                            break;
                    }
                    eventType = xpp.next();    //이건 다음 이벤트로~
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * 이 부분이 뿌려주는곳
         * 뿌리는건 핸들러가~
         *
         * @author Ans
         */
        private void view_text() {

            handler.post(new Runnable() {    //기본 핸들러니깐 handler.post하면됨

                @Override
                public void run() {


                    if (tCategory) {    //동네이름 들어왔다
                        text.setText(text.getText() + "지역:" + sCategory + "\n");
                        tCategory = false;
                    }
                    if ((tTm) && (sTm.length() > 11)) {        //발표시각 들어왔다
                        text.setText(text.getText() + "발표시각:" + sTm + "\n");
                        tTm = false;
                    }
                    if (tItem) {        //문서를 다 읽었다

                        for (int i = 0; i < data; i++) {    //array로 되어있으니 for문으로
                            if (sDay[i] != null) {        //이건 null integer 에러 예방을 위해(String은 null이 가능하지만intger는 안되니깐)
                                if (sDay[i].equals("0")) {    //발표시각이 0이면 오늘
                                    sDay[i] = "날짜:" + "오늘";

                                } else if (sDay[i].equals("1")) {    //1이면 내일
                                    sDay[i] = "날짜:" + "내일";

                                } else if (sDay[i].equals("2")) {    //2이면 모레
                                    sDay[i] = "날짜:" + "모레";

                                }
                            }

                        }

                        listadapter.setDay(sDay);    //날씨정보를 listview로 뿌려보자
                        listadapter.setTime(sHour);
                        listadapter.setTemp(sTemp);
                        listadapter.setWind(sWdKor);
                        listadapter.setHum(sReh);
                        listadapter.setWeather(sWfKor);
                        updated = true;                    //정보가 담겼으니 flag를 true로
                        listadapter.notifyDataSetChanged();
                        tItem = false;
                        data = 0;        //다음에 날씨를 더가져오게 되면 처음부터 저장해야겠지?
                        updateWidgetIntent = new Intent(getApplicationContext(), WeatherWidget.class);
                        updateWidgetIntent.putExtra("name", sCategory);
                        updateWidgetIntent.putExtra("weathericon", sWfKor[0]);
                        updateWidgetIntent.putExtra("weather", sWfKor[0] + " " + sTemp[0] + "도 " + sReh[0] + "%");
                        updateWidgetIntent.setAction(WeatherWidget.SETTING_WEATHER_INFO);
                        getApplicationContext().sendBroadcast(updateWidgetIntent);
//                        progress.dismiss();


                    }


                }
            });
        }
    }

    /**
     * 이곳에서 GPS방법과 리스너등을 세팅해 준다
     *
     * @author Ans
     */
    final public void getMyLocation() {


        long minTime = 60000;        //every 60sec
        float minDistance = 1;    //if moved over 1miter


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);//GPS

        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener);//기지국


    }


    /**
     * 이부분은 지오코딩해서 파서하는 곳
     *
     * @author Administrator
     */
    class getaddress extends Thread {    //국내라 정확한 지오코딩을 위해 daum api이용

        String key = "8b0153f352c2bdfbb1aacfb35f83585c77ceb38f";
        String url = "http://apis.daum.net/local/geo/coord2addr?apikey=";

        public void run() {

            try {
                XmlPullParserFactory fa = XmlPullParserFactory.newInstance();
                fa.setNamespaceAware(true);
                XmlPullParser xp = fa.newPullParser();

                String geocode = url + key + "&longitude=" + longitutde + "&latitude=" + latitude + "&format=simple&output=xml&inputCoordSystem=WGS84";
                URL url = new URL(geocode);
                //URL객체생성
                InputStream is = url.openStream();

                xp.setInput(is, "UTF-8");
                int eventType = xp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {    //문서의 끝이 아닐때

                    switch (eventType) {
                        case XmlPullParser.START_TAG:    //'<'시작태그를 만났을때

                            String tag = xp.getName();
                            if (tag.compareTo("rcode") == 0) { //파서가 result 태그를 만나면 x의 y의 속성 값을 각각 longitude,latitude에 넣음.
                                sSido_name = xp.getAttributeValue(null, "name1");
                                sGugun_name = xp.getAttributeValue(null, "name2");
                                sDong_name = xp.getAttributeValue(null, "name3");
                                parserEnd = true;
                            }
                            break;
                    }
                    eventType = xp.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (parserEnd) {
                showtext();
            }
        }

        private void showtext() {
            handler2.post(new Runnable() {

                @Override
                public void run() {

                    String getgugunTemp = null;

                    Toast.makeText(getApplicationContext(), "현재 위치는 " + sSido_name + " " + sGugun_name + " " + sDong_name, Toast.LENGTH_SHORT).show();
                    String sql = "select gugun_num, dong_num, dong_name from t_dong where dong_name = " + "'" + sDong_name + "'";
                    Cursor cur = db.rawQuery(sql, null);

                    int cnt = cur.getCount();
                    String[] dongNumTemp = new String[cnt];
                    String[] gugunNumTemp = new String[cnt];

                    if (cnt == 1) {
                        cur.moveToFirst();
                        numDong = cur.getString(1);
                    } else if (cur.getCount() > 1) {
                        cur.moveToFirst();
                        for (int i = 0; i < cnt; i++) {
                            gugunNumTemp[i] = cur.getString(0);
                            dongNumTemp[i] = cur.getString(1);
                            cur.moveToNext();
                        }
                        //cur.close();
                        String sql2 = "select sido_num, gugun_num, gjgun_name from t_gugun where gjgun_name = " + "'" + sGugun_name + "'";
                        Cursor cur2 = db.rawQuery(sql, null);
                        int cnt2 = cur2.getCount();
                        String[] gugunNum2Temp = new String[cnt2];
                        String[] sidoNumTemp = new String[cnt2];
                        if (cnt2 == 1) {
                            cur2.moveToFirst();
                            for (int i = 0; i < cnt; i++) {
                                gugunNum2Temp[0] = cur2.getString(1);
                                sidoNumTemp[i] = cur2.getString(0);
                                if (gugunNum2Temp[0] == gugunNumTemp[i]) {
                                    numDong = dongNumTemp[0];
                                    break;
                                }

                            }
                        } else if (cnt2 > 1) {
                            cur2.moveToFirst();
                            for (int i = 0; i < cnt2; i++) {
                                gugunNum2Temp[i] = cur2.getString(1);
                                sidoNumTemp[i] = cur2.getString(0);
                                cur.moveToNext();
                            }
                            //cur2.close();
                            String sql3 = "select sido_num, sido_name from t_sido where sido_name = " + "'" + sSido_name + "'";
                            Cursor cur3 = db.rawQuery(sql, null);
                            int cnt3 = cur3.getCount();
                            String sidoNum2Temp;
                            cur3.moveToFirst();
                            sidoNum2Temp = cur3.getString(0);
                            //cur3.close();
                            for (int i = 0; i < cnt2; i++) {
                                if (sidoNum2Temp == sidoNumTemp[i]) {
                                    getgugunTemp = sidoNumTemp[i];
                                    break;
                                }
                            }
                            for (int i = 0; i < cnt; i++) {
                                if (getgugunTemp == gugunNumTemp[i]) {
                                    numDong = dongNumTemp[i];
                                }
                            }

                        }

                    }

                    network_thread thread = new network_thread();        //스레드생성(UI 스레드사용시 system 뻗는다)
                    thread.start();    //스레드 시작


                }
            });

        }


    }

    /**
     * 이곳에서 실제로 지피에스 정보를 받아온다
     *
     * @author Ans
     */
    class MyLocationListener extends Context implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            latitude = location.getLatitude();//get latitude
            longitutde = location.getLongitude();//get longitutde
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager.removeUpdates(listener);    //지피에스 서비스 종료(이부분을 주석처리하면 설정한거대로 계속 받아옴)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                manager2.removeUpdates(listener2);
            } catch (Exception e) {
                Log.d("locationlistener", String.valueOf(e));
            }
            getaddress thread = new getaddress();        //지오코딩할 스레드

            thread.start();    //스레드 시작
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public AssetManager getAssets() {
            return null;
        }

        @Override
        public Resources getResources() {
            return null;
        }

        @Override
        public PackageManager getPackageManager() {
            return null;
        }

        @Override
        public ContentResolver getContentResolver() {
            return null;
        }

        @Override
        public Looper getMainLooper() {
            return null;
        }

        @Override
        public Context getApplicationContext() {
            return null;
        }

        @Override
        public void setTheme(int i) {

        }

        @Override
        public Resources.Theme getTheme() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public String getPackageName() {
            return null;
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return null;
        }

        @Override
        public String getPackageResourcePath() {
            return null;
        }

        @Override
        public String getPackageCodePath() {
            return null;
        }

        @Override
        public SharedPreferences getSharedPreferences(String s, int i) {
            return null;
        }

        @Override
        public boolean moveSharedPreferencesFrom(Context context, String s) {
            return false;
        }

        @Override
        public boolean deleteSharedPreferences(String s) {
            return false;
        }

        @Override
        public FileInputStream openFileInput(String s) throws FileNotFoundException {
            return null;
        }

        @Override
        public FileOutputStream openFileOutput(String s, int i) throws FileNotFoundException {
            return null;
        }

        @Override
        public boolean deleteFile(String s) {
            return false;
        }

        @Override
        public File getFileStreamPath(String s) {
            return null;
        }

        @Override
        public File getDataDir() {
            return null;
        }

        @Override
        public File getFilesDir() {
            return null;
        }

        @Override
        public File getNoBackupFilesDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalFilesDir(String s) {
            return null;
        }

        @Override
        public File[] getExternalFilesDirs(String s) {
            return new File[0];
        }

        @Override
        public File getObbDir() {
            return null;
        }

        @Override
        public File[] getObbDirs() {
            return new File[0];
        }

        @Override
        public File getCacheDir() {
            return null;
        }

        @Override
        public File getCodeCacheDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalCacheDir() {
            return null;
        }

        @Override
        public File[] getExternalCacheDirs() {
            return new File[0];
        }

        @Override
        public File[] getExternalMediaDirs() {
            return new File[0];
        }

        @Override
        public String[] fileList() {
            return new String[0];
        }

        @Override
        public File getDir(String s, int i) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory, DatabaseErrorHandler databaseErrorHandler) {
            return null;
        }

        @Override
        public boolean moveDatabaseFrom(Context context, String s) {
            return false;
        }

        @Override
        public boolean deleteDatabase(String s) {
            return false;
        }

        @Override
        public File getDatabasePath(String s) {
            return null;
        }

        @Override
        public String[] databaseList() {
            return new String[0];
        }

        @Override
        public Drawable getWallpaper() {
            return null;
        }

        @Override
        public Drawable peekWallpaper() {
            return null;
        }

        @Override
        public int getWallpaperDesiredMinimumWidth() {
            return 0;
        }

        @Override
        public int getWallpaperDesiredMinimumHeight() {
            return 0;
        }

        @Override
        public void setWallpaper(Bitmap bitmap) throws IOException {

        }

        @Override
        public void setWallpaper(InputStream inputStream) throws IOException {

        }

        @Override
        public void clearWallpaper() throws IOException {

        }

        @Override
        public void startActivity(Intent intent) {

        }

        @Override
        public void startActivity(Intent intent, Bundle bundle) {

        }

        @Override
        public void startActivities(Intent[] intents) {

        }

        @Override
        public void startActivities(Intent[] intents, Bundle bundle) {

        }

        @Override
        public void startIntentSender(IntentSender intentSender, Intent intent, int i, int i1, int i2) throws IntentSender.SendIntentException {

        }

        @Override
        public void startIntentSender(IntentSender intentSender, Intent intent, int i, int i1, int i2, Bundle bundle) throws IntentSender.SendIntentException {

        }

        @Override
        public void sendBroadcast(Intent intent) {

        }

        @Override
        public void sendBroadcast(Intent intent, String s) {

        }

        @Override
        public void sendOrderedBroadcast(Intent intent, String s) {

        }

        @Override
        public void sendOrderedBroadcast(Intent intent, String s, BroadcastReceiver broadcastReceiver, Handler handler, int i, String s1, Bundle bundle) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle userHandle) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle userHandle, String s) {

        }

        @Override
        public void sendOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, String s, BroadcastReceiver broadcastReceiver, Handler handler, int i, String s1, Bundle bundle) {

        }

        @Override
        public void sendStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver broadcastReceiver, Handler handler, int i, String s, Bundle bundle) {

        }

        @Override
        public void removeStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {

        }

        @Override
        public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, BroadcastReceiver broadcastReceiver, Handler handler, int i, String s, Bundle bundle) {

        }

        @Override
        public void removeStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {

        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, String s, Handler handler) {
            return null;
        }

        @Override
        public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {

        }

        @Nullable
        @Override
        public ComponentName startService(Intent intent) {
            return null;
        }

        @Override
        public boolean stopService(Intent intent) {
            return false;
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            return false;
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection) {

        }

        @Override
        public boolean startInstrumentation(ComponentName componentName, String s, Bundle bundle) {
            return false;
        }

        @Override
        public Object getSystemService(String s) {
            return null;
        }

        @Override
        public String getSystemServiceName(Class<?> aClass) {
            return null;
        }

        @Override
        public int checkPermission(String s, int i, int i1) {
            return 0;
        }

        @Override
        public int checkCallingPermission(String s) {
            return 0;
        }

        @Override
        public int checkCallingOrSelfPermission(String s) {
            return 0;
        }

        @Override
        public int checkSelfPermission(String s) {
            return 0;
        }

        @Override
        public void enforcePermission(String s, int i, int i1, String s1) {

        }

        @Override
        public void enforceCallingPermission(String s, String s1) {

        }

        @Override
        public void enforceCallingOrSelfPermission(String s, String s1) {

        }

        @Override
        public void grantUriPermission(String s, Uri uri, int i) {

        }

        @Override
        public void revokeUriPermission(Uri uri, int i) {

        }

        @Override
        public int checkUriPermission(Uri uri, int i, int i1, int i2) {
            return 0;
        }

        @Override
        public int checkCallingUriPermission(Uri uri, int i) {
            return 0;
        }

        @Override
        public int checkCallingOrSelfUriPermission(Uri uri, int i) {
            return 0;
        }

        @Override
        public int checkUriPermission(Uri uri, String s, String s1, int i, int i1, int i2) {
            return 0;
        }

        @Override
        public void enforceUriPermission(Uri uri, int i, int i1, int i2, String s) {

        }

        @Override
        public void enforceCallingUriPermission(Uri uri, int i, String s) {

        }

        @Override
        public void enforceCallingOrSelfUriPermission(Uri uri, int i, String s) {

        }

        @Override
        public void enforceUriPermission(Uri uri, String s, String s1, int i, int i1, int i2, String s2) {

        }

        @Override
        public Context createPackageContext(String s, int i) throws PackageManager.NameNotFoundException {
            return null;
        }

        @Override
        public Context createConfigurationContext(Configuration configuration) {
            return null;
        }

        @Override
        public Context createDisplayContext(Display display) {
            return null;
        }

        @Override
        public Context createDeviceProtectedStorageContext() {
            return null;
        }

        @Override
        public boolean isDeviceProtectedStorage() {
            return false;
        }
    }


    /**
     * 버튼에 대한 처리
     */
    @Override
    public void onClick(View v) {
//        progress = ProgressDialog.show(this, "Wait", "Getting the weather data!");
        text.setText("");    //일단 중복해서 누를경우 대비해서 내용 지워줌
        switch (v.getId()) {

            case R.id.getBtn:

                numDong = tempDong;
                network_thread thread = new network_thread();        //스레드생성(UI 스레드사용시 system 뻗는다)
                thread.start();    //스레드 시작

                break;
            case R.id.gpsBtn:
                getMyLocation();    //GPS setting
                break;
            default:
                break;
        }

    }

    /**
     * 스피너에 대한 처리
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (parent.getId()) {

            case R.id.sidospinner:
                numSido = sidonum[position];    //시도가 선택되면 해당 코드를 변수에 넣는다
                queryData(tableGugun);        //구군 DB
                break;
            case R.id.gugunspinner:
                numGugun = gugunnum[position];    //구군이 선택되면 해당 코드를 변수에
                queryData(tableDong);            //동면 DB가
                break;
            case R.id.dongspinner:
                tempDong = dongnum[position];
                numDong = tempDong;    //선택된 동면코드를 변수에 대입
                break;
            default:
                break;

        }

    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    public void NullCheck()
    {
        Boolean check = false;


    }
}