package com.ilya.voice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    SeekBar seekBar_text_size, seekBar_store_days;
    Button button_add_keywords, button_save_journal, button_show_guide, button_delete_journal;
    RadioButton radioButton_male, radioButton_female;
    Switch switch_vibro_at_load_sound, switch_vibro_after_pause, switch_keywords;
    TextView tv1, tv2, tv3, tv6, tv7, tv_credits;
    SharedPreferences sharedPreferences;
    public static final String
            SETTINGS_TEXT_SIZE = "text_size",
            SETTINGS_VIBRO_AT_LOAD_SOUND = "vibro_at_load_sound",
            SETTINGS_VIBRO_AFTER_PAUSE = "vibro_after_pause",
            SETTINGS_GENDER = "gender",
            SETTINGS_KEYWORDS = "keywords",
            SETTINGS_STORE_DAYS = "store_days",
            SETTINGS_LANGUAGE = "language",
            SETTINGS_SHOW_GUIDE = "show_guide",
            SETTINGS_REVERSE_ORIENTATION = "reverse_orientation",
            PATH_TO_SETTINGS = "settings";

    public static final int TEXTSIZE_LOW = 10;
    public static final int TEXTSIZE_MEDIUM = 20;
    public static final int TEXTSIZE_HIGH = 28;

    public static int TEXT_SIZE = TEXTSIZE_MEDIUM;
    SQLJournal sqlJournal;
    SQLiteDatabase db;
    public static final String KEY_FOR_DIALOG = "key_for_dialog",
            INFO_FOR_DIALOG = "info_for_dialog";
    public static final int ADD_WORD = 0, EDIT_WORD = 1, ADD_PHRASE = 2, EDIT_PHRASE = 3;
    public static boolean REVERSE_ORIENTATION = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(PATH_TO_SETTINGS, MODE_PRIVATE);

        seekBar_text_size = (SeekBar)findViewById(R.id.sb_text_size_settings);
        seekBar_store_days = (SeekBar)findViewById(R.id.sb_store_days_settings);
        switch_vibro_at_load_sound = (Switch)findViewById(R.id.sw_vibro_at_loud_sounds_settings);
        switch_vibro_after_pause = (Switch)findViewById(R.id.sw_vibro_after_pause_settings);
        switch_keywords = (Switch)findViewById(R.id.sw_keywords_settings);
        button_add_keywords = (Button)findViewById(R.id.btn_open_keyword_settings);
        button_save_journal = (Button)findViewById(R.id.btn_rotate_settings);
        button_show_guide = (Button)findViewById(R.id.btn_show_guide);
        button_delete_journal = (Button)findViewById(R.id.btn_delete_journal);
        radioButton_male = (RadioButton)findViewById(R.id.rb_male_settings);
        radioButton_female = (RadioButton)findViewById(R.id.rb_female_settings);
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
                    case R.id.btn_open_keyword_settings:
                        //открываю активити с ключевыми словами
                        Intent intent = new Intent(SettingsActivity.this, KeywordsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.btn_rotate_settings:
                        //вращаю
                        setOrientation();
                        break;
                    case R.id.btn_show_guide:
                        showGuide();
                        break;
                    case R.id.btn_delete_journal:
                        deleteJournal();
                        break;
                }
            }
        };
        button_add_keywords.setOnClickListener(listener);
        button_save_journal.setOnClickListener(listener);
        button_show_guide.setOnClickListener(listener);
        button_delete_journal.setOnClickListener(listener);

        seekBar_text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        TEXT_SIZE = TEXTSIZE_LOW;
                        break;
                    case 1:
                        TEXT_SIZE = TEXTSIZE_MEDIUM;
                        break;
                    case 2:
                        TEXT_SIZE = TEXTSIZE_HIGH;
                        break;
                }
                setTextSize();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBar_store_days.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        tv6.setText(getString(R.string.store_days)+
                                " ("+getString(R.string.store_days_1)+")");
                        break;
                    case 1:
                        tv6.setText(getString(R.string.store_days)+
                                " ("+getString(R.string.store_days_3)+")");
                        break;
                    case 2:
                        tv6.setText(getString(R.string.store_days)+
                                " ("+getString(R.string.store_days_7)+")");
                        break;
                    case 3:
                        tv6.setText(getString(R.string.store_days)+
                                " ("+getString(R.string.store_days_30)+")");
                        break;
                    case 4:
                        tv6.setText(getString(R.string.store_days)+
                                " ("+getString(R.string.store_days_always)+")");
                        break;
                }
                setTextSize();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
        getOrientation();
    }

    public void loadSettings(){
        try{
            REVERSE_ORIENTATION=sharedPreferences.getBoolean(SETTINGS_REVERSE_ORIENTATION, false);
            seekBar_store_days.setProgress(sharedPreferences.getInt(SETTINGS_STORE_DAYS, 0));
            switch (sharedPreferences.getInt(SETTINGS_STORE_DAYS, 0)) {
                case 0:
                    tv6.setText(getString(R.string.store_days)+
                            " ("+getString(R.string.store_days_1)+")");
                    break;
                case 1:
                    tv6.setText(getString(R.string.store_days)+
                            " ("+getString(R.string.store_days_3)+")");
                    break;
                case 2:
                    tv6.setText(getString(R.string.store_days)+
                            " ("+getString(R.string.store_days_7)+")");
                    break;
                case 3:
                    tv6.setText(getString(R.string.store_days)+
                            " ("+getString(R.string.store_days_30)+")");
                    break;
                case 4:
                    tv6.setText(getString(R.string.store_days)+
                            " ("+getString(R.string.store_days_always)+")");
                    break;
            }
            seekBar_text_size.setProgress(sharedPreferences.getInt(SETTINGS_TEXT_SIZE, 0));
            switch (sharedPreferences.getInt(SETTINGS_TEXT_SIZE, 0)) {
                case 0:
                    TEXT_SIZE = TEXTSIZE_LOW;
                    break;
                case 1:
                    TEXT_SIZE = TEXTSIZE_MEDIUM;
                    break;
                case 2:
                    TEXT_SIZE = TEXTSIZE_HIGH;
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
        editor.putBoolean(SETTINGS_REVERSE_ORIENTATION,REVERSE_ORIENTATION);

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
        button_delete_journal.setTextSize(TEXT_SIZE);
        radioButton_male.setTextSize(TEXT_SIZE);
        radioButton_female.setTextSize(TEXT_SIZE);
    }

    public void setOrientation(){
        if (REVERSE_ORIENTATION){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            REVERSE_ORIENTATION=false;
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            if(getRotateOrientation()){
                REVERSE_ORIENTATION=true;
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.enable_auto_rotate),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void getOrientation(){
        if (REVERSE_ORIENTATION){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    public boolean getRotateOrientation() {
        int rotate = getWindowManager().getDefaultDisplay().getRotation();
        if (rotate== Surface.ROTATION_180){
            return true;
        }
        return false;
    }
    public void showGuide(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETTINGS_SHOW_GUIDE, true);
        editor.commit();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    int count = 0;
    public void deleteJournal(){
        count++;
        if (count > 1) {
            sqlJournal = new SQLJournal(this, sqlJournal.NAME_TABLE, null,
                    sqlJournal.VERSION_TABLE);
            db = sqlJournal.getWritableDatabase();
            db.delete(SQLJournal.NAME_TABLE,"_ID LIKE ?", new String[]{"%"});
            db.close();
            Toast.makeText(this, getString(R.string.deleted_journal), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.click_to_confirm), Toast.LENGTH_SHORT).show();
            // resetting the counter in 2s
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    count = 0;
                }
            }, 2000);
        }
    }
}