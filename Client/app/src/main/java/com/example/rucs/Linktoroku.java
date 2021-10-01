package com.example.rucs;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Linktoroku extends AppCompatActivity {


    private Handler handler; // handler that gets info from Bluetooth service
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CODE_ENABLE_BT = 1;
    private byte[] mmBuffer;
    private Button mButton_Connect;    // 更新ボタン
    private Button mButton_Write;
    private Button mButton_upWrite;
    private Button mButton_up;
    private Button mButton_Back;
    private Spinner mSpinner_Bring;
    private Spinner mSpinner_Subject;
    private static String read_bag=null;
    private BringDB helper;
    private SQLiteDatabase db;
    private static String Bring_id_list[] ;
    private static String Subject_name_list[];
    private static String Bring_name_list[];
    private static String bSubject_name_list[];
    private int count_Bring=0;
    private int count_Subject=0;
    private String gBring_name;
    private String gSubject_name;
    private int count_Link=0;

    /** Bluetoothから受信した値. */
    private TextView mInputTextView;
    private static final String TAG = "BluetoothSample";
    /* Threadの状態を表す */
    private boolean isRunning;

    private BluetoothAdapter mBTAdapter = null;
    private BluetoothDevice mBTDevice = null;
    private BluetoothSocket mBTSocket = null;
    private OutputStream mOutputStream = null;//出力ストリーム
    private InputStream mmInStream = null;

    private boolean in =false;

    private String MY_UUID = "ef7ce24a-a1eb-45d4-9208-f896b0ae8336";

    /**↓pcのbluetoothアダプタのMacアドレス*/

    private String MacAddress = "94:E6:F7:65:5E:F4";

    /**↑pcのbluetoothアダプタのMacアドレス*/

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linktoroku);

        TextView touroku = findViewById(R.id.textView3);

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


        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.d(TAG, "No bluetooth support.");
            return;
        }
        // GUIアイテム
        mButton_Connect = (Button) findViewById(R.id.button_connect);
        mButton_Write = (Button) findViewById(R.id.button_write);
        mButton_Back =  (Button)findViewById(R.id.button_back);
        mButton_up =  (Button)findViewById(R.id.button_write_uo);


        mButton_Write.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                write();            // 読み込み
                return;
            }
        });


        mButton_up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                uwrite();            // 読み込み
                return;
            }
        });

        mButton_Back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

        mSpinner_Subject = findViewById(R.id.sniper_grade);
        mSpinner_Subject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                gSubject_name =item;
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
        mSpinner_Bring = findViewById(R.id.sniper_class);
        mSpinner_Bring.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                gBring_name =item;
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });



        ArrayAdapter<String> adapter_Subject = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter_Subject.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_Subject.add("---");
        ArrayAdapter<String> adapter_Bring = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter_Bring.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_Bring.add("---");

        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean t_mode = mode.getBoolean("_mode",true);
        if(t_mode){
            mButton_Connect.setText("こうしん");
            mButton_Back.setText("もどる");
            mButton_Write.setText("ほぞん");
            mButton_up.setText("うわがきほぞん");
            touroku.setText("IDとうろく");

        }


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


        /**データベースアクセス*/
        if (helper == null) {
            helper = new BringDB(getApplicationContext());
        }

        if (db == null) {
            db = helper.getReadableDatabase();
        }
        Log.d("debug", "**********Cursor");



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


        cursorBring.moveToFirst();

        Bring_id_list = new String[count_Bring];
        bSubject_name_list = new String[count_Bring];
        Bring_name_list = new String[count_Bring];

        for (int i = 0; i < count_Bring; i++) {
            Bring_id_list[i] = (cursorBring.getString(0));
            bSubject_name_list[i] = (cursorBring.getString(1));
            Bring_name_list[i] = (cursorBring.getString(2));
            cursorBring.moveToNext();
        }
        cursorBring.close();


        final Cursor cursorSubject = db.query(
                "subject_table",
                new String[]{"subject_name"},
                null,
                null,
                null,
                null,
                null
        );

        count_Subject = cursorSubject.getCount();

        cursorSubject.moveToFirst();

        Subject_name_list = new String[count_Subject];
        Log.d("debug", "**********Cursor1");

        for (int i = 0; i < count_Subject; i++) {
            Subject_name_list[i] = (cursorSubject.getString(0));
            cursorSubject.moveToNext();
        }
        cursorSubject.close();
        /**データベースアクセス*/
        Log.d("sample",":"+cursorSubject);
        Log.d("debug", "**********Cursor");

        /**spinnerのセット*/
        Set<String>  linkedHashSet = new LinkedHashSet<String>();
        for (int i = 0; i < count_Bring; i++) {
            linkedHashSet.add(Bring_name_list[i]);
        }

        Object[] strings_after = linkedHashSet.toArray();

        for(int i= 0; i<strings_after.length;i++){
            adapter_Bring.add(strings_after[i].toString());
        }

        for (int i = 0; i < count_Subject; i++) {
            adapter_Subject.add(Subject_name_list[i]);
        }
        mSpinner_Subject.setAdapter(adapter_Subject);
        mSpinner_Bring.setAdapter(adapter_Bring);

        mButton_Connect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                overridePendingTransition(0, 0);
                recreate();
                overridePendingTransition(0, 0);
            }
        });
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
                in =true;
            }else {
                in =false;
                Log.d("debug", "**********Cursor");
            }
        } catch (IOException e) {
            try{
                mBTSocket.close();
            }catch(IOException e1){/*ignore*/}
        }
    }

    private void ReConnect(){
        try {
            mBTSocket.close();
            mBTSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message vMsg = new Message();
        vMsg.obj = "connecting...";
        mHandler.sendMessage(vMsg);

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
                vMsg.obj = readMsg;
                mHandler.sendMessage(vMsg);
                in =true;
            }else {
                in =false;
                Log.d("debug", "**********Cursor");
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
                mBTSocket.close();
            } catch (IOException connectException) {/*ignore*/}
            mBTSocket = null;
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int action = msg.what;
            read_bag = (String)msg.obj;
            TextView vid = findViewById(R.id.vlink_id_in);
            vid.setText(read_bag);
            TextView tv = findViewById(R.id.textView4);
            if(read_bag.length()==24){
                tv.setVisibility(View.GONE);
            }


        }
    };




    // 書き込み
    private void write() {
        String cread_bag = read_bag;

        TextView textView = findViewById(R.id.touroku_can);
        boolean toroku = false;
        boolean torokued_li = false;
        boolean torokued_br = false;
        int toroku_no=0;
        in =true;


        if (cread_bag==null) {   //  read_bagがnullなら
            textView.setText("バッグの中身がありません");
            return;
        }

        /**バッグの中身の数を参照*/
        if(cread_bag.length()>24){
            textView.setText("複数のタグが検出されています");
            Log.d("debug", "**********php"+cread_bag.length());
            return;
        }
        if(cread_bag.length()<24){
            textView.setText("タグが検出されてません");
            Log.d("debug", "**********php"+cread_bag.length());
            return;
        }

        for (int i = 0; i < count_Bring; i++) {
            if(bSubject_name_list[i].equals(gSubject_name)){
                if(Bring_name_list[i].equals(gBring_name)){
                    toroku=true;
                    toroku_no=i;
                }
            }
        }

        /**TEST*/
        if(helper == null){
            helper = new BringDB(getApplicationContext());
        }

        if(db == null){
            db = helper.getReadableDatabase();
        }
        Log.d("debug","**********Cursor");


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

        for(int i=0;i<count_Link;i++){
            if(cread_bag.equals(link_id[i])){
                torokued_li =true;
            }
        }

        for(int i=0;i<count_Link;i++){
            if(Bring_id_list[toroku_no].equals(bring_id[i])){
                torokued_br = true;
            }
        }
        /**TEST*/

        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean t_mode = mode.getBoolean("_mode",true);

        if(t_mode){
            if(torokued_li){
                textView.setText("そのIDはすでにとうろくされています");
            }else{
                if(torokued_br){
                    textView.setText("そのもちものはべつのIDでほぞんされています");
                }else{
                    if(toroku){
                        ContentValues values = new ContentValues();
                        values.put("link_id", cread_bag);
                        values.put("bring_id", Bring_id_list[toroku_no]);
                        db.insert("link_table", null, values);
                        textView.setText("ほぞんしました");
                        Log.d("debug", "**********php");
                    }else {
                        textView.setText("そのアイテムはひつようありません");
                    }
                }

            }
        }else {
            if(torokued_li){
                textView.setText("そのIDは既に登録されています");
            }else{
                if(torokued_br){
                    textView.setText("その持ち物は別のIDで保存されています");
                }else{
                    if(toroku){
                        ContentValues values = new ContentValues();
                        values.put("link_id", cread_bag);
                        values.put("bring_id", Bring_id_list[toroku_no]);
                        db.insert("link_table", null, values);
                        textView.setText("保存しました");
                        Log.d("debug", "**********php");
                    }else {
                        textView.setText("そのアイテムは必要ありません");
                    }
                }

            }
        }


    }

    // 書き込み
    private void uwrite() {
        String cread_bag = read_bag;

        TextView textView = findViewById(R.id.touroku_can);
        boolean toroku = false;
        boolean torokued_li = false;
        int toroku_no=0;
        in =true;


        if (cread_bag==null) {   //  read_bagがnullなら
            textView.setText("バッグの中身がありません");
            return;
        }

        /**バッグの中身の数を参照*/
        if(cread_bag.length()>24){
            textView.setText("複数のタグが検出されています");
            Log.d("debug", "**********php"+cread_bag.length());
            return;
        }
        if(cread_bag.length()<24){
            textView.setText("タグが検出されてません");
            Log.d("debug", "**********php"+cread_bag.length());
            return;
        }

        for (int i = 0; i < count_Bring; i++) {
            if(bSubject_name_list[i].equals(gSubject_name)){
                if(Bring_name_list[i].equals(gBring_name)){
                    toroku=true;
                    toroku_no=i;
                }
            }
        }

        if(helper == null){
            helper = new BringDB(getApplicationContext());
        }

        if(db == null){
            db = helper.getReadableDatabase();
        }
        Log.d("debug","**********Cursor");


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

        for(int i=0;i<count_Link;i++){
            if(cread_bag.equals(link_id[i])){
                torokued_li =true;
            }
        }

        boolean torokued_br = false;
        for(int i=0;i<count_Link;i++){
            if(Bring_id_list[toroku_no].equals(bring_id[i])){
                torokued_br = true;
            }
        }

        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean t_mode = mode.getBoolean("_mode",true);
        if(t_mode){

            if(toroku){
                if(!torokued_li){
                    if(torokued_br){
                        db.delete("link_table", "bring_id = " + Bring_id_list[toroku_no], null);
                        ContentValues values = new ContentValues();
                        values.put("link_id", cread_bag);
                        values.put("bring_id", Bring_id_list[toroku_no]);
                        db.insert("link_table", null, values);
                        textView.setText("うわがきほぞんしました");
                        Log.d("debug", "**********php");
                    }else {
                        textView.setText("このIDはすでに登録されています");
                    }
                }else{
                    textView.setText("そのアイテムはとうろくされてません");
                }
            }else {
                textView.setText("そのアイテムはひつようありません");
            }

        }else {
            if(toroku){
                if(!torokued_li){
                    if(torokued_br){
                        db.delete("link_table", "bring_id = " + Bring_id_list[toroku_no], null);
                        ContentValues values = new ContentValues();
                        values.put("link_id", cread_bag);
                        values.put("bring_id", Bring_id_list[toroku_no]);
                        db.insert("link_table", null, values);
                        textView.setText("上書き保存保存しました");
                        Log.d("debug", "**********php");
                    }else {
                        textView.setText("このIDは既に登録されています");
                    }
                }else{
                    if(torokued_br){
                        textView.setText("そのアイテムはこのIDで登録されています");
                    }else {
                        textView.setText("そのアイテムは登録されてません");
                    }
                }
            }else {
                textView.setText("そのアイテムは必要ありません");
            }
        }







    }
}

