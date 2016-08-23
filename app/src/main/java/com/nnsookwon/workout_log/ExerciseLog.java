package com.nnsookwon.workout_log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by nnsoo on 8/17/2016.
 */
public class ExerciseLog {

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDataBase;
    String[] columns;

    public ExerciseLog(Context c) {
        ourContext = c;
        open();
        columns = new String[]{DbHelper.KEY_ROWID, DbHelper.KEY_DATE,
                DbHelper.KEY_EXERCISE, DbHelper.KEY_SETS};
        close();
    }

    public ExerciseLog open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDataBase = ourHelper.getWritableDatabase();

        return this;
    }

    public void close() {
        ourHelper.close();
    }

    public void createEntry(String date, String exerciseName, String sets) {
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.KEY_DATE, date);
        cv.put(DbHelper.KEY_EXERCISE, exerciseName);
        cv.put(DbHelper.KEY_SETS, sets);

        ourDataBase.insert(DbHelper.DATABASE_TABLE, null, cv);
    }

    public void updateEntry(String date, String exerciseName, String sets) {
        //updates sets in database via EditEntry.class

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE, columns,
                DbHelper.KEY_DATE + "=? and " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{date, exerciseName}, null, null, null, null);

        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);
        int iSet = c.getColumnIndex(DbHelper.KEY_SETS);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ContentValues cv = new ContentValues();
            cv.put(DbHelper.KEY_SETS, sets);

            ourDataBase.update(DbHelper.DATABASE_TABLE, cv, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
        }
    }

    public void deleteEntry(String date, String exerciseName) {
        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE, columns,
                DbHelper.KEY_DATE + "=? and " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{date, exerciseName}, null, null, null, null);
        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);
        if (c.getCount() > 0) {
            c.moveToFirst();
            ourDataBase.delete(DbHelper.DATABASE_TABLE, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
        }
    }

    public ArrayList<Exercise> getLogEntries(String date) {
        //retrieves from database log entries on specified date

        ArrayList<Exercise> exercises = new ArrayList<Exercise>();

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE, columns,
                DbHelper.KEY_DATE + " = ?", new String[]{date}, null, null, null, null);

        int iDate = c.getColumnIndex(DbHelper.KEY_DATE);
        int iExercise = c.getColumnIndex(DbHelper.KEY_EXERCISE);
        int iSet = c.getColumnIndex(DbHelper.KEY_SETS);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            Exercise exercise = new Exercise(c.getString(iDate), c.getString(iExercise));
            ArrayList<double[]> sets = Exercise.jsonStringToArrayList(c.getString(iSet));
            for (double[] set : sets) {
                exercise.addNewSet(set[0], set[1]);
            }
            exercises.add(exercise);
        }
        return exercises;
    }

    public boolean dateHasExercise(String date, String exerciseName) {
        //returns true if date already has that exercise logged in database

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE, columns,
                DbHelper.KEY_DATE + "=? AND " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{date, exerciseName}, null, null, null, null);

        return c.getCount() != 0;
    }

    public void addSet(String date, String exerciseName, double[] nSet) {
        //pre-condition: dateHasExercise(date, exerciseName) returned true
        //updates Json String in database to add new set

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE, columns,
                DbHelper.KEY_DATE + "=? and " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{date, exerciseName}, null, null, null, null);

        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);
        int iSet = c.getColumnIndex(DbHelper.KEY_SETS);

        if (c.getCount() > 0) {
            c.moveToFirst();
            Exercise exercise = new Exercise();
            ArrayList<double[]> sets = Exercise.jsonStringToArrayList(c.getString(iSet));
            sets.add(nSet);

            for (double[] set : sets) {
                exercise.addNewSet(set[0], set[1]);
            }

            ContentValues cv = new ContentValues();
            cv.put(DbHelper.KEY_SETS, exercise.arrayListToJsonString());

            ourDataBase.update(DbHelper.DATABASE_TABLE, cv, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
        }
    }

    public void deleteDataBase() {
        ourContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }


}
