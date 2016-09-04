package com.nnsookwon.workout_log;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView log_date;
    private ExerciseLogDB exerciseLogDB;
    private Calendar calendar;
    private LinearLayout logEntrySpace;
    private String dateToday, dateYesterday, dateTomorrow, oldestEntry;
    private boolean skipDate;
    private SimpleDateFormat simpleDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        exerciseLogDB = new ExerciseLogDB(getApplicationContext());
        simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        refreshDateStrings();
        logEntrySpace = (LinearLayout) findViewById(R.id.main_activity_log_entries);
        calendar = Calendar.getInstance();
        log_date = (TextView) findViewById(R.id.main_activity_date);

        log_date.setText(getShortDate(calendar));

        fillLogEntry(getFormattedDate(calendar), log_date);
        ScrollView sv = (ScrollView) findViewById(R.id.main_activity_scroll);
        sv.setOnTouchListener(new SwipeGestureListener(MainActivity.this) {
            public void onSwipeLeft() {
                backDate(null);
            }

            public void onSwipeRight() {
                forwardDate(null);
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.b_manage_exercises:
                Intent intent = new Intent(MainActivity.this, ManageExercises.class);
                startActivity(intent);
                break;

            case R.id.b_skipDates:
                item.setChecked(!item.isChecked());
                skipDate = item.isChecked();
                break;
        }
        return true;
    }

    public String getFormattedDate(Calendar cal) {

        return simpleDateFormat.format(cal.getTime());
    }

    public String getShortDate(Calendar cal) {
        //returns formatted date without the year
        String str = getFormattedDate(cal);
        return str.substring(0, str.length() - 6);
    }

    public void refreshDateStrings() {
        Calendar cal = Calendar.getInstance();
        dateToday = getFormattedDate(cal);
        cal.add(Calendar.DATE, 1);
        dateTomorrow = getFormattedDate(cal);
        cal.add(Calendar.DATE, -2);
        dateYesterday = getFormattedDate(cal);
        exerciseLogDB.open();
        oldestEntry = exerciseLogDB.getOldestEntryDate();
        exerciseLogDB.close();
    }

    public void forwardDate(View v) {
        try {
            if (skipDate) {
                if (simpleDateFormat.parse(getFormattedDate(calendar)).before(simpleDateFormat.parse(dateToday)))
                {
                    calendar.add(Calendar.DATE, 1);
                    if (fillLogEntry(getFormattedDate(calendar), log_date) == 0)
                        forwardDate(null);
                }
            }
            else{
                calendar.add(Calendar.DATE, 1);
                fillLogEntry(getFormattedDate(calendar),log_date);
            }
        }catch (ParseException e){
            Log.e("Parse Exception","String cannot be formatted");
        }
    }

    public void backDate(View v) {
        try {
            if (skipDate) {
                if (simpleDateFormat.parse(getFormattedDate(calendar)).after(simpleDateFormat.parse(oldestEntry)))
                {
                    calendar.add(Calendar.DATE, -1);
                    if (fillLogEntry(getFormattedDate(calendar), log_date) == 0)
                        backDate(null);
                }
            }
            else{
                calendar.add(Calendar.DATE, -1);
                fillLogEntry(getFormattedDate(calendar),log_date);
            }
        }catch (ParseException e){
            Log.e("Parse Exception","String cannot be formatted");
        }
    }



    public void jumpToDate(View v) {
        //to quickly jump to a date on calendar

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(MainActivity.this,
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

    public int fillLogEntry(String date, TextView tv_date) {
        //fills logEntrySpace to be displayed on main application view and returns number of entries
        int n = 0;
        logEntrySpace.removeAllViews();
        exerciseLogDB.open();
        ArrayList<Exercise> exercises = exerciseLogDB.getLogEntries(date);
        exerciseLogDB.close();

        if (date.equals(dateToday))
            tv_date.setText("Today");
        else if (date.equals(dateTomorrow))
            tv_date.setText("Tomorrow");
        else if (date.equals(dateYesterday))
            tv_date.setText("Yesterday");
        else
            tv_date.setText(getShortDate(calendar));
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
            name_sets = new LinearLayout(MainActivity.this);
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
            n++;
        }
        return n;
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
                intent = new Intent(MainActivity.this, AddNewExercise.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                break;
            case R.id.b_edit:
                intent = new Intent(MainActivity.this, EditEntry.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                break;
            case R.id.b_view_history:
                intent = new Intent(MainActivity.this, ViewExerciseHistory.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                break;
        }
    }

    public void showMenuOptions(View v) {
        final View view = v;
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.b_addMore:
                    case R.id.b_edit:
                    case R.id.b_view_history:
                        menuItemClicked(view, item);
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();

    }

    public void onResume() {
        //updates the date Strings in case user comes back the next day
        super.onResume();
        refreshDateStrings();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            fillLogEntry(getFormattedDate(calendar), log_date);
        }
    }
}
