package com.example.rucs;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CheckDB extends AppCompatActivity {

    private static Button  mButton_delete;
    private static Button  mButton_delete_all;
    private static Button  mButton_back;
    private static TextView vid;
    private static TextView vdelete;
    private static Spinner mSpinner;
    private BringDB helper;
    private SQLiteDatabase db;
    private int count_Link=0;
    private int count_Bring=0;
    private String delete_item;
    private String delete_id;
    private boolean t_mode;
    private static String[] bring_id_list;
    private static String[] bring_name_list;
    private static String[] subject_name_list;
    private static String[] link_id_list;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkdb);

        vdelete = findViewById(R.id.delete_can);
        vid = findViewById(R.id.vid);

        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        t_mode = mode.getBoolean("_mode",true);



        /**データベースアクセス*/
        if (helper == null) {
            helper = new BringDB(getApplicationContext());
        }

        if (db == null) {
            db = helper.getReadableDatabase();
        }
        Log.d("debug", "**********Cursor");

        mButton_delete =findViewById(R.id.button_delete);
        mButton_delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!delete_item.equals("---")){
                    String Link_list[] = delete_item.split(":");
                    int id = Integer.parseInt(Link_list[0])-1;
                    String delete_name = "link_id ="+link_id_list[id];
                    db.delete("link_table", "bring_id = " + delete_id, null);
                    if(t_mode){
                        vdelete.setText(Link_list[1]+"をさくじょしました");
                    }else{
                        vdelete.setText(Link_list[1]+"を削除しました");
                    }

                    set();
                }
            }
        });


        mButton_delete_all =findViewById(R.id.button_delete_all);
        mButton_delete_all.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                    db.delete("link_table", null, null);
                if(t_mode){
                    vdelete.setText("ぜんデータをさくじょしました");
                }else{
                    vdelete.setText("全データを削除しました");
                }

                    set();

            }
        });

        mButton_back = findViewById(R.id.button_back);
        mButton_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });


        mSpinner = findViewById(R.id.spinner_grade);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                delete_item = item;
                if(!delete_item.equals("---")){
                    String list[] = delete_item.split(":");
                    int ID = Integer.parseInt(list[0])-1;
                    delete_id = bring_id_list[ID];
                    vid.setText(link_id_list[ID]);
                }
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        set();

        t_mode = mode.getBoolean("_mode",true);
        if(t_mode){
            mButton_delete.setText("さくじょ");
            mButton_delete_all.setText("いっかつさくじょ");

        }

    }

    public void set(){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("---");

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

        link_id_list = new String[count_Link];
        bring_id_list = new  String[count_Link];
        bring_name_list = new  String[count_Link];
        subject_name_list  = new  String[count_Link];

        for(int i=0;i<count_Link;i++){
            link_id_list[i]=null;
            bring_id_list[i]=null;
            bring_name_list[i]= null;
            subject_name_list[i]=null;
        }

        cursorLink.moveToFirst();

        for (int i = 0; i <cursorLink.getCount(); i++) {
            link_id_list[i]=(cursorLink.getString(0));
            bring_id_list[i]=(cursorLink.getString(1));
            cursorLink.moveToNext();
        }

        cursorLink.close();

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

        String bring_id[] = new String[count_Bring];
        String subject_name[] = new String[count_Bring];
        String bring_name[] = new String[count_Bring];

        for (int i = 0; i < cursorBring.getCount(); i++) {
            bring_id[i] = (cursorBring.getString(0));
            subject_name[i] = (cursorBring.getString(1));
            bring_name[i] = (cursorBring.getString(2));
            cursorBring.moveToNext();
        }
        cursorBring.close();
        /**データベースアクセス*/

        /**bring_idの入手*/
        for(int c=0;c<count_Link;c++){
            for(int i=0;i<cursorBring.getCount();i++){
                if(bring_id_list[c].equals(bring_id[i])){
                    bring_name_list[c]=bring_name[i];
                    subject_name_list[c]=subject_name[i];
                }
            }
        }

        for (int i = 0; i < count_Link; i++) {
            adapter.add(i+1+":"+subject_name_list[i]+"/"+bring_name_list[i]);
        }
        mSpinner.setAdapter(adapter);
        vid.setText("");

        /**bring_idの入手*/
    }

}