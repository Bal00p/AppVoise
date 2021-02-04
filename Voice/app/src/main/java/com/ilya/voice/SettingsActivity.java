package com.ilya.voice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    SeekBar seekBar_text_size, seekBar_pitch, seekBar_speech_rate, seekBar_store_days;
    Button button_add_keywords;
    CheckBox checkBox_vibro_at_load_sound, checkBox_vibro_after_pause, checkBox_keywords,
            checkBox_voicing_emoticons;
    TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8;
    ListView listView_keywords;
    SharedPreferences sharedPreferences;
    public static final String
            SETTINGS_TEXT_SIZE = "text_size",
            SETTINGS_VIBRO_AT_LOAD_SOUND = "vibro_at_load_sound",
            SETTINGS_VIBRO_AFTER_PAUSE = "vibro_after_pause",
            SETTINGS_PITCH = "pitch",
            SETTINGS_SPEECH_RATE = "speech_rate",
            SETTINGS_KEYWORDS = "keywords",
            SETTINGS_STORE_DAYS = "store_days",
            SETTINGS_VOICING_EMOTICONS = "sound_signs",
            SETTINGS_LANGUAGE = "language",
            PATH_TO_SETTINGS = "settings";
    public int TEXT_SIZE = 10;
    SQLWords sqlWords;
    public String[] columns_words = {"_ID",
            SQLWords.COLUMN_WHAT_IS_IT,
            SQLWords.COLUMN_WORD,
            SQLWords.COLUMN_RATING};
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
        seekBar_pitch = (SeekBar)findViewById(R.id.sb_pitch_settings);
        seekBar_speech_rate = (SeekBar)findViewById(R.id.sb_speech_rate_settings);
        seekBar_store_days = (SeekBar)findViewById(R.id.sb_store_days_settings);
        checkBox_vibro_at_load_sound = (CheckBox)findViewById(R.id.cb_vibro_at_loud_sounds_settings);
        checkBox_vibro_after_pause = (CheckBox)findViewById(R.id.cb_vibro_after_pause_settings);
        checkBox_keywords = (CheckBox)findViewById(R.id.cb_keywords_settings);
        checkBox_voicing_emoticons = (CheckBox)findViewById(R.id.cb_voicing_emoticons_settings);
        button_add_keywords = (Button)findViewById(R.id.btn_add_keyword_settings);
        listView_keywords = (ListView)findViewById(R.id.lv_keywords);
        tv1 = (TextView)findViewById(R.id.tv1_settings);
        tv2 = (TextView)findViewById(R.id.tv2_settings);
        tv3 = (TextView)findViewById(R.id.tv3_settings);
        tv4 = (TextView)findViewById(R.id.tv4_settings);
        tv5 = (TextView)findViewById(R.id.tv5_settings);
        tv6 = (TextView)findViewById(R.id.tv6_settings);
        tv7 = (TextView)findViewById(R.id.tv7_settings);
        tv8 = (TextView)findViewById(R.id.tv8_settings);

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
                                Toast.makeText(getApplicationContext(), "YES", Toast.LENGTH_SHORT).show();
                            }catch (Exception e){}
                        }
                        break;
                }
            }
        };
        button_add_keywords.setOnClickListener(listener);

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
            checkBox_vibro_at_load_sound.setChecked(sharedPreferences.getBoolean(SETTINGS_VIBRO_AT_LOAD_SOUND, false));
            checkBox_vibro_after_pause.setChecked(sharedPreferences.getBoolean(SETTINGS_VIBRO_AFTER_PAUSE, false));
            checkBox_voicing_emoticons.setChecked(sharedPreferences.getBoolean(SETTINGS_VOICING_EMOTICONS, false));
            seekBar_pitch.setProgress(sharedPreferences.getInt(SETTINGS_PITCH, 0));
            seekBar_speech_rate.setProgress(sharedPreferences.getInt(SETTINGS_SPEECH_RATE, 0));
            checkBox_keywords.setChecked(sharedPreferences.getBoolean(SETTINGS_KEYWORDS, false));
        }catch (Exception e){ Toast.makeText(this, "NO", Toast.LENGTH_SHORT).show();}
    }

    public void saveSettings(){
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(SETTINGS_STORE_DAYS, seekBar_store_days.getProgress());
        editor.putInt(SETTINGS_TEXT_SIZE, seekBar_text_size.getProgress());
        editor.putBoolean(SETTINGS_VIBRO_AT_LOAD_SOUND, checkBox_vibro_at_load_sound.isChecked());
        editor.putBoolean(SETTINGS_VIBRO_AFTER_PAUSE, checkBox_vibro_after_pause.isChecked());
        editor.putInt(SETTINGS_PITCH, seekBar_pitch.getProgress());
        editor.putInt(SETTINGS_SPEECH_RATE, seekBar_speech_rate.getProgress());
        editor.putBoolean(SETTINGS_KEYWORDS, checkBox_keywords.isChecked());
        editor.putBoolean(SETTINGS_VOICING_EMOTICONS, checkBox_voicing_emoticons.isChecked());

        editor.commit();
//        Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }

    public void setTextSize(){
        tv1.setTextSize(TEXT_SIZE);
        tv2.setTextSize(TEXT_SIZE);
        tv3.setTextSize(TEXT_SIZE);
        tv4.setTextSize(TEXT_SIZE);
        tv5.setTextSize(TEXT_SIZE);
        tv6.setTextSize(TEXT_SIZE);
        tv7.setTextSize(TEXT_SIZE);
        tv8.setTextSize(TEXT_SIZE);
//        button_add_keywords.setTextSize(TEXT_SIZE);
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
}