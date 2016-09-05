package com.nnsookwon.workout_log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by nnsoo on 8/21/2016.
 */
public class EditEntry extends AppCompatActivity {

    private Exercise exercise;
    private TextView header;
    private TableLayout table;
    private ExerciseLogDB exerciseLogDB;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_entry);
        initExercise();
        exerciseLogDB = new ExerciseLogDB(getApplicationContext());
        table = (TableLayout) findViewById(R.id.edit_entry_table);
        if (table != null)
            table.setStretchAllColumns(true);
//        header = (TextView) findViewById(R.id.exercise_history_header);

        getSupportActionBar().setTitle(exercise.getExerciseName());
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
        int id_reps = 2;

        for (double[] set : exercise.getSetList()) {

            //if int overflows
            if (id_weight == Integer.MAX_VALUE || id_reps == Integer.MAX_VALUE) {
                Toast.makeText(EditEntry.this, "Unable to load all sets", Toast.LENGTH_SHORT).show();
                break;
            }

            TableRow row = new TableRow(EditEntry.this);
//
            ContextThemeWrapper themer = new ContextThemeWrapper(EditEntry.this, R.style.Edit_Entry_Weight);
            EditText et_weight = new EditText(themer);
            et_weight.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            et_weight.setId(id_weight);
            id_weight += 2;
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
            et_reps.setId(id_reps);
            id_reps += 2;
            et_reps.setText((int) set[1] + "");

            row.addView(et_weight, cellLp);
            row.addView(tv, cellLp);
            row.addView(et_reps, cellLp);
            table.addView(row, rowLp);
        }
    }

    public void deleteEntry(View v) {
        exerciseLogDB.open();
        exerciseLogDB.deleteEntry(exercise);
        exerciseLogDB.close();
        Toast.makeText(EditEntry.this, "Entry removed from workout log", Toast.LENGTH_SHORT).show();
        finished(v);
    }


    public void finished(View v) {
        exerciseLogDB.open();
        String weight;
        String reps;
        boolean error = false;

        for (int i = 1; i <= exercise.getSetList().size(); i++) {
            weight = ((EditText) findViewById(i * 2 - 1)).getText().toString();
            reps = ((EditText) findViewById(i * 2)).getText().toString();
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
                exerciseLogDB.deleteEntry(exercise);
            else
                exerciseLogDB.updateEntry(exercise);

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        exerciseLogDB.close();
    }


}
