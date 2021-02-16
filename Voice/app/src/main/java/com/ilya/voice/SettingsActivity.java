package com.ilya.voice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    SeekBar seekBar_text_size, seekBar_store_days;
    Button button_add_keywords, button_save_journal, button_show_guide;
    RadioButton radioButton_male, radioButton_female;
    Switch switch_vibro_at_load_sound, switch_vibro_after_pause, switch_keywords;
    TextView tv1, tv2, tv3, tv6, tv7, tv_credits;
    ListView listView_keywords;
    SharedPreferences sharedPreferences;
    public static final String
            SETTINGS_TEXT_SIZE = "text_size",
            SETTINGS_VIBRO_AT_LOAD_SOUND = "vibro_at_load_sound",
            SETTINGS_VIBRO_AFTER_PAUSE = "vibro_after_pause",
            SETTINGS_GENDER = "gender",
            SETTINGS_KEYWORDS = "keywords",
            SETTINGS_STORE_DAYS = "store_days",
            SETTINGS_VOICING_EMOTICONS = "sound_signs",
            SETTINGS_LANGUAGE = "language",
            SETTINGS_SHOW_GUIDE = "show_guide",
            PATH_TO_SETTINGS = "settings";
    public int TEXT_SIZE = 10;
    SQLWords sqlWords;
    SQLJournal sqlJournal;
    public String[] columns_words = {"_ID",
            SQLWords.COLUMN_WHAT_IS_IT,
            SQLWords.COLUMN_WORD,
            SQLWords.COLUMN_RATING};
    public String[] columns_journal = {"_ID",
            SQLJournal.COLUMN_WHAT_IS_IT,
            SQLJournal.COLUMN_DATE,
            SQLJournal.COLUMN_CONTENT};
    SQLiteDatabase db;
    Cursor cursor;
    DialogFragment dialogEditWord;
    public static final String KEY_FOR_DIALOG = "key_for_dialog",
            INFO_FOR_DIALOG = "info_for_dialog";
    public static final int ADD_WORD = 0, EDIT_WORD = 1, ADD_PHRASE = 2, EDIT_PHRASE = 3;
    public static boolean FULL_KEYWORDS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PATH_TO_SETTINGS, MODE_PRIVATE);
        sqlWords = new SQLWords(this, SQLWords.NAME_TABLE, null,
                SQLWords.VERSION_TABLE);

        seekBar_text_size = (SeekBar)findViewById(R.id.sb_text_size_settings);
        seekBar_store_days = (SeekBar)findViewById(R.id.sb_store_days_settings);
        switch_vibro_at_load_sound = (Switch)findViewById(R.id.sw_vibro_at_loud_sounds_settings);
        switch_vibro_after_pause = (Switch)findViewById(R.id.sw_vibro_after_pause_settings);
        switch_keywords = (Switch)findViewById(R.id.sw_keywords_settings);
        button_add_keywords = (Button)findViewById(R.id.btn_add_keyword_settings);
        button_save_journal = (Button)findViewById(R.id.btn_save_journal);
        button_show_guide = (Button)findViewById(R.id.btn_show_guide);
        radioButton_male = (RadioButton)findViewById(R.id.rb_male_settings);
        radioButton_female = (RadioButton)findViewById(R.id.rb_female_settings);
        listView_keywords = (ListView)findViewById(R.id.lv_keywords);
        tv1 = (TextView)findViewById(R.id.tv1_settings);
        tv2 = (TextView)findViewById(R.id.tv2_settings);
        tv3 = (TextView)findViewById(R.id.tv3_settings);
        tv6 = (TextView)findViewById(R.id.tv6_settings);
        tv7 = (TextView)findViewById(R.id.tv7_settings);
        tv_credits = (TextView)findViewById(R.id.tv_credits);
        tv_credits.setText(getString(R.string.credits));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_add_keyword_settings:
                        if(FULL_KEYWORDS){
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.keyword_limit),Toast.LENGTH_SHORT).show();
                        }else{
                            try{
                                //вызов диалога для создания нового слова
                                Bundle argument = new Bundle();
                                argument.putInt(KEY_FOR_DIALOG, ADD_WORD);
                                argument.putString(INFO_FOR_DIALOG, "NO");
                                dialogEditWord = new DialogEditWord();
                                FragmentManager manager = getSupportFragmentManager();
                                dialogEditWord.setArguments(argument);
                                dialogEditWord.show(manager, "addWord");
                            }catch (Exception e){}
                        }
                        break;
                    case R.id.btn_save_journal:
                        //записываю журнал
                        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("", getJournal());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.saved_clipboard), Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.btn_show_guide:
                        showGuide();
                        break;
                    case R.id.rb_male_settings:

                        break;
                    case R.id.rb_female_settings:

                        break;
                }
            }
        };
        button_add_keywords.setOnClickListener(listener);
        button_save_journal.setOnClickListener(listener);
        button_show_guide.setOnClickListener(listener);
        radioButton_male.setOnClickListener(listener);
        radioButton_female.setOnClickListener(listener);

        seekBar_text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        TEXT_SIZE = 10;
                        break;
                    case 1:
                        TEXT_SIZE = 20;
                        break;
                    case 2:
                        TEXT_SIZE = 30;
                        break;
                }
                setTextSize();
                fillKeywords();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        listView_keywords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //вызвать диалог с полным редактированием данного слова
                Bundle argument = new Bundle();
                argument.putInt(KEY_FOR_DIALOG, EDIT_WORD);
                argument.putString(INFO_FOR_DIALOG, ((TextView)view).getText().toString());
                dialogEditWord = new DialogEditWord();
                FragmentManager manager = getSupportFragmentManager();
                dialogEditWord.setArguments(argument);
                dialogEditWord.show(manager, "editWord");
                return true;
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        //сохраняю
        saveSettings();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //загружаю
        loadSettings();
        fillKeywords();
        getOrientation();
    }

    public void loadSettings(){
        try{
            seekBar_store_days.setProgress(sharedPreferences.getInt(SETTINGS_STORE_DAYS, 0));
            seekBar_text_size.setProgress(sharedPreferences.getInt(SETTINGS_TEXT_SIZE, 0));
            switch (sharedPreferences.getInt(SETTINGS_TEXT_SIZE, 0)) {
                case 0:
                    TEXT_SIZE = 10;
                    break;
                case 1:
                    TEXT_SIZE = 20;
                    break;
                case 2:
                    TEXT_SIZE = 30;
                    break;
            }
            setTextSize();
            switch_vibro_at_load_sound.setChecked(sharedPreferences.getBoolean(SETTINGS_VIBRO_AT_LOAD_SOUND, false));
            switch_vibro_after_pause.setChecked(sharedPreferences.getBoolean(SETTINGS_VIBRO_AFTER_PAUSE, false));
            radioButton_female.setChecked(true);
            radioButton_male.setChecked(sharedPreferences.getBoolean(SETTINGS_GENDER, false));
            switch_keywords.setChecked(sharedPreferences.getBoolean(SETTINGS_KEYWORDS, false));
        }catch (Exception e){ Toast.makeText(this, "NO", Toast.LENGTH_SHORT).show();}
    }

    public void saveSettings(){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SETTINGS_STORE_DAYS, seekBar_store_days.getProgress());
        editor.putInt(SETTINGS_TEXT_SIZE, seekBar_text_size.getProgress());
        editor.putBoolean(SETTINGS_VIBRO_AT_LOAD_SOUND, switch_vibro_at_load_sound.isChecked());
        editor.putBoolean(SETTINGS_VIBRO_AFTER_PAUSE, switch_vibro_after_pause.isChecked());
        editor.putBoolean(SETTINGS_GENDER, radioButton_male.isChecked());
        editor.putBoolean(SETTINGS_KEYWORDS, switch_keywords.isChecked());

        editor.commit();
    }

    public void setTextSize(){
        tv1.setTextSize(TEXT_SIZE);
        tv2.setTextSize(TEXT_SIZE);
        tv3.setTextSize(TEXT_SIZE);
        tv6.setTextSize(TEXT_SIZE);
        tv7.setTextSize(TEXT_SIZE);
        button_save_journal.setTextSize(TEXT_SIZE);
        button_add_keywords.setTextSize(TEXT_SIZE);
        button_show_guide.setTextSize(TEXT_SIZE);
        radioButton_male.setTextSize(TEXT_SIZE);
        radioButton_female.setTextSize(TEXT_SIZE);
    }

    public void fillKeywords(){
        ArrayList list_keywords = new ArrayList();
        db = sqlWords.getReadableDatabase();
        cursor = db.query(sqlWords.NAME_TABLE,new String[]{columns_words[2]},
                SQLWords.COLUMN_WHAT_IS_IT+" = ?", new String[]{"1"},
                null,null,null);
        int index_word = cursor.getColumnIndex(columns_words[2]);
        while (cursor.moveToNext()){
            list_keywords.add(cursor.getString(index_word));
        }
        if (list_keywords.size()==10){
            FULL_KEYWORDS=true;
        }
        cursor.close();
        db.close();
        listView_keywords.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list_keywords) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(TEXT_SIZE);
                }
                return view;
            }
        });
    }
    public String getJournal(){
        String journal = "";
        sqlJournal = new SQLJournal(this, sqlJournal.NAME_TABLE, null,
                sqlJournal.VERSION_TABLE);
        db = sqlJournal.getReadableDatabase();
        cursor = db.query(sqlJournal.NAME_TABLE, columns_journal, null, null,
                null, null, columns_journal[2]);
        int index_id = cursor.getColumnIndex("_ID");
        int index_what_is_it = cursor.getColumnIndex(columns_journal[1]);
        int index_date = cursor.getColumnIndex(columns_journal[2]);
        int index_content = cursor.getColumnIndex(columns_journal[3]);
        while (cursor.moveToNext()) {
            int what_is_it = cursor.getInt(index_what_is_it);
            String date = cursor.getString(index_date);
            String content = cursor.getString(index_content);
            switch (what_is_it) {
                case 0:
                    journal+=(" \n"+"---"+date+"-"+content);
                    break;
                case 1:
                    journal+=(" \n"+"<<<"+date+"\n"+content);
                    break;
                case 2:
                    journal+=(" \n"+">>>"+date+"\n"+content);
                    break;
            }
        }
        cursor.close();
        db.close();
        return journal;
    }
    public void getOrientation(){
        if (MainActivity.REVERSE_ORIENTATION){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    public void showGuide(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETTINGS_SHOW_GUIDE, true);
        editor.commit();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}