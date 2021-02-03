package com.ilya.voice;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PhrasesFragment extends Fragment {

    Button button_add, button_back;
    ListView listView_phrases;
    SQLWords sqlWords;
    Cursor cursor;
    SQLiteDatabase db;
    DialogFragment dialogEditWord;
    GestureDetector gestureDetector;

    public interface onSomeEventListener {
        public void someEvent(String s);
    }

    onSomeEventListener someEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (onSomeEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phrases, container, false);

        gestureDetector = new GestureDetector(new PhrasesFragment.GestureListener());

        sqlWords = new SQLWords(getActivity().getApplicationContext(), SQLWords.NAME_TABLE,
                null, SQLWords.VERSION_TABLE);
        button_add = (Button)view.findViewById(R.id.btn_add_phrase);
        button_back = (Button)view.findViewById(R.id.btn_back_fragment);
        listView_phrases = (ListView)view.findViewById(R.id.lv_phrases);
        fillPhrases();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.btn_add_phrase:
                        //вызов диалога для создания новой фразы
                        Bundle argument = new Bundle();
                        argument.putInt(SettingsActivity.KEY_FOR_DIALOG, SettingsActivity.ADD_PHRASE);
                        argument.putString(SettingsActivity.INFO_FOR_DIALOG, "NO");
                        dialogEditWord = new DialogEditWord();
                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        dialogEditWord.setArguments(argument);
                        dialogEditWord.show(manager, "addPhrase");
                        break;
                    case R.id.btn_back_fragment:
                        someEventListener.someEvent("");
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
                someEventListener.someEvent(((TextView)itemClicked).getText().toString());
            }
        });

        listView_phrases.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //вызвать диалог с полным редактированием данной фразы
                Bundle argument = new Bundle();
                argument.putInt(SettingsActivity.KEY_FOR_DIALOG, SettingsActivity.EDIT_PHRASE);
                argument.putString(SettingsActivity.INFO_FOR_DIALOG, ((TextView)view).getText().toString());
                dialogEditWord = new DialogEditWord();
                FragmentManager manager = getActivity().getSupportFragmentManager();
                dialogEditWord.setArguments(argument);
                dialogEditWord.show(manager, "editPhrase");
                return false;
            }
        });
        button_back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        return view;
    }

    public void fillPhrases(){
        ArrayList list_phrases = new ArrayList();
        db = sqlWords.getReadableDatabase();
        cursor = db.query(sqlWords.NAME_TABLE,new String[]{sqlWords.COLUMN_WORD},
                SQLWords.COLUMN_WHAT_IS_IT+" = ?", new String[]{"2"},
                null,null,null);
        int index_word = cursor.getColumnIndex(sqlWords.COLUMN_WORD);
        while (cursor.moveToNext()){
            list_phrases.add(cursor.getString(index_word));
        }
        cursor.close();
        db.close();
        listView_phrases.setAdapter(new ArrayAdapter(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_1, list_phrases) {
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
                someEventListener.someEvent("");
                return false; // справа налево
            }
            return false;
        }
    }
}