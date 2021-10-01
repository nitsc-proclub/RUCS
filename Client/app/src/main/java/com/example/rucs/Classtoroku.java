package com.example.rucs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

public class Classtoroku extends AppCompatActivity {

    private static final String AllID = "_text";
    private BringDB helper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touroku);

        int ClassGradeID=0;
        int ClassID=0;
        int GradeID=0;

        Button returnButton = findViewById(R.id.back);
        TextView textView =findViewById(R.id.textView);
        TextView textView2 =findViewById(R.id.textView2);
        TextView Grade = findViewById(R.id.Grade);

        //情報取得
        SharedPreferences mode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean t_mode = mode.getBoolean("_mode",true);
        Button saveButton = findViewById(R.id.save);


        if(helper == null){
            helper = new BringDB(getApplicationContext());
        }

        if(db == null){
            db = helper.getWritableDatabase();//データベースの取得
        }


        // ラジオグループのオブジェクトを取得
        RadioGroup rg_Grade = (RadioGroup)findViewById(R.id.radioGrade);
        // ラジオグループのオブジェクトを取得
        RadioGroup rg_Class = (RadioGroup)findViewById(R.id.radioclass);

        //情報取得
        SharedPreferences preGrade = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String pDateClassID = preGrade.getString(AllID,"学年が指定されていません");



        if(pDateClassID.equals("学年が指定されていません")){

        }else {
            ClassGradeID =  Integer.parseInt(pDateClassID);
            ClassID = ClassGradeID % 10;
            GradeID = ClassGradeID / 10;
        }

        switch(ClassID){
            case 2:
                rg_Class.check(R.id.cl2);
                break;
            case 3:
                rg_Class.check(R.id.cl3);
                break;
            case 4:
                rg_Class.check(R.id.cl4);
                break;
            case 5:
                rg_Class.check(R.id.cl5);
                break;
            case 6:
                rg_Class.check(R.id.cl6);
                break;
            default:
                rg_Class.check(R.id.cl1);
                break;
        }

        switch(GradeID){
            case 2:
                rg_Grade.check(R.id.gr2);
                break;
            case 3:
                rg_Grade.check(R.id.gr3);
                break;
            case 4:
                rg_Grade.check(R.id.gr4);
                break;
            case 5:
                rg_Grade.check(R.id.gr5);
                break;
            case 6:
                rg_Grade.check(R.id.gr6);
                break;
            default:
                rg_Grade.check(R.id.gr1);
                break;
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                returnButton.setEnabled(false);

                // チェックされているラジオボタンの ID を取得
                int id_Grade = rg_Grade.getCheckedRadioButtonId();
                Log.d("debug","**********Cursor"+id_Grade);

                // チェックされているラジオボタンオブジェクトを取得
                RadioButton radioButton_Grade = (RadioButton)findViewById(id_Grade);
                String text_Grade = radioButton_Grade.getText().toString();


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

                TextView vSave = findViewById(R.id.save_view);

                if (pDateClassID.equals(text)){
                    if(t_mode){
                        vSave.setText("げんざいとうろくされている学年とクラスです");
                    }else {
                        vSave.setText("現在登録されている学年とクラスです");
                    }
                }else {
                    // SharedPreferencesのEditorインスタンスに値を追加し、コミットする
                    db.delete("bring_table", null, null);
                    db.delete("subject_table", null, null);
                    db.delete("link_table", null, null);
                    preGrade.edit().putString(AllID, text).commit();
                    Log.d("debug", "**********finish");

                    if(t_mode){
                        vSave.setText("ほぞんされました");
                    }else {
                        vSave.setText("保存されました");
                    }
                }



                int Grade = Integer.parseInt(text_Grade);
                if(Grade<3){
                    mode.edit().putBoolean("_mode", true).commit();
                }else {
                    mode.edit().putBoolean("_mode", false).commit();
                }

                returnButton.setEnabled(true);

            }
        });


        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if(t_mode){
            returnButton.setText("もどる");
            saveButton.setText("ほぞん");
            Grade.setText("がくねん");
            textView.setText("あなたのがくねんとクラスをせんたくしてください");
            textView2.setText("⚠︎　へんこうするととうろくされたデータがリセットされます");
        }

    }
}
