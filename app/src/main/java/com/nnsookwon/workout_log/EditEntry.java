package com.nnsookwon.workout_log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by nnsoo on 8/21/2016.
 */
public class EditEntry extends AppCompatActivity {

    private Exercise exercise;
    private TextView header;
    private TableLayout table;
    private ExerciseLog exerciseLog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_entry);
        initExercise();
        exerciseLog = new ExerciseLog(getApplicationContext());
        table = (TableLayout) findViewById(R.id.table);
        table.setStretchAllColumns(true);
        header = (TextView) findViewById(R.id.tv_header);
        header.setText(exercise.getExerciseName());
        populateETfields();
    }

    public void initExercise() {
        exercise = new Exercise();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            exercise.setDate(bundle.getString("date"));
            exercise.setExerciseName(bundle.getString("exerciseName"));
            for (double[] set : Exercise.jsonStringToArrayList(bundle.getString("sets"))) {
                exercise.addNewSet(set[0], set[1]);
            }
        }
    }

    public void populateETfields() {

        table.removeAllViews();
        TableLayout.LayoutParams rowLp = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f);
        TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f);

        int id_weight = 1;
        int id_reps = 10001;

        for (double[] set : exercise.getSetList()) {
            TableRow row = new TableRow(EditEntry.this);
//
            ContextThemeWrapper themer = new ContextThemeWrapper(EditEntry.this, R.style.Edit_Entry_Weight);
            EditText et_weight = new EditText(themer);
            et_weight.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            et_weight.setId(id_weight++);
            et_weight.setText(set[0] + "");

            themer.setTheme(R.style.Edit_Entry_Mid_Col);
            TextView tv = new TextView(themer);
            tv.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            tv.setText("  lb.   X");

            themer.setTheme(R.style.Edit_Entry_Reps);
            EditText et_reps = new EditText(themer);
            et_reps.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            et_reps.setId(id_reps++);
            et_reps.setText((int) set[1] + "");

            row.addView(et_weight, cellLp);
            row.addView(tv, cellLp);
            row.addView(et_reps, cellLp);
            table.addView(row, rowLp);
        }
    }

    public void deleteEntry(View v) {
        exerciseLog.open();
        exerciseLog.deleteEntry(exercise.getDate(), exercise.getExerciseName());
        exerciseLog.close();
        Toast.makeText(EditEntry.this, "Entry removed from workout log", Toast.LENGTH_SHORT).show();
        finished(v);
    }


    public void finished(View v) {
        exerciseLog.open();
        String weight;
        String reps;
        boolean error = false;

        for (int i = 1; i <= exercise.getSetList().size(); i++) {
            weight = ((EditText) findViewById(i)).getText().toString();
            reps = ((EditText) findViewById(i + 10000)).getText().toString();
            if (weight.isEmpty() && reps.isEmpty()) {
                //if weight and reps 0, delete set from entry
                exercise.removeSet(i - 1);

            } else if (weight.isEmpty() || reps.isEmpty()) {
                //prevent user from submitting incomplete fields

                error = true;
                Toast.makeText(EditEntry.this,
                        "Please fill out all fields. \nOr leave both weight and sets blank to remove row",
                        Toast.LENGTH_LONG).show();
            } else {
                exercise.updateSet(i - 1,
                        Double.parseDouble(weight),
                        Double.parseDouble(reps));
            }
        }

        if (!error) {
            //if no errors, complete update and return to main
            if (exercise.getSetList().size() == 0)
                exerciseLog.deleteEntry(exercise.getDate(), exercise.getExerciseName());
            else
                exerciseLog.updateEntry(exercise.getDate(), exercise.getExerciseName(), exercise.arrayListToJsonString());

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        exerciseLog.close();
    }


}
