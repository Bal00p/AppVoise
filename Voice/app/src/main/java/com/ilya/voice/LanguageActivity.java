package com.ilya.voice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LanguageActivity extends AppCompatActivity {

    public static final Language[] LANGUAGES = {
            new Language("ru-RU","Русский"),
            new Language("uk-UA","Українська"),
            new Language("be-BY","Беларуский"),
            new Language("zh-CN","中文"),
            new Language("en-US","English(US)"),
            new Language("en-GB","English(GB)"),
            new Language("fr-FR","Française"),
            new Language("de-DE","Deutsche"),
            new Language("it-IT","Italiana"),
            new Language("ja-JP","日本語"),
            new Language("pt-PT","Portugues"),
            new Language("es-ES","Española"),
            new Language("tr-TR","Türk")
    };
    ListView listView_languages;
    SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        sharedPreferences = getSharedPreferences(SettingsActivity.PATH_TO_SETTINGS, MODE_PRIVATE);
        listView_languages = (ListView)findViewById(R.id.lv_select_language);
        ArrayList<String> list_language = new ArrayList();
        for(Language language: LANGUAGES){
            list_language.add(language.name);
        }
        ArrayAdapter adapter_language = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list_language);
        listView_languages.setAdapter(adapter_language);

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
}