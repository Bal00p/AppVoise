package com.ilya.voice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLJournal extends SQLiteOpenHelper {
    public static final int VERSION_TABLE = 1;
    public static final String NAME_TABLE = "JOURNAL";
    public static final String COLUMN_WHAT_IS_IT = "WHAT_IS_IT"; //0 - time, 1 - my, 2 - outside
    public static final String COLUMN_DATE = "DATE";
    public static final String COLUMN_CONTENT = "CONTENT";

    public SQLJournal(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+NAME_TABLE+" (_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                +COLUMN_WHAT_IS_IT+" INTEGER, "
                +COLUMN_DATE+" TEXT, "
                +COLUMN_CONTENT+" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
