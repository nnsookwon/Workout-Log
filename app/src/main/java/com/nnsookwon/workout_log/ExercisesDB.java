package com.nnsookwon.workout_log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by nnsoo on 8/27/2016.
 */
public class ExercisesDB {
    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDataBase;
    String[] columns;

    public ExercisesDB(Context c) {
        ourContext = c;
        open();
        columns = new String[]{DbHelper.KEY_ROWID, DbHelper.KEY_CATEGORY,
                DbHelper.KEY_EXERCISE};
        initDB();
        close();
    }

    public ExercisesDB open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDataBase = ourHelper.getWritableDatabase();

        return this;
    }

    public void close() {
        ourHelper.close();
    }

    public void initDB() {
        Cursor c = ourDataBase.rawQuery("SELECT count(*) FROM " + DbHelper.DATABASE_TABLE_EXERCISES, null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            createEntry("Arms", "Bicep Curls");
            createEntry("Arms", "Hammer Curls");
            createEntry("Arms", "Side Curls");
            createEntry("Arms", "Dips");
            createEntry("Arms", "Tricep Pull-downs");
            createEntry("Chest", "Bench Press");
            createEntry("Chest", "Incline Bench Press");
            createEntry("Chest", "Decline Bench Press");
            createEntry("Chest", "DB Chest Press");
            createEntry("Chest", "Incline DB Chest Press");
            createEntry("Chest", "Decline DB Chest Press");
            createEntry("Back", "Pull-ups");
            createEntry("Back", "T-bar Rows");
            createEntry("Back", "Cable Rows");
            createEntry("Back", "Barbell Rows");
            createEntry("Back", "DB Shrugs");
            createEntry("Shoulders", "Lateral DB Raises");
            createEntry("Shoulders", "Rear DB Raises");
            createEntry("Shoulders", "DB Shoulder Press");
            createEntry("Shoulders", "Barbell Shoulder Press");
            createEntry("Legs", "Squats");
            createEntry("Legs", "Deadlifts");
            createEntry("Core", "Leg Raises");
            createEntry("Core", "DB Oblique Curls");
        }
    }

    public boolean createEntry(String category, String exerciseName) {
        //returns false if exerciseName already exists in DB
        //prevents duplicate entries of same exerciseName

        if (category == null || exerciseName == null)
            return false;

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_EXERCISES, columns,
                DbHelper.KEY_EXERCISE + "=?",
                new String[]{exerciseName}, null, null, null, null);

        if (c.getCount() > 0)
            return false;

        ContentValues cv = new ContentValues();
        cv.put(DbHelper.KEY_CATEGORY, category);
        cv.put(DbHelper.KEY_EXERCISE, exerciseName);

        ourDataBase.insert(DbHelper.DATABASE_TABLE_EXERCISES, null, cv);
        return true;
    }

    public boolean updateEntry(String category, String exerciseName) {
        //returns false if exerciseName does not exist in DB

        if (category == null || exerciseName == null)
            return false;

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_EXERCISES, columns,
                DbHelper.KEY_EXERCISE + "=?",
                new String[]{exerciseName}, null, null, null, null);

        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ContentValues cv = new ContentValues();
            cv.put(DbHelper.KEY_CATEGORY, category);
            cv.put(DbHelper.KEY_EXERCISE, exerciseName);

            ourDataBase.update(DbHelper.DATABASE_TABLE_EXERCISES, cv, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
            return true;
        }
        return false;
    }


    public boolean deleteEntry(String exerciseName) {
        //returns false if exerciseName does not exist in DB

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_EXERCISES, columns,
                DbHelper.KEY_EXERCISE + "=?",
                new String[]{exerciseName}, null, null, null, null);
        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);
        if (c.getCount() > 0) {
            c.moveToFirst();
            ourDataBase.delete(DbHelper.DATABASE_TABLE_EXERCISES, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
            return true;
        }
        return false;
    }

    public ArrayList<String> getCategories() {
        ArrayList<String> categories = new ArrayList<String>();
        categories.add("");

        Cursor c = ourDataBase.rawQuery("SELECT DISTINCT " + DbHelper.KEY_CATEGORY +
                " FROM " + DbHelper.DATABASE_TABLE_EXERCISES +
                " ORDER BY " + DbHelper.KEY_CATEGORY, null);
        int iCategory = c.getColumnIndex(DbHelper.KEY_CATEGORY);

        c.moveToFirst();
        while (!c.isAfterLast()) {
            categories.add(c.getString(iCategory));
            c.moveToNext();
        }
        return categories;
    }

    public ArrayList<String> getExercises(String category) {
        //returns ArrayList of exercises under category
        //if empty string passed in, return ArrayList with one empty string

        ArrayList<String> exercises = new ArrayList<String>();
        exercises.add("");

        Cursor c;
        if (!category.trim().isEmpty()) {
            try {
                exercises.add("Other");
                c = ourDataBase.rawQuery("SELECT * FROM " + DbHelper.DATABASE_TABLE_EXERCISES +
                        " WHERE " + DbHelper.KEY_CATEGORY + " = " + "\"" + category + "\"" +
                        " ORDER BY " + DbHelper.KEY_EXERCISE, null);


                int iExercise = c.getColumnIndex(DbHelper.KEY_EXERCISE);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    exercises.add(c.getString(iExercise));
                    c.moveToNext();
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
        return exercises;
    }
}
