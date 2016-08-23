package com.nnsookwon.workout_log;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MyApplication extends AppCompatActivity {

    private TextView log_date, log_exercise;


    private Button addNewExercise;

    public ExerciseLog exerciseLog;
    Calendar calendar;
    LinearLayout logEntrySpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_application);

        logEntrySpace = (LinearLayout) findViewById(R.id.log_entries);
        calendar = Calendar.getInstance();
        log_date = (TextView) findViewById(R.id.date);

        log_date.setText(getFormattedDate(calendar));

        exerciseLog = new ExerciseLog(getApplicationContext());

        //log_exercise = (TextView)findViewById(R.id.log_exercise);
        fillLogEntry(getFormattedDate(calendar), log_date);
    }

    public String getFormattedDate(Calendar cal) {

        return new SimpleDateFormat("MMMM dd, yyyy").format(cal.getTime());

    }

    public void forwardDate(View v) {
        calendar.add(Calendar.DATE, 1);
        fillLogEntry(getFormattedDate(calendar), log_date);
    }

    public void backDate(View v) {
        calendar.add(Calendar.DATE, -1);

        fillLogEntry(getFormattedDate(calendar), log_date);
    }

    public void jumpToDate(View v) {
        //to quickly jump to a date on calendar

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(MyApplication.this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int y, int m, int d) {
                        calendar.set(y, m, d);
                        String nDate = (new SimpleDateFormat("MMMM dd, yyyy").format(calendar.getTime()));

                        fillLogEntry(nDate, log_date);
                    }
                }, year, month, day);

        mDatePicker.setTitle("Jump to date:");
        mDatePicker.show();
    }

    public ExerciseLog getExerciseLog() {
        return exerciseLog;
    }

    public void fillLogEntry(String date, TextView tv_date) {
        //fills logEntrySpace to be displayed on main application view

        logEntrySpace.removeAllViews();
        exerciseLog.open();
        ArrayList<Exercise> exercises = exerciseLog.getLogEntries(date);
        exerciseLog.close();
        tv_date.setText(date);
        int id_exercise = 1;
        int id_edit = 10001;

        for (Exercise exercise : exercises) {
            ContextThemeWrapper themeSetter = new ContextThemeWrapper(MyApplication.this, R.style.Log_Entry);
            LogEntry entry = (LogEntry) getLayoutInflater().inflate(R.layout.log_entry_template, null);
            entry.setId(id_exercise++);
            entry.setExercise(exercise);

            ImageButton b_editEntry = (ImageButton) getLayoutInflater().inflate(R.layout.edit_entry_template, entry, false);
            b_editEntry.setId(id_edit++);
            TextView tv_exerciseSets = (TextView) getLayoutInflater().inflate(R.layout.log_entry_exercise_sets_template, null);
            TextView tv_exerciseName = (TextView) getLayoutInflater().inflate(R.layout.log_entry_exercise_name_template, null);

            tv_exerciseName.setText(exercise.getExerciseName());
            tv_exerciseSets.setText(exercise.getSetInfo());

            LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            entry.addView(b_editEntry);
            entry.addView(tv_exerciseName);
            entry.addView(tv_exerciseSets);

            llParams.setMargins(0, 24, 0, 24);
            logEntrySpace.addView(entry, llParams);
        }
    }

    public void goToAddExercise(View v) {
        //go to AddNewExercise.class

        Bundle bundle = new Bundle();
        bundle.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
        bundle.putInt("month", calendar.get(Calendar.MONTH));
        bundle.putInt("year", calendar.get(Calendar.YEAR));
        Intent intent = new Intent(this, AddNewExercise.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }
    public void goToEditEntry(View v) {
        //go to EditExercise.class

        int id_exercise = (int) (v.getId() - 10000);
        LogEntry entry = (LogEntry) findViewById(id_exercise);

        Intent intent = new Intent(MyApplication.this, EditEntry.class);
        Bundle bundle = new Bundle();
        bundle.putString("date", entry.getExercise().getDate());
        bundle.putString("exerciseName", entry.getExercise().getExerciseName());
        bundle.putString("sets", entry.getExercise().arrayListToJsonString());
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                fillLogEntry(getFormattedDate(calendar), log_date);
            }
        }
    }
}
