package com.ilya.voice;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PhrasesFragment extends Fragment {


    Button button_back;
    ImageButton button_add;
    EditText editText_search;
    ListView listView_phrases;
    SQLWords sqlWords;
    Cursor cursor;
    SQLiteDatabase db;
    DialogFragment dialogEditPhrase;
    GestureDetector gestureDetector;

    public interface onSomeEventListenerMain {
        public void someEvent(String s);
    }

    onSomeEventListenerMain someEventListenerMain;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListenerMain = (onSomeEventListenerMain) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListenerMain");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phrases, container, false);

        gestureDetector = new GestureDetector(new PhrasesFragment.GestureListener());

        sqlWords = new SQLWords(getActivity().getApplicationContext(), SQLWords.NAME_TABLE,
                null, SQLWords.VERSION_TABLE);
        button_add = (ImageButton)view.findViewById(R.id.btn_add_phrase);
        button_back = (Button)view.findViewById(R.id.btn_back_fragment);
        editText_search = (EditText) view.findViewById(R.id.et_search_phrases);
        Drawable img = getResources().getDrawable(android.R.drawable.ic_menu_search);
        img.setBounds(0, 0, 50, 50);
        editText_search.setCompoundDrawables(img, null, null, null);
        
        listView_phrases = (ListView)view.findViewById(R.id.lv_phrases);
        fillPhrases("");

        listView_phrases.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //вызвать диалог с полным редактированием данной фразы
                Bundle argument = new Bundle();
                argument.putInt(SettingsActivity.KEY_FOR_DIALOG, SettingsActivity.EDIT_PHRASE);
                argument.putString(SettingsActivity.INFO_FOR_DIALOG, ((TextView)view).getText().toString());
                dialogEditPhrase = new DialogEditPhrase();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                dialogEditPhrase.setArguments(argument);
                dialogEditPhrase.show(manager, "editPhrase");
                return true;
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_add_phrase:
                        //вызов диалога для создания новой фразы
                        Bundle argument = new Bundle();
                        argument.putInt(SettingsActivity.KEY_FOR_DIALOG, SettingsActivity.ADD_PHRASE);
                        argument.putString(SettingsActivity.INFO_FOR_DIALOG, "NO");
                        dialogEditPhrase = new DialogEditPhrase();
                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        dialogEditPhrase.setArguments(argument);
                        dialogEditPhrase.show(manager, "addPhrase");
                        break;
                    case R.id.btn_back_fragment:
                        someEventListenerMain.someEvent("cloSe_phrAses");
                        break;
                }
            }
        };
        button_add.setOnClickListener(listener);
        button_back.setOnClickListener(listener);

        listView_phrases.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                //озвучиваю и закрываю
                someEventListenerMain.someEvent(((TextView)itemClicked).getText().toString());
            }
        });

        button_back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        editText_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                fillPhrases(s.toString());
            }
        });
        return view;
    }

    public void fillPhrases(String criterion){
        ArrayList list_phrases = new ArrayList();
        db = sqlWords.getReadableDatabase();
        if(criterion.equals("")){
            cursor = db.query(sqlWords.NAME_TABLE,new String[]{sqlWords.COLUMN_WORD},
                    SQLWords.COLUMN_WHAT_IS_IT+" = ?", new String[]{"2"},
                    null,null,null);
        }else{
            cursor = db.query(sqlWords.NAME_TABLE,new String[]{sqlWords.COLUMN_WORD},
                    SQLWords.COLUMN_WHAT_IS_IT+" = ? AND "+
                            SQLWords.COLUMN_WORD+" LIKE ?", new String[]{"2","%"+criterion+"%"},
                    null,null,null);
        }
        int index_word = cursor.getColumnIndex(sqlWords.COLUMN_WORD);
        while (cursor.moveToNext()){
            list_phrases.add(cursor.getString(index_word));
        }
        if(list_phrases.size()==0 && criterion.equals("")){
            db = sqlWords.getWritableDatabase();
            ContentValues row = new ContentValues();
            row.put(sqlWords.COLUMN_WORD,getString(R.string.phrase_1));
            row.put(sqlWords.COLUMN_WHAT_IS_IT, 2);
            row.put(sqlWords.COLUMN_RATING, 0);
            db.insert(sqlWords.NAME_TABLE, null, row);
            row = new ContentValues();
            row.put(sqlWords.COLUMN_WORD,getString(R.string.phrase_2));
            row.put(sqlWords.COLUMN_WHAT_IS_IT, 2);
            row.put(sqlWords.COLUMN_RATING, 0);
            db.insert(sqlWords.NAME_TABLE, null, row);
            row = new ContentValues();
            row.put(sqlWords.COLUMN_WORD,getString(R.string.phrase_3));
            row.put(sqlWords.COLUMN_WHAT_IS_IT, 2);
            row.put(sqlWords.COLUMN_RATING, 0);
            db.insert(sqlWords.NAME_TABLE, null, row);
            list_phrases.add(getString(R.string.phrase_1));
            list_phrases.add(getString(R.string.phrase_2));
            list_phrases.add(getString(R.string.phrase_3));
        }
        cursor.close();
        db.close();
        listView_phrases.setAdapter(new ArrayAdapter(getActivity().getApplicationContext(),
                R.layout.textviev, list_phrases) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(MainActivity.TEXT_SIZE);
                }
                return view;
            }
        });
    }
    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > MainActivity.SWIPE_MIN_DISTANCE &&
                    Math.abs(velocityX) > MainActivity.SWIPE_THRESHOLD_VELOCITY) {
                someEventListenerMain.someEvent("cloSe_phrAses");
                return false; // справа налево
            }
            return false;
        }
    }
}