
package com.example.rucs;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckBag extends AppCompatActivity {


    private Handler handler; // handler that gets info from Bluetooth service
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CODE_ENABLE_BT = 1;
    private byte[] mmBuffer;
    BluetoothAdapter bluetoothAdapter;
    private Button mButton_Connect;    // 接続ボタン
    private Button mButton_Disconnect;    // 切断ボタン
    private Button mButton_Write;
    private Button mButton_Back;
    private Button mButton_plas;
    private Button mButton_minus;
    private static String read_bag="";
    private static final String DEBUG_TAG = "AsyncSample";
    private static final String BRING_URL = "https://fulab.tk/rucs/api/todayBring.php?classID=";
    private BringDB helper;
    private SQLiteDatabase db;
    private int count_Link=0;
    private int count_Subject;
    private int page =0;
    private String pDateClassID;
    private int count__link;

    /** Bluetoothから受信した値. */
    private TextView mInputTextView;
    private static final String TAG = "BluetoothSample";
    /* Threadの状態を表す */
    private boolean isRunning;


    private BluetoothAdapter mBTAdapter = null;
    private BluetoothDevice mBTDevice = null;
    private BluetoothSocket mBTSocket = null;
    private OutputStream mOutputStream = null;//出力ストリーム

    private Button btnSend;//送信用ボタン
    private Button btnFinish;//終了用ボタン
    private TextView textview;//MacAddress表示用

    /**↓pcのbluetoothアダプタのMacアドレス*/

    private String MacAddress = "94:E6:F7:65:5E:F4";

    /**↑pcのbluetoothアダプタのMacアドレス*/


    private String MY_UUID = "ef7ce24a-a1eb-45d4-9208-f896b0ae8336";
    private InputStream mmInStream = null;

    private static final String AllID = "_text";


    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkbag);


        //情報取得
        SharedPreferences preGrade = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pDateClassID = preGrade.getString(AllID,"学年が指定されていません");

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Device doesn't support Bluetooth
        if (bluetoothAdapter == null) Log.d("error", "Device doesn't support Bluetooth");
        else Log.d("success","this device can use Bluetooth");

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.d(TAG, "Bluetooth is disabled.");
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("device name",deviceName);
                Log.d("mac address",deviceHardwareAddress);
            }
        }

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
//
        final String SERVER_DEVICE_ADDRESS = "00:09:DD:40:DA:C3";
        final String SERVER_SERVICE_UUID = "b43fb147-c64e-4aae-b68c-c3b6640cba45";

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.d(TAG, "No bluetooth support.");
            return;
        }

        // GUIアイテム
        mButton_Connect = (Button)findViewById( R.id.button_connect );
        mButton_Write =  (Button)findViewById( R.id.button_write);
        mButton_Back =  (Button)findViewById(R.id.button_back);
//        mButton_plas.setEnabled(false);
//        mButton_minus.setEnabled(false);

        mButton_Connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mButton_Connect.setEnabled( false );    // 接続ボタンの無効化（連打対策）
                mButton_Write.setEnabled(true);
                Send();
                return;
            }
        });



        mButton_Write.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
//                mButton_plas.setEnabled(true);
//                mButton_minus.setEnabled(false);
                write();            // 読み込み
                return;
            }
        });

        mButton_Back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });


