package com.nnsookwon.workout_log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nnsoo on 8/18/2016.
 */
    public class DbHelper extends SQLiteOpenHelper {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DATE_SORT = "date_sort";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_EXERCISE = "exercise";
    public static final String KEY_SETS = "sets";


    public static final String DATABASE_NAME = "WorkoutLog";
    public static final String DATABASE_TABLE_LOG_ENTRIES = "log_entry_table";
    public static final String DATABASE_TABLE_EXERCISES = "exercises_table";
    public static final int DATABASE_VERSION = 1;


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //for log entries
        db.execSQL("CREATE TABLE " + DATABASE_TABLE_LOG_ENTRIES + " (" +
                KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DATE + " TEXT NOT NULL, " +
                KEY_DATE_SORT + " TEXT NOT NULL, " +
                KEY_EXERCISE + " TEXT NOT NULL, " +
                KEY_CATEGORY + " TEXT NOT NULL, " +
                KEY_SETS + " TEXT NOT NULL);");

        //for types of exercises
        db.execSQL("CREATE TABLE " + DATABASE_TABLE_EXERCISES + " (" +
                KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_CATEGORY + " TEXT NOT NULL, " +
                KEY_EXERCISE + " TEXT NOT NULL);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LOG_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_EXERCISES);
        onCreate(db);
    }
}