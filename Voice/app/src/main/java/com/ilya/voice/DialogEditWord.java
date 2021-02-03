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
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class DialogEditWord extends DialogFragment {

    SQLWords sqlWords;
    SQLiteDatabase db;
    String info;
    int argument;
    ContentValues row;
    EditText editText_keyword;
    View view;

    public interface onSomeEventListener {
        public void someEvent(String s);
    }

    PhrasesFragment.onSomeEventListener someEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (PhrasesFragment.onSomeEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    //реализую диалоговое окно
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        sqlWords = new SQLWords(getActivity().getApplicationContext(), SQLWords.NAME_TABLE,
                null, SQLWords.VERSION_TABLE);
        argument = getArguments().getInt(SettingsActivity.KEY_FOR_DIALOG);
        info = getArguments().getString(SettingsActivity.INFO_FOR_DIALOG);
        view = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_dialog_edit_word, null);
        editText_keyword = (EditText)view.findViewById(R.id.et_keyword_dialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(setMessage(argument))
                .setView(view)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // cancel
                    }
                });
        if (argument==1 || argument==3){
            editText_keyword.setText(info);
            builder.setPositiveButton(R.string.edit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    switch (argument){
                        case 1:
                            editWord(info);
                            break;
                        case 3:
                            editPhrase(info);
                            break;
                    }
                }
            })
            .setNeutralButton( R.string.remove, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    switch (argument){
                        case 1:
                            deleteWord(info);
                            break;
                        case 3:
                            deletePhrase(info);
                            break;
                    }
                }
            });
        }else{
            builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    switch (argument){
                        case 0:
                            addWord();
                            break;
                        case 2:
                            addPhrase();
                            break;
                    }
                }
            });
        }
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
    public void addWord(){
        db = sqlWords.getWritableDatabase();
        row = new ContentValues();
        row.put(SQLWords.COLUMN_WHAT_IS_IT,1);
        row.put(SQLWords.COLUMN_RATING,0);
        row.put(SQLWords.COLUMN_WORD,editText_keyword.getText().toString());
        db.insert(SQLWords.NAME_TABLE,null,row);
        db.close();
        Intent i = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }
    public void editWord(String word){
        db = sqlWords.getWritableDatabase();
        row = new ContentValues();
        row.put(SQLWords.COLUMN_WHAT_IS_IT,1);
        row.put(SQLWords.COLUMN_RATING,0);
        row.put(SQLWords.COLUMN_WORD,editText_keyword.getText().toString());
        db.update(SQLWords.NAME_TABLE,row,SQLWords.COLUMN_WORD+" = ?",
                new String[]{word});
        db.close();
        Intent i = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }
    public void deleteWord(String word){
        db = sqlWords.getWritableDatabase();
        db.delete(SQLWords.NAME_TABLE, SQLWords.COLUMN_WORD+" = ?",
                new String[]{word});
        db.close();
        Intent i = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
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