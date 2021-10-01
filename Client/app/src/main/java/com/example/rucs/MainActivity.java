package com.example.rucs;

        import androidx.annotation.UiThread;
        import androidx.annotation.WorkerThread;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.os.HandlerCompat;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.database.sqlite.SQLiteDatabase;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Looper;
        import android.preference.PreferenceManager;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.ImageView;
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


public class MainActivity extends AppCompatActivity {
    private static final String AllID = "_text";

    private static final String DEBUG_TAG = "AsyncSample";

    private static final String BRING_URL = "https://fulab.tk/rucs/api/readBring.php?classID=";

    private static final String SUBJECT_URL = "https://fulab.tk/rucs/api/readSubject.php?classID=";

    private static final String NEWS_URL = "https://fulab.tk/rucs/api/news.php?classID=";


    private Button touroku;
    private Button bag;
    private boolean f_news=false;
    private boolean f_bring=false;
    private boolean f_subject=false;
    private static final String TAG = "BluetoothSample";
    private BringDB helper;
    private SQLiteDatabase db;
    private static final int REQUEST_ENABLE_BT = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(helper == null){
            helper = new BringDB(getApplicationContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();//データベースの取得
        }


        String Bring_All_url;
        String Subject_All_url;
        String News_All_url;

        //情報取得
        SharedPreferences preGrade = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String pDateClassID = preGrade.getString(AllID,"学年が指定されていません");

        int ClassGradeID=0;
        int ClassID=0;
        int GradeID=0;

        if(pDateClassID.equals("学年が指定されていません")){
            Intent intent = new Intent(getApplication(),Classtoroku.class);
            startActivity(intent);
        }else {
            ClassGradeID =  Integer.parseInt(pDateClassID);
            ClassID = ClassGradeID % 10;
            GradeID = ClassGradeID / 10;
        }

        TextView textView=findViewById(R.id.news);
        TextView osirase = findViewById(R.id.osirase);


        //画像取得
        ImageView imageView = findViewById(R.id.imagerogo);
        imageView.setImageResource(R.drawable.rogo);


        bag = findViewById(R.id.bag);
        bag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplication(),CheckBag.class);
                startActivity(intent);
            }
        });


        Button time = findViewById(R.id.time);
        time.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplication(),Timetable.class);
                startActivity(intent);
            }
        });


        Button other = findViewById(R.id.other);
        other.setText("学年登録　　"+GradeID+"年"+ClassID+"組");
        other.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplication(),Classtoroku.class);
                startActivity(intent);
            }
        });

        touroku =findViewById(R.id.touroku);
        touroku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(),Linktoroku.class);
                startActivity(intent);
            }
        });


        final ImageButton road = findViewById(R.id.imagerogo);
        road.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(0, 0);
                recreate();
                overridePendingTransition(0, 0);
            }
        });

        touroku.setEnabled(false);
        bag.setEnabled(false);


        Bring_All_url=BRING_URL+pDateClassID;
        Subject_All_url=SUBJECT_URL+pDateClassID;
        News_All_url=NEWS_URL+pDateClassID;



        receiveSubject(Subject_All_url);
        receiveNews(News_All_url);
        receiveBringInfo(Bring_All_url);

        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean t_mode = mode.getBoolean("_mode",true);
        if(t_mode){
            osirase.setText("おしらせ");
            bag.setText("なかみかくにん");
            time.setText("じかんわり");
            touroku.setText("IDとうろく");
            other.setText("がくねんとうろく\n"+GradeID+"ねん"+ClassID+"くみ");
        }else {
            osirase.setText("お知らせ");
            bag.setText("中身確認");
            time.setText("時間割");
            touroku.setText("ID登録");
        }





    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean t_mode = mode.getBoolean("_mode",true);
        if(t_mode){
            inflater.inflate(R.menu.menu_touroku_mode, menu);
        }else {
            inflater.inflate(R.menu.menu_touroku, menu);

        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        boolean returnVal = true;
        int itemID = item.getItemId();
        switch (itemID) {
            case R.id.class_toroku:
                Intent intent = new Intent(getApplication(), Classtoroku.class);
                startActivity(intent);
                break;

            case R.id.link_toroku:
                Intent intents = new Intent(getApplication(), Linktoroku.class);
                startActivity(intents);
                break;
            case  R.id.checkDB:
                Intent intentDB = new Intent(getApplication(), CheckDB.class);
                startActivity(intentDB);
            default:
                returnVal = super.onOptionsItemSelected(item);
        }
        return returnVal;
    }

    @Override
    public void onRestart(){
        super.onRestart();
        overridePendingTransition(0, 0);
        recreate();
        overridePendingTransition(0, 0);
    }




    /**持ち物情報の入手*/
    @UiThread
    private  void  receiveBringInfo(final String BRING_URL){
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainLooper);
        BringInfoInfoBackgroundReceiver backgroundReceiver = new BringInfoInfoBackgroundReceiver(handler,BRING_URL);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(backgroundReceiver);
    }

    private  class  BringInfoInfoBackgroundReceiver implements Runnable {

        private final Handler _hander;
        private final String _bring_url;

        private BringInfoInfoBackgroundReceiver(Handler handler, String BRING_URL) {
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
            BringInfoPostExecustor postExcutor = new BringInfoPostExecustor(result);
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

    private  class BringInfoPostExecustor implements Runnable{

        private  final  String _result;

        String Subject_id[];
        String Subject_name[];
        String Bring_id[];
        String Bring_name[];

        public  BringInfoPostExecustor(String result){
            _result = result;
        }

        @UiThread
        @Override
        public void run(){

            try {
                JSONObject jsonResult = new JSONObject(_result);

                //長さ取得
                Log.d("sample","長さ"+jsonResult.length());

                Subject_id = new String[jsonResult.length()];
                Subject_name = new String[jsonResult.length()];
                Bring_id = new String[jsonResult.length()];
                Bring_name  = new String[jsonResult.length()];


                Log.d("debug","**********Cursor");
                for (int i = 1; i <= jsonResult.length(); i++) {
                    String singleKey = jsonResult.getString(String.valueOf(i));
                    JSONObject json1 = new JSONObject(singleKey);
                    //Key名を入力
                    String classID = json1.getString("subjectName");
                    Log.d("sample",i+":"+classID);
                    Subject_id[i-1]=json1.getString("subjectID");
                    Subject_name[i-1]= json1.getString("subjectName");
                    Bring_id[i-1]=json1.getString("bringID");
                    Bring_name[i-1]=json1.getString("name");
                }

                //シングル出力用
                String singleKey = jsonResult.getString("1");

                JSONObject json1 = new JSONObject(singleKey);
                String classID = json1.getString("classID");
                Log.d("sample",classID);

                for(int i = 1; i <= jsonResult.length(); i++) {
                    ContentValues values = new ContentValues();
                    values.put("subject_id",Subject_id[i-1]);
                    values.put("bring_id",Bring_id[i-1]);
                    values.put("subject_name",Subject_name[i-1]);
                    values.put("bring_name",Bring_name[i-1]);


                    Log.d("sample",i+":"+classID);

                    db.insert("bring_table",null,values);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            touroku.setEnabled(true);
            bag.setEnabled(true);
        }

    }
    /**持ち物情報の入手*/


    /**教科情報の入手*/
    @UiThread
    private  void  receiveSubject(final String SUBJECT_URL){
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainLooper);
        Subject_BackgroundReceiver backgroundReceiver = new Subject_BackgroundReceiver(handler,SUBJECT_URL);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(backgroundReceiver);
    }

    private  class  Subject_BackgroundReceiver implements Runnable {

        private final Handler _hander;
        private final String _subject_url;

        private Subject_BackgroundReceiver(Handler handler, String SUBJECT_URL) {
            _hander =handler;
            _subject_url = SUBJECT_URL;
        }

        @WorkerThread
        @Override
        public void run(){

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";

            try {
                URL url = new URL(_subject_url);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);
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
            Subject_PostExecustor postExcutor = new Subject_PostExecustor(result);
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

    private  class Subject_PostExecustor implements Runnable{

        private  final  String _result;

        String Class_id[];
        String Subject_id[];
        String Subject_name[];
        String Subject_color[];

        public  Subject_PostExecustor(String result){
            _result = result;
        }

        @UiThread
        @Override
        public void run(){


            //以下はreadSubject.phpのバージョン
            try {
                Log.d("debug","**********Csss");
                // JSONObject を インスタンス化して、
                // 引数に先程定義した Stringを入れるとJSONObject完成
                JSONObject jsonResult = new JSONObject(_result);

                Class_id = new String[jsonResult.length()];
                Subject_id = new String[jsonResult.length()];
                Subject_name = new String[jsonResult.length()];
                Subject_color = new String[jsonResult.length()];


                //長さ取得
                Log.d("sample","長さ"+jsonResult.length());

                for (int i = 1; i <= jsonResult.length(); i++) {
                    String singleKey = jsonResult.getString(String.valueOf(i));
                    JSONObject json1 = new JSONObject(singleKey);
                    //Key名を入力
                    String classID = json1.getString("subjectColor");
                    Log.d("sample",i+":"+classID);

                    Class_id[i-1]=json1.getString("classID");
                    Subject_id[i-1]=json1.getString("subjectID");
                    Subject_name[i-1]= json1.getString("name");
                    Subject_color[i-1]= json1.getString("subjectColor");
                }


                for(int i = 1; i <= jsonResult.length(); i++) {
                    ContentValues values = new ContentValues();
                    values.put("class_id",Class_id[i-1]);
                    values.put("subject_id",Subject_id[i-1]);
                    values.put("subject_name",Subject_name[i-1]);
                    values.put("subject_color",Subject_color[i-1]);

                    db.insert("subject_table",null,values);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            f_subject=true;


        }

    }
    /**教科情報の入手*/

    /**お知らせ情報の入手*/
    @UiThread
    private  void  receiveNews(final String NEWS_URL){
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainLooper);
        News_BackgroundReceiver backgroundReceiver = new News_BackgroundReceiver(handler,NEWS_URL);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(backgroundReceiver);
    }

    private  class  News_BackgroundReceiver implements Runnable {

        private final Handler _hander;
        private final String _news_url;

        private News_BackgroundReceiver(Handler handler, String NEWS_URL) {
            _hander =handler;
            _news_url = NEWS_URL;
        }

        @WorkerThread
        @Override
        public void run(){

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";

            try {
                URL url = new URL(_news_url);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(1000);
                con.setReadTimeout(1000);
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
            News_PostExecustor postExcutor = new News_PostExecustor(result);
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

    private  class News_PostExecustor implements Runnable{

        private  final  String _result;

        public  News_PostExecustor(String result){
            _result = result;
        }

        @UiThread
        @Override
        public void run(){
            TextView textView = findViewById(R.id.news);

            //以下はreadSubject.phpのバージョン
            try {
                Log.d("debug","**********Csss");
                // JSONObject を インスタンス化して、
                // 引数に先程定義した Stringを入れるとJSONObject完成
                JSONObject jsonResult = new JSONObject(_result);

                //長さ取得
                Log.d("sample","長さ"+jsonResult.length());

                String news = jsonResult.getString("message");
                textView.setText(news);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            f_news=true;
        }
    }
    /**お知らせ情報の入手*/
}

