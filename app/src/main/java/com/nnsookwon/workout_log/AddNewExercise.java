package com.nnsookwon.workout_log;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by nnsoo on 8/17/2016.
 */
public class AddNewExercise extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String nDate;
    private String nExercise;

    private EditText et_date, et_weight, et_reps;
    private Button b_addToLog, b_exitOut;
    public ExerciseLog exerciseLog;

    private Spinner sp_exerciseOptions;
    ArrayList<String> exercises = new ArrayList<String>();
    SharedPreferences savedExercises;
    public static String filename = "SavedExercises";
    private static final String KEY_EXERCISES = "key_exercises";
    ArrayAdapter<String> adapter;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_exercise);


        exerciseLog = new ExerciseLog(getApplicationContext());
        savedExercises = getSharedPreferences(filename, Context.MODE_PRIVATE);

        if (savedExercises.contains(KEY_EXERCISES))
            loadExerciseList();
        if (exercises.size() == 0)
            initExercises();

        et_date = (EditText) findViewById(R.id.et_date);
        et_weight = (EditText) findViewById(R.id.et_weight);
        et_reps = (EditText) findViewById(R.id.et_reps);
        b_addToLog = (Button) findViewById(R.id.b_addToLog);
        b_exitOut = (Button) findViewById(R.id.b_exitAddNewExercise);


        adapter = new ArrayAdapter<String>(AddNewExercise.this,
                android.R.layout.simple_spinner_dropdown_item, exercises);
        sp_exerciseOptions = (Spinner) findViewById(R.id.sp_exercises);
        sp_exerciseOptions.setAdapter(adapter);
        sp_exerciseOptions.setOnItemSelectedListener(AddNewExercise.this);

        initCalendar();
    }

    public void loadExerciseList() {
        Gson gson = new Gson();
        String json = savedExercises.getString(KEY_EXERCISES, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        exercises = gson.fromJson(json, type);
    }

    public void saveExercistList() {
        SharedPreferences.Editor editor = savedExercises.edit();
        Gson gson = new Gson();

        String json = gson.toJson(exercises);

        editor.putString(KEY_EXERCISES, json);
        editor.commit();
    }

    public void initExercises() {
        exercises.add("");
        exercises.add("Other");
        exercises.add("Flat Bench Press");
        exercises.add("Incline DB Press");
        exercises.add("Lateral DB Raises");
        exercises.add("DB Shoulder Press");
        exercises.add("Bicep Curls");
        exercises.add("DB Shrugs");


    }

    public void onItemSelected(AdapterView<?> av, View v, int n, long l) {
        int pos = sp_exerciseOptions.getSelectedItemPosition();
        if (pos == 1) {
            addOption();
        }
        pos = sp_exerciseOptions.getSelectedItemPosition(); //in case "other" was selected

        nExercise = exercises.get(pos);
    }

    public void onNothingSelected(AdapterView<?> av) {

    }

    public void addOption() {
        //add option to list of exercises

        View view = AddNewExercise.this.getLayoutInflater().inflate(
                R.layout.add_new_exercise, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewExercise.this);
        builder.setTitle("Add New Exercise");
        builder.setMessage("Enter new exercise: ");
        final EditText input = new EditText(AddNewExercise.this);
        input.setSingleLine();
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int whichButton) {
                if (!input.getText().toString().isEmpty()) {
                    exercises.add(input.getText().toString());
                    sp_exerciseOptions.setSelection(exercises.size() - 1);
                    saveExercistList();
                } else
                    sp_exerciseOptions.setSelection(0);
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int whichButton) {
                d.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void initCalendar() {
        calendar = Calendar.getInstance();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            calendar.set(bundle.getInt("year"), bundle.getInt("month"), bundle.getInt("day"));
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

        } else if (!et_date.getText().toString().isEmpty() &&
                !sp_exerciseOptions.getSelectedItem().toString().isEmpty() &&
                !et_weight.getText().toString().isEmpty() &&
                !et_reps.getText().toString().isEmpty()) {
            //user can only create entry if all fields are filled

            Exercise exercise = new Exercise(nDate, nExercise);
            exercise.addNewSet(Double.parseDouble(et_weight.getText().toString()),
                    Double.parseDouble(et_reps.getText().toString()));

            exerciseLog.open();
            if (exerciseLog.dateHasExercise(nDate, nExercise)) {
                exerciseLog.addSet(nDate, nExercise, exercise.getSet(0));
            } else {
                exerciseLog.createEntry(exercise.getDate(), exercise.getExerciseName(), exercise.arrayListToJsonString());
            }
            exerciseLog.close();

            Toast.makeText(AddNewExercise.this, "Added to log", Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(AddNewExercise.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
    }

    public void exitAddNewExercise(View v) {
        //return back to MyApplication
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}




