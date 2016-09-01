package com.nnsookwon.workout_log;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by nnsoo on 8/17/2016.
 */
public class AddNewExercise extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String date, dateSort, exerciseName, category;

    private EditText et_date, et_weight, et_reps;
    private ExerciseLogDB exerciseLogDB;
    private ExercisesDB exercisesDB;

    private boolean hasChangedWeight, hasChangedReps;
    private double weight, incrementWeight;
    private int reps, incrementRep;

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

        getSupportActionBar().setTitle("Add New Exercise");

        exerciseLogDB = new ExerciseLogDB(getApplicationContext());
        exerciseLogDB.open();

        exerciseName = "";
        category = "";

        exercisesDB = new ExercisesDB(getApplicationContext());
        exercisesDB.open();
        initSpinners();

        hasChangedWeight = hasChangedReps = false;
        weight = reps = 0;


        //increase/decrease by this much each time plus/minus button pressed
        incrementWeight = 5;
        incrementRep = 1;

        et_date = (EditText) findViewById(R.id.et_date);
        et_weight = (EditText) findViewById(R.id.et_weight);
        et_reps = (EditText) findViewById(R.id.et_reps);

        initETfields();
        initFields();
    }

    public void initETfields() {
        et_weight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hasChangedWeight = true;
                return false;
            }
        });

        et_reps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hasChangedReps = true;
                return false;
            }
        });
    }

    public void initSpinners() {
        categories = exercisesDB.getCategories();
        adapterCategories = new ArrayAdapter<String>(AddNewExercise.this,
                android.R.layout.simple_spinner_item, categories);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        exercises = exercisesDB.getExercises(category);
        adapterExercises = new ArrayAdapter<String>(AddNewExercise.this,
                android.R.layout.simple_spinner_item, exercises);
        adapterExercises.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_exercises.setAdapter(adapterExercises);
    }

    public void onItemSelected(AdapterView<?> av, View v, int n, long l) {
        if (userIsInteracting) {
            int pos;
            switch (av.getId()) {
                case R.id.sp_categories:
                    pos = sp_categories.getSelectedItemPosition();
                    category = categories.get(pos);
                    updateExerciseSpinner();
                    sp_exercises.performClick();
                    break;
                case R.id.sp_exercises:
                    pos = sp_exercises.getSelectedItemPosition();
                    if (pos == 1)
                        addOption();
                    else {
                        exerciseName = exercises.get(pos);
                        motivate();
                    }
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

                    if (!exercisesDB.createEntry(category, newExercise))
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
                category = bundle.getString("category");
                sp_categories.setSelection(categories.indexOf(category));
                updateExerciseSpinner();
            }
            if (bundle.containsKey("exerciseName")) {
                exerciseName = bundle.getString("exerciseName");
                int pos = exercises.indexOf(exerciseName);
                sp_exercises.setSelection(pos, true);
                Log.e("TAG", "" + sp_exercises.getSelectedItem().toString().equals(exerciseName));
                Log.e("TAG", "" + sp_exercises.getSelectedItem().toString());

            }

        }
        date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(calendar.getTime());
        dateSort = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
        et_date.setText(date);
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
                        date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(calendar.getTime());
                        dateSort = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
                        et_date.setText(date);
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

            Exercise exercise = new Exercise(date, dateSort, exerciseName, category);
            exercise.addNewSet(Double.parseDouble(et_weight.getText().toString()),
                    Double.parseDouble(et_reps.getText().toString()));


            if (exerciseLogDB.dateHasExercise(exercise)) {
                exerciseLogDB.addSet(exercise);
            } else {
                exerciseLogDB.createEntry(exercise);
            }

            Toast.makeText(AddNewExercise.this, "Added to log", Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(AddNewExercise.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();

    }

    public void incrementWeight(View v) {
        if (hasChangedWeight) {
            try {
                weight = Double.parseDouble(et_weight.getText().toString());
            } catch (NumberFormatException e) {
                Log.d("Parse Exception", "Field blank");
            }
        }else
            hasChangedWeight=false;
        weight += incrementWeight;
        et_weight.setText(String.valueOf(weight));
    }

    public void decrementWeight(View v) {
        if (hasChangedWeight) {
            try {
                weight = Double.parseDouble(et_weight.getText().toString());
            } catch (NumberFormatException e) {
                Log.d("Parse Exception", "Field blank");
            }
        }else
            hasChangedWeight=false;
        weight -= incrementWeight;
        weight = weight < 0 ? 0: weight;
        et_weight.setText(String.valueOf(weight));
    }

    public void incrementRep(View v) {
        if (hasChangedReps) {
            try {
                reps = Integer.parseInt(et_reps.getText().toString());
            } catch (NumberFormatException e) {
                Log.d("Parse Exception", "Field blank");
            }
        }else
            hasChangedReps=false;
        reps += incrementRep;
        et_reps.setText(String.valueOf(reps));
    }

    public void decrementRep(View v) {
        if (hasChangedReps) {
            try {
                reps = Integer.parseInt(et_reps.getText().toString());
                Log.e("Clicked on field","Yes");
            } catch (NumberFormatException e) {
                Log.d("Parse Exception", "Field blank");
            }
        }else
            hasChangedReps=false;
        reps -= incrementRep;
        reps = reps < 0 ? 0: reps;
        et_reps.setText(String.valueOf(reps));
    }

    public void motivate() {
        try {
            ArrayList<Exercise> exercises = exerciseLogDB.getExerciseHistory(exerciseName, 0, 2);
            Exercise exercise = exercises.get(0);
            if (exercise.getDate().equals(date))
                exercise = exercises.get(1);
            //Don't show today's entry, get the previous workout

            Toast toast = Toast.makeText(AddNewExercise.this, "", Toast.LENGTH_LONG);

            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            String header = getString(R.string.motivator);
            stringBuilder.append(header);
            stringBuilder.setSpan(new UnderlineSpan(), 0, header.length(), 0);
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, header.length(), 0);
            stringBuilder.append("\n").append( exercise.getSetInfo());

            TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);

            if (tv != null) {
                tv.setText(stringBuilder);

                tv.setGravity(Gravity.RIGHT);
            }
            toast.show();
        } catch (IndexOutOfBoundsException e) {
            Log.d("IndexOutOfBounds", "exercise not found in database");
        }

    }

    public void exitAddNewExercise(View v) {
        //return back to MainActivity
        exerciseLogDB.close();
        exercisesDB.close();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    public void onPause() {
        exerciseLogDB.close();
        exercisesDB.close();
        super.onPause();
    }
}




