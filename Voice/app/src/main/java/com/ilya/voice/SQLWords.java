package com.ilya.voice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLWords extends SQLiteOpenHelper {
    public static final int VERSION_TABLE = 1;
    public static final String NAME_TABLE = "FAST_WORD";
    public static final String COLUMN_WHAT_IS_IT = "WHAT_IS_IT"; //0 - textToSpeech, 1 - KeyWords
    public static final String COLUMN_WORD = "WORD";
    public static final String COLUMN_RATING = "RATING";

    public SQLWords(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+NAME_TABLE+" (_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                +COLUMN_WHAT_IS_IT+" INTEGER, "
                +COLUMN_WORD+" TEXT, "
                +COLUMN_RATING+" INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
