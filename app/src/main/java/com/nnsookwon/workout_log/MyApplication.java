package com.nnsookwon.workout_log;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends AppCompatActivity {

    private TextView log_date, log_exercise;


    private Button addNewExercise;

    private ExerciseLogDB exerciseLogDB;
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

        exerciseLogDB = new ExerciseLogDB(getApplicationContext());

        fillLogEntry(getFormattedDate(calendar), log_date);
        ScrollView sv = (ScrollView) findViewById(R.id.scroll);
        sv.setOnTouchListener(new SwipeGestureListener(MyApplication.this) {
            public void onSwipeLeft() {
                backDate(null);
            }

            public void onSwipeRight() {
                forwardDate(null);
            }
        });
    }

    public String getFormattedDate(Calendar cal) {

        return new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(cal.getTime());

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
                        String nDate = (new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(calendar.getTime()));

                        fillLogEntry(nDate, log_date);
                    }
                }, year, month, day);

        mDatePicker.setTitle("Jump to date:");
        mDatePicker.show();
    }

    public ExerciseLogDB getExerciseLogDB() {
        return exerciseLogDB;
    }

    public void fillLogEntry(String date, TextView tv_date) {
        //fills logEntrySpace to be displayed on main application view

        logEntrySpace.removeAllViews();
        exerciseLogDB.open();
        ArrayList<Exercise> exercises = exerciseLogDB.getLogEntries(date);
        exerciseLogDB.close();
        tv_date.setText(date);
        int id_exercise = 1;
        int id_options = 100001;

        TextView tv_exerciseSets;
        TextView tv_exerciseName;
        LinearLayout.LayoutParams llParams;
        LinearLayout name_sets;
        ImageButton b_menu_options;

        for (Exercise exercise : exercises) {

            LogEntry entry = (LogEntry) getLayoutInflater().inflate(R.layout.log_entry_template, null);
            entry.setId(id_exercise++);
            entry.setExercise(exercise);

            tv_exerciseSets = (TextView) getLayoutInflater().inflate(R.layout.log_entry_exercise_sets_template, null);
            tv_exerciseName = (TextView) getLayoutInflater().inflate(R.layout.log_entry_exercise_name_template, null);
            tv_exerciseName.setText(exercise.getExerciseName());
            tv_exerciseSets.setText(exercise.getSetInfo());

            llParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            name_sets = new LinearLayout(MyApplication.this);
            name_sets.setLayoutParams(llParams);
            name_sets.setOrientation(LinearLayout.VERTICAL);
            name_sets.setPadding(8, 0, 32, 0);

            name_sets.addView(tv_exerciseName);
            name_sets.addView(tv_exerciseSets);

            b_menu_options = (ImageButton) getLayoutInflater().inflate(R.layout.menu_options_template, null);
            b_menu_options.setId(id_options++);

            llParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            b_menu_options.setLayoutParams(llParams);

            llParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            entry.setLayoutParams(llParams);
            entry.addView(name_sets);
            entry.addView(b_menu_options);

            llParams.setMargins(0, 16, 0, 16);
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

    public void menuItemClicked(View v, MenuItem item) {
        int id_exercise = (v.getId() - 100000);
        LogEntry entry = (LogEntry) findViewById(id_exercise);
        Bundle bundle = new Bundle();
        bundle.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
        bundle.putInt("month", calendar.get(Calendar.MONTH));
        bundle.putInt("year", calendar.get(Calendar.YEAR));
        if (entry != null && entry.getExercise() != null) {
            bundle.putString("exerciseName", entry.getExercise().getExerciseName());
            bundle.putString("date", entry.getExercise().getDate());
            bundle.putString("category", entry.getExercise().getCategory());
            bundle.putString("sets", entry.getExercise().arrayListToJsonString());
        }

        Intent intent;
        switch (item.getItemId()) {
            case R.id.b_addMore:
                intent = new Intent(MyApplication.this, AddNewExercise.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                break;
            case R.id.b_edit:
                intent = new Intent(MyApplication.this, EditEntry.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                break;
            case R.id.b_view_history:
                intent = new Intent(MyApplication.this, ViewExerciseHistory.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                break;
        }
    }

    public void showMenuOptions(View v) {
        final View view = v;
        PopupMenu popupMenu = new PopupMenu(MyApplication.this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.b_addMore:
                    case R.id.b_edit:
                    case R.id.b_view_history:
                        menuItemClicked(view,item);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            fillLogEntry(getFormattedDate(calendar), log_date);
        }
    }
}
