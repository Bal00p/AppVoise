package com.ilya.voice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.fragment.app.DialogFragment;

public class DialogEditPhrase extends DialogFragment {

    SQLWords sqlWords;
    SQLiteDatabase db;
    String info;
    int argument;
    ContentValues row;
    EditText editText_keyword;
    View view;

    PhrasesFragment.onSomeEventListenerMain someEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (PhrasesFragment.onSomeEventListenerMain) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListenerMain");
        }
    }

    //реализую диалоговое окно
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(MainActivity.OPEN_FRAGMENT){

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            sqlWords = new SQLWords(getActivity().getApplicationContext(), SQLWords.NAME_TABLE,
                    null, SQLWords.VERSION_TABLE);
            argument = getArguments().getInt(SettingsActivity.KEY_FOR_DIALOG);
            info = getArguments().getString(SettingsActivity.INFO_FOR_DIALOG);
            view = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_dialog_edit_word, null);
            editText_keyword = (EditText) view.findViewById(R.id.et_keyword_dialog);
            builder.setMessage(setMessage(argument))
                    .setView(view)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // cancel
                        }
                    });
            if (argument == 1 || argument == 3) {
                editText_keyword.setText(info);
                builder.setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editPhrase(info);
                    }
                })
                        .setNeutralButton(R.string.remove, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deletePhrase(info);
                            }
                        });
            } else {
                builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addPhrase();
                    }
                });
            }
        }catch (Exception e){}
        return builder.create();
    }
    public String setMessage(int argument){
        switch (argument){
            case 0:
                return getString(R.string.add_word);
            case 1:
                return getString(R.string.edit_word);
            case 2:
                return getString(R.string.add_phrase);
            case 3:
                return getString(R.string.edit_phrase);
        }
        return "ERROR";
    }
    public void addPhrase(){
        db = sqlWords.getWritableDatabase();
        row = new ContentValues();
        row.put(SQLWords.COLUMN_WHAT_IS_IT,2);
        row.put(SQLWords.COLUMN_RATING,0);
        row.put(SQLWords.COLUMN_WORD,editText_keyword.getText().toString());
        db.insert(SQLWords.NAME_TABLE,null,row);
        db.close();
        someEventListener.someEvent("re_Open");
    }
    public void editPhrase(String word){
        db = sqlWords.getWritableDatabase();
        row = new ContentValues();
        row.put(SQLWords.COLUMN_WHAT_IS_IT,2);
        row.put(SQLWords.COLUMN_RATING,0);
        row.put(SQLWords.COLUMN_WORD,editText_keyword.getText().toString());
        db.update(SQLWords.NAME_TABLE,row,SQLWords.COLUMN_WORD+" = ?",
                new String[]{word});
        db.close();
        someEventListener.someEvent("re_Open");
    }
    public void deletePhrase(String word){
        db = sqlWords.getWritableDatabase();
        db.delete(SQLWords.NAME_TABLE, SQLWords.COLUMN_WORD+" = ?",
                new String[]{word});
        db.close();
        someEventListener.someEvent("re_Open");
    }
}