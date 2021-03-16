package com.ilya.voice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LanguageActivity extends AppCompatActivity {

    public static final Language[] LANGUAGES = {
            new Language("ru-RU","Русский"),
            new Language("ar-AE","العربية"),
            new Language("sq-AL","Shqip"),
            new Language("be-BY","Беларуский"),
            new Language("bg-BG","Български"),
            new Language("ca-ES","Català"),
            new Language("zh-CN","中文"),
            new Language("hr-HR","Hrvatski"),
            new Language("cs-CZ","Čeština"),
            new Language("da-DK","Dansk"),
            new Language("nl-NL","Nederlands"),
            new Language("en-US","English(US)"),
            new Language("en-GB","English(GB)"),
            new Language("et-EE","Eesti"),
            new Language("fi-FI","Suomi"),
            new Language("fr-FR","Française"),
            new Language("de-DE","Deutsche"),
            new Language("el-GR","Ελληνικά"),
            new Language("iw-IL","עברית"),
            new Language("hi-IN","हिंदी"),
            new Language("hu-HU","Magyar"),
            new Language("is-IS","Íslenska"),
            new Language("in-ID","Indonesia"),
            new Language("ga-IE","Gaeilge"),
            new Language("it-IT","Italiana"),
            new Language("ja-JP","日本語"),
            new Language("ko-KR","한국어"),
            new Language("lv-LV","Latviešu"),
            new Language("lt-LT","Lietuvių"),
            new Language("mk-MK","Македонски"),
            new Language("ms-MY","Melayu"),
            new Language("mt-MT","Malti"),
            new Language("no-NO","Norsk"),
            new Language("pl-PL","Polski"),
            new Language("pt-PT","Portugues"),
            new Language("ro-RO","Română"),
            new Language("sr-RS","Српски"),
            new Language("sk-SK","Slovenčina"),
            new Language("sl-SI","Slovenščina"),
            new Language("es-ES","Español"),
            new Language("sv-SE","Svenska"),
            new Language("th-TH","ไทย"),
            new Language("tr-TR","Türk"),
            new Language("uk-UA","Українська"),
            new Language("vi-VN","Việt")
    };
    ListView listView_languages;
    SharedPreferences sharedPreferences = null;
    public static int TEXT_SIZE = SettingsActivity.TEXTSIZE_MEDIUM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        sharedPreferences = getSharedPreferences(SettingsActivity.PATH_TO_SETTINGS, MODE_PRIVATE);
        getOrientation();
        listView_languages = (ListView)findViewById(R.id.lv_select_language);
        ArrayList<String> list_language = new ArrayList();
        for(Language language: LANGUAGES){
            list_language.add(language.name);
        }
        listView_languages.setAdapter(new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list_language) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(TEXT_SIZE);
                    ((TextView) view).setTextColor(getColor(R.color.textColor_in_settings));
                }
                return view;
            }
        });

        listView_languages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                try{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    for(Language language: LANGUAGES){
                        if (language.name.equals(((TextView) itemClicked).getText().toString())){
                            editor.putString(SettingsActivity.SETTINGS_LANGUAGE, language.tag);
                        }
                    }
                    editor.commit();
                    finish();
                }catch (Exception e){}
            }
        });
    }
    public void getOrientation(){
        if (sharedPreferences.getBoolean(SettingsActivity.SETTINGS_REVERSE_ORIENTATION, false)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    public void loadSettings(){
        switch (sharedPreferences.getInt(SettingsActivity.SETTINGS_TEXT_SIZE, 0)) {
            case 0:
                TEXT_SIZE = SettingsActivity.TEXTSIZE_LOW;
                break;
            case 1:
                TEXT_SIZE = SettingsActivity.TEXTSIZE_MEDIUM;
                break;
            case 2:
                TEXT_SIZE = SettingsActivity.TEXTSIZE_HIGH;
                break;
        }
    }
}