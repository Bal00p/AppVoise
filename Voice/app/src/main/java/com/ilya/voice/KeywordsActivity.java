package com.ilya.voice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class KeywordsActivity extends AppCompatActivity {

    ListView listView_keywords;
    Button button_add_keywords;
    public String[] columns_words = {"_ID",
            SQLWords.COLUMN_WHAT_IS_IT,
            SQLWords.COLUMN_WORD,
            SQLWords.COLUMN_RATING};
    public static int TEXT_SIZE = SettingsActivity.TEXTSIZE_MEDIUM;
    SQLWords sqlWords;
    SQLiteDatabase db;
    Cursor cursor;
    DialogFragment dialogEditWord;
    public static boolean FULL_KEYWORDS = false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keywords);

        sharedPreferences = getSharedPreferences(SettingsActivity.PATH_TO_SETTINGS, MODE_PRIVATE);
        sqlWords = new SQLWords(this, SQLWords.NAME_TABLE, null,
                SQLWords.VERSION_TABLE);

        button_add_keywords = (Button)findViewById(R.id.btn_add_keywords);
        listView_keywords = (ListView)findViewById(R.id.lv_keywords);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_add_keywords:
                        if(FULL_KEYWORDS){
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.keyword_limit),Toast.LENGTH_SHORT).show();
                        }else{
                            try{
                                //вызов диалога для создания нового слова
                                Bundle argument = new Bundle();
                                argument.putInt(SettingsActivity.KEY_FOR_DIALOG, SettingsActivity.ADD_WORD);
                                argument.putString(SettingsActivity.INFO_FOR_DIALOG, "NO");
                                dialogEditWord = new DialogEditWord();
                                FragmentManager manager = getSupportFragmentManager();
                                dialogEditWord.setArguments(argument);
                                dialogEditWord.show(manager, "addWord");
                            }catch (Exception e){}
                        }
                        break;
                }
            }
        };
        button_add_keywords.setOnClickListener(listener);

        listView_keywords.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //вызвать диалог с полным редактированием данного слова
                Bundle argument = new Bundle();
                argument.putInt(SettingsActivity.KEY_FOR_DIALOG, SettingsActivity.EDIT_WORD);
                argument.putString(SettingsActivity.INFO_FOR_DIALOG, ((TextView)view).getText().toString());
                dialogEditWord = new DialogEditWord();
                FragmentManager manager = getSupportFragmentManager();
                dialogEditWord.setArguments(argument);
                dialogEditWord.show(manager, "editWord");
                return true;
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        //загружаю
        getOrientation();
        loadSettings();
        fillKeywords();
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
        button_add_keywords.setTextSize(TEXT_SIZE);
    }
    public void getOrientation(){
        if (sharedPreferences.getBoolean(SettingsActivity.SETTINGS_REVERSE_ORIENTATION, false)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}