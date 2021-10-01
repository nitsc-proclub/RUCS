package com.example.rucs;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.HandlerCompat;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Timetable extends AppCompatActivity {

    private static final String DEBUG_TAG = "AsyncSample";
    private String AllID ="_text";
    private String TIME_URL ="https://fulab.tk/rucs/api/readPreset.php?classID=";

    private static TextView tv[][] = new TextView[6][5];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable);

        TextView time= findViewById(R.id.time);
        TextView week0 =findViewById(R.id.time0);
        TextView week1 =findViewById(R.id.time1);
        TextView week2 =findViewById(R.id.time2);
        TextView week3 =findViewById(R.id.time3);
        TextView week4 =findViewById(R.id.time4);

        tv[0][0]=findViewById(R.id.time00);tv[1][1]=findViewById(R.id.time11);
        tv[1][0]=findViewById(R.id.time01);tv[2][1]=findViewById(R.id.time12);
        tv[2][0]=findViewById(R.id.time02);tv[3][1]=findViewById(R.id.time13);
        tv[3][0]=findViewById(R.id.time03);tv[4][1]=findViewById(R.id.time14);
        tv[4][0]=findViewById(R.id.time04);tv[5][1]=findViewById(R.id.time15);
        tv[5][0]=findViewById(R.id.time05);tv[0][1]=findViewById(R.id.time10);
        tv[0][2]=findViewById(R.id.time20);tv[0][3]=findViewById(R.id.time30);
        tv[1][2]=findViewById(R.id.time21);tv[1][3]=findViewById(R.id.time31);
        tv[2][2]=findViewById(R.id.time22);tv[2][3]=findViewById(R.id.time32);
        tv[3][2]=findViewById(R.id.time23);tv[3][3]=findViewById(R.id.time33);
        tv[4][2]=findViewById(R.id.time24);tv[4][3]=findViewById(R.id.time34);
        tv[5][2]=findViewById(R.id.time25);tv[5][3]=findViewById(R.id.time35);
        tv[0][4]=findViewById(R.id.time40);
        tv[1][4]=findViewById(R.id.time41);
        tv[2][4]=findViewById(R.id.time42);
        tv[3][4]=findViewById(R.id.time43);
        tv[4][4]=findViewById(R.id.time44);
        tv[5][4]=findViewById(R.id.time45);

        //情報取得
        SharedPreferences preGrade = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String pDateClassID = preGrade.getString(AllID,"学年が指定されていません");


        //ページ遷移
        Button returnButton = findViewById(R.id.button_back);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String time_All_url=TIME_URL+pDateClassID;
        //長さ取得
        Log.d("sample", "せいこう" +time_All_url);
        receiveBringInfo(time_All_url);


        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean t_mode = mode.getBoolean("_mode",true);
        if(t_mode){
            returnButton.setText("もどる");
            time.setText("じかんわり");
            week0.setText("げつ");
            week1.setText("か");
            week2.setText("すい");
            week3.setText("もく");
            week4.setText("きん");
        }

    }

    /**持ち物情報の入手*/
    @UiThread
    private  void  receiveBringInfo(final String BRING_URL){
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainLooper);
        Timetable.TimeInfoBackgroundReceiver backgroundReceiver = new Timetable.TimeInfoBackgroundReceiver(handler,BRING_URL);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(backgroundReceiver);
    }

    private  class  TimeInfoBackgroundReceiver implements Runnable {

        private final Handler _hander;
        private final String _bring_url;

        private TimeInfoBackgroundReceiver(Handler handler, String BRING_URL) {
            _hander =handler;
            _bring_url = BRING_URL;
        }

        @WorkerThread
        @Override
        public void run(){

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";

            try {
                URL url = new URL(_bring_url);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(3000);
                con.setReadTimeout(3000);
                con.setRequestMethod("GET");
                con.connect();
                is = con.getInputStream();
                result = is2String(is);
            }
            catch(MalformedURLException ex) {
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            }
            // タイムアウトの場合の例外処理。
            catch(SocketTimeoutException ex) {
                Log.w(DEBUG_TAG, "通信タイムアウト", ex);
            }
            catch(IOException ex) {
                Log.e(DEBUG_TAG, "通信失敗", ex);
            }
            finally {
                // HttpURLConnectionオブジェクトがnullでないなら解放。
                if(con != null) {
                    con.disconnect();
                }
                // InputStreamオブジェクトがnullでないなら解放。
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch(IOException ex) {
                        Log.e(DEBUG_TAG, "InputStream解放失敗", ex);
                    }
                }
            }
           Timetable.TimeInfoPostExecustor postExcutor = new Timetable.TimeInfoPostExecustor(result);
            _hander.post(postExcutor);
        }
        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while(0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }
    }

    private  class TimeInfoPostExecustor implements Runnable{

        private  final  String _result;

        String Subject_id[];
        String Subject_name[];
        String Bring_id[];
        String Bring_name[];

        public  TimeInfoPostExecustor(String result){
            _result = result;
        }

        @UiThread
        @Override
        public void run(){
            //長さ取得
            Log.d("sample", "せいこう" + _result);

            /**json解析*/
            try {
                // JSONObject を インスタンス化して、
                // 引数に先程定義した Stringを入れるとJSONObject完成
                JSONObject jsonResult = new JSONObject(_result);

                String jtime = jsonResult.getString("preset");

                JSONObject json1 = new JSONObject(jtime);
                int count=1;
                for(int i=0;i<5;i++){
                    for (int j=0;j<6;j++){
                        String dtime = json1.getString(String.valueOf(count));
                        JSONObject jkoma = new JSONObject(dtime);
                        String subject = jkoma.getString("subject");
                        String color = jkoma.getString("color");

                        tv[j][i].setText(subject);
                        tv[j][i].setBackgroundColor(Color.parseColor(color));

                        Log.d("sample", "せいこう" + subject + color);

                        count++;


                    }
                }

                String Date = jsonResult.getString("date");
                Log.d("date", Date);

                //クラス出力
                String ClassID = jsonResult.getString("class");
                Log.d("classID", ClassID);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            /***/


        }

    }
    /**持ち物情報の入手*/


}
