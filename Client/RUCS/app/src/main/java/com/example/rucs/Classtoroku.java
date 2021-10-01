package com.example.rucs;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Classtoroku extends AppCompatActivity {

    private static final String AllID = "_text";
    private BringDB helper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touroku);

        if(helper == null){
            helper = new BringDB(getApplicationContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();//データベースの取得
        }

        Button saveButton = findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ラジオグループのオブジェクトを取得
                RadioGroup rg_Grade = (RadioGroup)findViewById(R.id.radioGrade);
                // チェックされているラジオボタンの ID を取得
                int id_Grade = rg_Grade.getCheckedRadioButtonId();
                // チェックされているラジオボタンオブジェクトを取得
                RadioButton radioButton_Grade = (RadioButton)findViewById(id_Grade);
                String text_Grade = radioButton_Grade.getText().toString();

                // ラジオグループのオブジェクトを取得
                RadioGroup rg_Class = (RadioGroup)findViewById(R.id.radioclass);
                // チェックされているラジオボタンの ID を取得
                int id_Class = rg_Class.getCheckedRadioButtonId();
                // チェックされているラジオボタンオブジェクトを取得
                RadioButton radioButton_Class = (RadioButton)findViewById(id_Class);
                String text_Class = radioButton_Class.getText().toString();

                String text = text_Grade + text_Class;

                // PreferenceManagerを介してアプリのデフォルトのSharedPreferencesインスタンスを取得する
                SharedPreferences preGrade = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //情報取得
                String pDateClassID = preGrade.getString(AllID,"学年が指定されていません");

                if (pDateClassID.equals(text)){
                    Log.d("debug","**********Cursor");
                }else {
                    // SharedPreferencesのEditorインスタンスに値を追加し、コミットする
                    db.delete("bring_table", null, null);
                    db.delete("subject_table", null, null);
                    db.delete("link_table", null, null);
                    preGrade.edit().putString(AllID, text).commit();
                }

                TextView vSave = findViewById(R.id.save_view);
                vSave.setText("保存されました");


            }
        });



        Button returnButton = findViewById(R.id.back);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
