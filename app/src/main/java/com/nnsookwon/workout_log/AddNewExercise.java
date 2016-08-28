package com.nnsookwon.workout_log;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by nnsoo on 8/17/2016.
 */
public class AddNewExercise extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String nDate, nExerciseName, nCategory;

    private EditText et_date, et_weight, et_reps;
    private ExerciseLogDB exerciseLogDB;
    private ExercisesDB exercisesDB;

    private Spinner sp_categories;
    private Spinner sp_exercises;
    ArrayList<String> categories;
    ArrayList<String> exercises;

    ArrayAdapter<String> adapterCategories;
    ArrayAdapter<String> adapterExercises;
    SharedPreferences savedExercises;
    public static String filename = "SavedExercises";
    private static final String KEY_EXERCISES = "key_exercises";
    private boolean userIsInteracting;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_exercise);
        userIsInteracting = false;

        exerciseLogDB = new ExerciseLogDB(getApplicationContext());

        nExerciseName = "";
        nCategory = "";

        exercisesDB = new ExercisesDB(getApplicationContext());
        exercisesDB.open();
        initSpinners();

        et_date = (EditText) findViewById(R.id.et_date);
        et_weight = (EditText) findViewById(R.id.et_weight);
        et_reps = (EditText) findViewById(R.id.et_reps);

        initFields();
    }

    public void initSpinners() {
        categories = exercisesDB.getCategories();
        adapterCategories = new ArrayAdapter<String>(AddNewExercise.this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        sp_categories = (Spinner) findViewById(R.id.sp_categories);

        if (sp_categories != null) {
            sp_categories.setOnItemSelectedListener(AddNewExercise.this);
            sp_categories.setAdapter(adapterCategories);
        }

        sp_exercises = (Spinner) findViewById(R.id.sp_exercises);
        if (sp_exercises != null) {
            sp_exercises.setOnItemSelectedListener(AddNewExercise.this);
            updateExerciseSpinner();
        }
    }

    public void updateExerciseSpinner() {
        exercises = exercisesDB.getExercises(nCategory);
        adapterExercises = new ArrayAdapter<String>(AddNewExercise.this,
                android.R.layout.simple_spinner_dropdown_item, exercises);
        sp_exercises.setAdapter(adapterExercises);
    }

    public void onItemSelected(AdapterView<?> av, View v, int n, long l) {
        if (userIsInteracting) {
            int pos;
            switch (av.getId()) {
                case R.id.sp_categories:
                    pos = sp_categories.getSelectedItemPosition();
                    nCategory = categories.get(pos);
                    updateExerciseSpinner();
                    break;
                case R.id.sp_exercises:
                    pos = sp_exercises.getSelectedItemPosition();
                    if (pos == 1)
                        addOption();
                    else
                        nExerciseName = exercises.get(pos);
                    break;
            }
        }
    }
    public void onNothingSelected(AdapterView<?> av) {

    }

    public void addOption() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewExercise.this);
        builder.setTitle("Add New Exercise");
        builder.setMessage("Enter new exercise: ");
        final EditText input = new EditText(AddNewExercise.this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setSingleLine();
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int whichButton) {
                String newExercise = input.getText().toString().trim().replaceAll("\\s+", " ");
                //removes leading and trailing spaces, and multiple spaces in between

                if (newExercise.length() > 0) {
                    //input must be more than just spaces

                    if (!exercisesDB.createEntry(nCategory, newExercise))
                        Toast.makeText(AddNewExercise.this, "Exercise already exists", Toast.LENGTH_SHORT).show();

                    updateExerciseSpinner();
                    sp_exercises.setSelection(exercises.indexOf(newExercise));
                } else
                    sp_exercises.setSelection(0);
                d.dismiss();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int whichButton) {
                sp_exercises.setSelection(0);
                d.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface d) {
                sp_exercises.setSelection(0);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void initFields() {
        calendar = Calendar.getInstance();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("year") &&
                    bundle.containsKey("month") &&
                    bundle.containsKey("day"))
                calendar.set(bundle.getInt("year"), bundle.getInt("month"), bundle.getInt("day"));

            if (bundle.containsKey("category")) {
                nCategory = bundle.getString("category");
                sp_categories.setSelection(categories.indexOf(nCategory));
                updateExerciseSpinner();
            }
            if (bundle.containsKey("exerciseName")) {
                nExerciseName = bundle.getString("exerciseName");
                int pos = exercises.indexOf(nExerciseName);
                sp_exercises.setSelection(pos,true);
                Log.e("TAG",""+sp_exercises.getSelectedItem().toString().equals(nExerciseName));
                Log.e("TAG",""+sp_exercises.getSelectedItem().toString());

            }

        }
        nDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(calendar.getTime());
        et_date.setText(nDate);
    }


    public void chooseDate(View v) {
        //generate DatePickerDialog to choose date for log entry
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePicker = new DatePickerDialog(AddNewExercise.this,
                new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int y, int m, int d) {

                        calendar.set(y, m, d);
                        nDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(calendar.getTime());
                        et_date.setText(nDate);
                    }
                }, year, month, day);
        mDatePicker.setTitle("Select date");
        mDatePicker.show();
    }

    public void addToLog(View v) {

        if (et_reps.getText().toString().equals("0")) {
            //prevent user from adding a set of 0 reps
            Toast.makeText(AddNewExercise.this, "Please enter valid number of reps", Toast.LENGTH_SHORT).show();

        } else if (!(et_date.getText().toString().isEmpty() ||
                sp_categories.getSelectedItem().toString().isEmpty() ||
                sp_exercises.getSelectedItem().toString().isEmpty() ||
                et_weight.getText().toString().isEmpty() ||
                et_reps.getText().toString().isEmpty())) {
            //user can only create entry if all fields are filled

            Exercise exercise = new Exercise(nDate, nExerciseName, nCategory);
            exercise.addNewSet(Double.parseDouble(et_weight.getText().toString()),
                    Double.parseDouble(et_reps.getText().toString()));

            exerciseLogDB.open();
            if (exerciseLogDB.dateHasExercise(exercise)) {
                exerciseLogDB.addSet(exercise);
            } else {
                exerciseLogDB.createEntry(exercise);
            }
            exerciseLogDB.close();

            Toast.makeText(AddNewExercise.this, "Added to log", Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(AddNewExercise.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();

    }

    public void exitAddNewExercise(View v) {
        //return back to MyApplication
        exercisesDB.close();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    public void onBackPressed(){
        exercisesDB.close();
        super.onBackPressed();
    }
}




