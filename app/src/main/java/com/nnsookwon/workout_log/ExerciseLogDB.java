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
public class ExerciseLogDB {

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDataBase;
    String[] columns;

    public ExerciseLogDB(Context c) {
        ourContext = c;
        open();
        columns = new String[]{DbHelper.KEY_ROWID, DbHelper.KEY_DATE, DbHelper.KEY_DATE_SORT,
                DbHelper.KEY_EXERCISE, DbHelper.KEY_CATEGORY, DbHelper.KEY_SETS};
        close();
    }

    public ExerciseLogDB open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDataBase = ourHelper.getWritableDatabase();

        return this;
    }

    public void close() {
        ourHelper.close();
    }

    public void createEntry(Exercise exercise) {
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.KEY_DATE, exercise.getDate());
        cv.put(DbHelper.KEY_DATE_SORT, exercise.getDateSort());
        cv.put(DbHelper.KEY_EXERCISE, exercise.getExerciseName());
        cv.put(DbHelper.KEY_CATEGORY, exercise.getCategory());
        cv.put(DbHelper.KEY_SETS, exercise.arrayListToJsonString());

        if (!dateHasExercise(exercise))
            ourDataBase.insert(DbHelper.DATABASE_TABLE_LOG_ENTRIES, null, cv);
    }

    public void updateEntry(Exercise exercise) {
        //updates sets in database via EditEntry.class

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_LOG_ENTRIES, columns,
                DbHelper.KEY_DATE + "=? and " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{exercise.getDate(), exercise.getExerciseName()},
                null, null, null, null);

        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);

        if (c.getCount() > 0) {
            c.moveToFirst();
            ContentValues cv = new ContentValues();
            cv.put(DbHelper.KEY_SETS, exercise.arrayListToJsonString());

            ourDataBase.update(DbHelper.DATABASE_TABLE_LOG_ENTRIES, cv, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
        }
    }

    public void deleteEntry(Exercise exercise) {
        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_LOG_ENTRIES, columns,
                DbHelper.KEY_DATE + "=? and " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{exercise.getDate(), exercise.getExerciseName()},
                null, null, null, null);
        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);
        if (c.getCount() > 0) {
            c.moveToFirst();
            ourDataBase.delete(DbHelper.DATABASE_TABLE_LOG_ENTRIES, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
        }
    }

    public ArrayList<Exercise> getLogEntries(String date) {
        //retrieves from database log entries on specified date

        ArrayList<Exercise> exercises = new ArrayList<Exercise>();

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_LOG_ENTRIES, columns,
                DbHelper.KEY_DATE + " = ?", new String[]{date}, null, null, null, null);

        int iDate = c.getColumnIndex(DbHelper.KEY_DATE);
        int iDateSort = c.getColumnIndex(DbHelper.KEY_DATE_SORT);
        int iExercise = c.getColumnIndex(DbHelper.KEY_EXERCISE);
        int iCategory = c.getColumnIndex(DbHelper.KEY_CATEGORY);
        int iSet = c.getColumnIndex(DbHelper.KEY_SETS);

        Exercise exercise;
        ArrayList<double[]> sets;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            exercise = new Exercise(c.getString(iDate), c.getString(iDateSort),
                    c.getString(iExercise), c.getString(iCategory));
            sets = Exercise.jsonStringToArrayList(c.getString(iSet));
            for (double[] set : sets) {
                exercise.addNewSet(set[0], set[1]);
            }
            exercises.add(exercise);
        }
        return exercises;
    }

    public ArrayList<Exercise> getExerciseHistory(String exerciseName, int start, int end) {
        //returns ArrayList of entries of particular exercise in reverse chronological order,
        //range from position start to end.
        //used to see progress in gym

        ArrayList<Exercise> exercises = new ArrayList<Exercise>();

        Cursor c = ourDataBase.rawQuery("SELECT * FROM " +
                DbHelper.DATABASE_TABLE_LOG_ENTRIES +
                " WHERE " + DbHelper.KEY_EXERCISE + " = \"" + exerciseName + "\"" +
                " ORDER BY date(" + DbHelper.KEY_DATE_SORT + ") DESC LIMIT " + (end-start) + " OFFSET " + start, null);

        int iDate = c.getColumnIndex(DbHelper.KEY_DATE);
        int iDateSort = c.getColumnIndex(DbHelper.KEY_DATE_SORT);
        int iExercise = c.getColumnIndex(DbHelper.KEY_EXERCISE);
        int iCategory = c.getColumnIndex(DbHelper.KEY_CATEGORY);
        int iSet = c.getColumnIndex(DbHelper.KEY_SETS);

        Exercise exercise;
        ArrayList<double[]> sets;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            exercise = new Exercise(c.getString(iDate), c.getString(iDateSort),
                    c.getString(iExercise), c.getString(iCategory));
            sets = Exercise.jsonStringToArrayList(c.getString(iSet));
            for (double[] set : sets) {
                exercise.addNewSet(set[0], set[1]);
            }
            exercises.add(exercise);
        }
        return exercises;
    }

    public boolean dateHasExercise(Exercise exercise) {
        //returns true if date already has that exercise logged in database

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_LOG_ENTRIES, columns,
                DbHelper.KEY_DATE + "=? AND " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{exercise.getDate(), exercise.getExerciseName()},
                null, null, null, null);

        return c.getCount() != 0;
    }

    public void addSet(Exercise exercise) {
        //pre-condition: dateHasExercise(date, exerciseName) returned true
        //updates Json String in database to add new set

        Cursor c = ourDataBase.query(DbHelper.DATABASE_TABLE_LOG_ENTRIES, columns,
                DbHelper.KEY_DATE + "=? and " + DbHelper.KEY_EXERCISE + "=?",
                new String[]{exercise.getDate(), exercise.getExerciseName()},
                null, null, null, null);

        int iRow = c.getColumnIndex(DbHelper.KEY_ROWID);
        int iSet = c.getColumnIndex(DbHelper.KEY_SETS);


        if (c.getCount() > 0) {
            c.moveToFirst();
            ArrayList<double[]> sets = Exercise.jsonStringToArrayList(c.getString(iSet));
            sets.add(exercise.getSet(0));
            exercise.removeSet(0);
            for (double[] set : sets) {
                exercise.addNewSet(set[0], set[1]);
            }

            ContentValues cv = new ContentValues();
            cv.put(DbHelper.KEY_SETS, exercise.arrayListToJsonString());

            ourDataBase.update(DbHelper.DATABASE_TABLE_LOG_ENTRIES, cv, DbHelper.KEY_ROWID + "=" + c.getString(iRow), null);
        }
    }

    public void deleteDataBase() {
        ourContext.deleteDatabase(DbHelper.DATABASE_NAME);
    }


}