//        mButton_plas.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
//                page=page+15;
//                if(page==30){
//                    mButton_plas.setEnabled(false);
//                    mButton_minus.setEnabled(true);
//                }else{
//                    mButton_plas.setEnabled(true);
//                    mButton_minus.setEnabled(true);
//                }
//                write();
//                return;
//            }
//        });
//        mButton_minus.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v){
//                page=page-15;
//                if(page==0){
//                    mButton_plas.setEnabled(true);
//                    mButton_minus.setEnabled(false);
//                }else{
//                    mButton_plas.setEnabled(true);
//                    mButton_minus.setEnabled(true);
//                }
//                write();            // 読み込み
//                return;
//            }
//        });

        //ソケットを確立する関数
        BTConnect();

        //ソケットが取得出来たら、出力用ストリームを作成する
        if(mBTSocket != null){
            try{
                mOutputStream = mBTSocket.getOutputStream();
            }catch(IOException e){/*ignore*/}
        }else{

        }


        Send();
        
    }

    private void BTConnect(){
        Message valueMsg = new Message();
        valueMsg.obj = "connecting...";
        mHandler.sendMessage(valueMsg);

        //BTアダプタのインスタンスを取得
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        //相手先BTデバイスのインスタンスを取得
        mBTDevice = mBTAdapter.getRemoteDevice(MacAddress);
        //ソケットの設定
        try {
            mBTSocket = mBTDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            mBTSocket = null;
        }

        if(mBTSocket != null) {
            //接続開始
            mBTAdapter.cancelDiscovery();
            try {
                mBTSocket.connect();
            } catch (IOException connectException) {
                isRunning = false;
                try {
                    mBTSocket.close();
                    mBTSocket = null;
                } catch (IOException closeException) {
                    return;
                }
            }
        }
    }

    private void Send(){
        //文字列を送信する
        isRunning = true;
        byte[] bytes_ = {};
        String str = "test send from android";
        bytes_ = str.getBytes();
        try {
            //ここで送信
            mOutputStream.write(bytes_);

            mmInStream = mBTSocket.getInputStream();
            // InputStreamのバッファを格納
            byte[] buffer = new byte[1024];
            // 取得したバッファのサイズを格納
            int bytes;
            // InputStreamの読み込み
            bytes = mmInStream.read(buffer);
            Log.i(TAG,"bytes="+bytes);
            // String型に変換
            String readMsg = new String(buffer, 0, bytes);
            // null以外なら表示
            if(readMsg.trim() != null && !readMsg.trim().equals("")){
                //valueMsgにレスポンスが詰まってます。
                Log.i(TAG,"value="+readMsg.trim());
                Message valueMsg = new Message();
                valueMsg.obj = readMsg;
                mHandler.sendMessage(valueMsg);
            }
        } catch (IOException e) {
            try{
                mBTSocket.close();
            }catch(IOException e1){/*ignore*/}
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mBTSocket != null){
            try {
                mBTSocket.connect();
            } catch (IOException connectException) {/*ignore*/}
            mBTSocket = null;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            read_bag = (String)msg.obj;
        }
    };


    // 書き込み
    private void write()
    {

        if( read_bag == null)
        {   //  read_bagがnullなら
            return;
        }

        /**バッグの中身の数を参照*/
        String cread_bag = read_bag;

        int totalCharacters = 1;
        char temp;

        for (int i = 0; i < cread_bag.length(); i++) {

            temp = cread_bag.charAt(i);
            if (temp == ',') {
                totalCharacters++;
            }
        }

        count__link=totalCharacters;
        String Link_list[] = cread_bag.split(",");
        String BRING_ALL_URL=BRING_URL+pDateClassID;
        receiveBringInfo(BRING_ALL_URL,Link_list,page);

    }

    @UiThread
    private  void  receiveBringInfo(final String BRING_URL,String[] Link_list,int page){
        Looper mainLooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainLooper);
        BringInfoInfoBackgroundReceiver backgroundReceiver = new BringInfoInfoBackgroundReceiver(handler,BRING_URL,Link_list,page);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(backgroundReceiver);
    }

    private  class  BringInfoInfoBackgroundReceiver implements Runnable {

        private final Handler _hander;
        private final String _bring_url;
        private final String _link_list[];
        private int _page;



        private BringInfoInfoBackgroundReceiver(Handler handler, String BRING_URL,String[] Link_list,int page) {
            _hander =handler;
            _bring_url = BRING_URL;
            _link_list = Link_list;
            _page = page;
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
            BringInfoPostExecustor postExcutor = new BringInfoPostExecustor(result,_link_list,_page);
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
        private final String _link_list[];
        private int _page;

        private String bring_id_list[];
        private String bring_name_list[];
        private String link_id_list[];
        private String subject_name_list[];
        private String subject_color_list[];
        private String link_have_id[];
        private TextView vlist[] = new TextView[20];
        private TextView vlist_in[] = new TextView[20];

        public  BringInfoPostExecustor(String result,String[] Link_list,int page){
            _link_list = Link_list;
            _result = result;
            _page = page;
        }

        @UiThread
        @Override
        public void run(){
            Log.d("debug","**********     "+_link_list.length);



            vlist[0] = findViewById(R.id.checkbag_0);vlist_in[0] = findViewById(R.id.checkbag_in_0);
            vlist[1] = findViewById(R.id.checkbag_1);vlist_in[1] = findViewById(R.id.checkbag_in_1);
            vlist[2] = findViewById(R.id.checkbag_2);vlist_in[2] = findViewById(R.id.checkbag_in_2);
            vlist[3] = findViewById(R.id.checkbag_3);vlist_in[3] = findViewById(R.id.checkbag_in_3);
            vlist[4] = findViewById(R.id.checkbag_4);vlist_in[4] = findViewById(R.id.checkbag_in_4);
            vlist[5] = findViewById(R.id.checkbag_5);vlist_in[5] = findViewById(R.id.checkbag_in_5);
            vlist[6] = findViewById(R.id.checkbag_6);vlist_in[6] = findViewById(R.id.checkbag_in_6);
            vlist[7] = findViewById(R.id.checkbag_7);vlist_in[7] = findViewById(R.id.checkbag_in_7);
            vlist[8] = findViewById(R.id.checkbag_8);vlist_in[8] = findViewById(R.id.checkbag_in_8);
            vlist[9] = findViewById(R.id.checkbag_9);vlist_in[9] = findViewById(R.id.checkbag_in_9);
            vlist[10] = findViewById(R.id.checkbag_10);vlist_in[10] = findViewById(R.id.checkbag_in_10);
            vlist[11] = findViewById(R.id.checkbag_11);vlist_in[11] = findViewById(R.id.checkbag_in_11);
            vlist[12] = findViewById(R.id.checkbag_12);vlist_in[12] = findViewById(R.id.checkbag_in_12);
            vlist[13] = findViewById(R.id.checkbag_13);vlist_in[13] = findViewById(R.id.checkbag_in_13);
            vlist[14] = findViewById(R.id.checkbag_14);vlist_in[14] = findViewById(R.id.checkbag_in_14);
            vlist[15] = findViewById(R.id.checkbag_15);vlist_in[15] = findViewById(R.id.checkbag_in_15);
            vlist[16] = findViewById(R.id.checkbag_16);vlist_in[16] = findViewById(R.id.checkbag_in_16);
            vlist[17] = findViewById(R.id.checkbag_17);vlist_in[17] = findViewById(R.id.checkbag_in_17);
            vlist[18] = findViewById(R.id.checkbag_18);vlist_in[18] = findViewById(R.id.checkbag_in_18);
            vlist[19] = findViewById(R.id.checkbag_19);vlist_in[19] = findViewById(R.id.checkbag_in_19);

            bring_id_list = new String[20];
            bring_name_list = new String[20];
            link_id_list  = new String[20];
            link_have_id = new  String[20];
            subject_name_list = new String[20];
            subject_color_list = new String[20];

            int count =0;
            int count_bag=0;
            int count_Bring=0;

            /**json解析*/
            try {
                // JSONObject を インスタンス化して、
                // 引数に先程定義した Stringを入れるとJSONObject完成
                JSONObject jsonResult = new JSONObject(_result);

                //長さ取得
                Log.d("sample", "長さ" + jsonResult.length());
                count_bag=jsonResult.length()-2;

                for (int i = 1; i <= (jsonResult.length() - 2); i++) {

                    String singleKey = jsonResult.getString(String.valueOf(i));
                    JSONObject json1 = new JSONObject(singleKey);
                    //Key名を入力
                    String bname = json1.getString("bring");
                    String sname = json1.getString("title");


                    String jBring = bname.replace("[", "").replace("]", "");


                    JSONObject json_bring = new JSONObject(jBring);
                    // Log.d("sample", "長さ" + json_bring.length());
                    for (int j=1;j<=(json_bring.length());j++){
                        String bring_name = json_bring.getString(String.valueOf(j));
                        //Log.d("sample", i + ":" + bring_name);
                        subject_name_list[count]=sname;
                        bring_name_list[count]=bring_name;
                        count++;
                    }
                }
                //日付出力
                String Date = jsonResult.getString("date");
                Log.d("date", Date);

                //クラス出力
                String ClassID = jsonResult.getString("class");
                Log.d("classID", ClassID);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            /***/



            for(int i=0;i<20;i++){
                bring_id_list[i]="";
            }

            if(helper == null){
                helper = new BringDB(getApplicationContext());
            }

            if(db == null){
                db = helper.getReadableDatabase();
            }
            Log.d("debug","**********Cursor");

            /**データベースアクセス*/
            final Cursor cursorLink = db.query(
                    "link_table",
                    new String[] { "link_id", "bring_id" },
                    null,
                    null,
                    null,
                    null,
                    null
            );

            count_Link=cursorLink.getCount();

            String link_id[] = new String[count_Link];
            String bring_id[] = new  String[count_Link];

            cursorLink.moveToFirst();

            for (int i = 0; i <cursorLink.getCount(); i++) {
                link_id[i]=(cursorLink.getString(0));
                bring_id[i]=(cursorLink.getString(1));
                cursorLink.moveToNext();
            }

            cursorLink.close();

            final Cursor cursorSubject = db.query(
                    "subject_table",
                    new String[] { "subject_id", "subject_name","subject_color" },
                    null,
                    null,
                    null,
                    null,
                    null
            );

            count_Subject=cursorSubject.getCount();

            String sSubject_id[] = new String[count_Subject];
            String sSubject_name[] = new  String[count_Subject];
            String sSubject_color_id[] = new String[count_Subject];


            cursorSubject.moveToFirst();

            for(int i=0;i<count_Subject;i++){
                sSubject_id[i]=(cursorSubject.getString(0));
                sSubject_name[i]=(cursorSubject.getString(1));
                sSubject_color_id[i]=(cursorSubject.getString(2));
                cursorSubject.moveToNext();
            }
            cursorSubject.close();

            final Cursor cursorBring = db.query(
                    "bring_table",
                    new String[]{"bring_id", "subject_name", "bring_name"},
                    null,
                    null,
                    null,
                    null,
                    null
            );

            count_Bring = cursorBring.getCount();
            Log.d("bring", "test:   "+cursorBring.getCount());



            cursorBring.moveToFirst();

            String bBring_id[] = new String[count_Bring];
            String bSubject_name[] = new String[count_Bring];
            String bBring_name[] = new String[count_Bring];

            for (int i = 0; i < cursorBring.getCount(); i++) {
                bBring_id[i] = (cursorBring.getString(0));
                bSubject_name[i] = (cursorBring.getString(1));
                bBring_name[i] = (cursorBring.getString(2));
                Log.d("name", "test:   "+bBring_name[i]);
                Log.d("name", "test:   "+bSubject_name[i]);
                cursorBring.moveToNext();
            }
            cursorBring.close();
            /**データベースアクセス*/


            /**bring_idの入手*/
            for(int c=0;c<count;c++){
                for(int i=0;i<cursorBring.getCount();i++){
                    if(bring_name_list[c].equals(bBring_name[i])){
                        if(subject_name_list[c].equals(bSubject_name[i])){
                            bring_id_list[c]=bBring_id[i];
                        }
                    }
                }
            }
            /**bring_idの入手*/

            /**bring_idに対応したlink_idの獲得*/
            for(int c=0; c<count;c++){
                int ch=0;
                for (int i=0 ; i<cursorLink.getCount(); i++){
                    if(bring_id_list[c].equals(bring_id[i])){
                        link_id_list[c]=link_id[i];
                        ch=1;
                    }
                }
                if (ch==0){
                    link_id_list[c]="not_id";
                }
            }
            /**bring_idに対応したlink_idの獲得*/


            /**link_idが登録されてるかの確認*/
            for(int c=0; c<count;c++) {
                link_have_id[c]="not_have";
                for (int i = 0; i < _link_list.length; i++) {
                    if (link_id_list[c].equals(_link_list[i])) {
                        link_have_id[c]="have";
                        Log.d("debug","**********Cursorggg");
                    }
                    Log.d("debug","  "+ link_have_id[c] + " "+_link_list[i]);
                }
            }
            /**link_idが登録されてるかの確認*/


            /**Subject_colorの入手*/
            for(int c=0; c<count;c++){
                for (int i=0 ; i<cursorSubject.getCount(); i++){
                    if(subject_name_list[c].equals(sSubject_name[i])){
                        subject_color_list[c]=sSubject_color_id[i];
                    }
                }
            }
            /**Subject_colorの入手*/



            /**画面出力*/
            for(int i=0;i<20;i++){
                if(bring_id_list[i+_page].equals("")){
                    vlist[i].setText("");
                    vlist[i].setBackgroundColor(Color.WHITE);
                    vlist_in[i].setText("　　　");
                    vlist_in[i].setBackgroundColor(Color.WHITE);
                }else {
                    vlist[i].setText(subject_name_list[i+page]+" "+bring_name_list[i+_page]);
                    vlist[i].setBackgroundColor(Color.parseColor(subject_color_list[i+_page]));
                    if(link_have_id[i].equals("have")){
                        vlist_in[i].setText("　〇　");
                        vlist_in[i].setBackgroundColor(Color.parseColor(subject_color_list[i+_page]));
                    }else if(link_id_list[i].equals("not_id")){
                        vlist_in[i].setText("登録X");
                        vlist_in[i].setBackgroundColor(Color.parseColor("#a9a9a9"));
                    }else {
                        vlist_in[i].setText("バッグX");
                        vlist_in[i].setBackgroundColor(Color.parseColor("#a9a9a9"));
                    }
                }
            }
            /**画面出力*/


        }

    }
}