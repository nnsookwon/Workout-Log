package com.nnsookwon.workout_log;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by nnsoo on 9/1/2016.
 */
public class ManageExercises extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner sp_categories, sp_exercises;
    private ArrayList<String> categories, exercises;
    private ArrayAdapter<String> adapterCategories, adapterExercises;
    private ExercisesDB exercisesDB;
    private String category, exerciseName;

    private boolean userIsInteracting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_exercises);

        getSupportActionBar().setTitle("Manage Exercises");
        userIsInteracting = false;
        sp_categories = (Spinner) findViewById(R.id.manage_exercises_sp_categories);
        sp_exercises = (Spinner) findViewById(R.id.manage_exercises_sp_exercises);

        exercisesDB = new ExercisesDB(getApplicationContext());
        exercisesDB.open();
        category = "";
        exerciseName = "";
        initSpinners();

    }

    @Override
    public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
        if (userIsInteracting) {
            int pos;
            switch (av.getId()) {
                case R.id.manage_exercises_sp_categories:
                    pos = sp_categories.getSelectedItemPosition();
                    category = categories.get(pos);
                    updateExerciseSpinner();
                    sp_exercises.performClick();

                    break;
                case R.id.manage_exercises_sp_exercises:
                    pos = sp_exercises.getSelectedItemPosition();
                    exerciseName = exercises.get(pos);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void initSpinners() {
        categories = exercisesDB.getCategories();
        adapterCategories = new ArrayAdapter<String>(ManageExercises.this,
                android.R.layout.simple_spinner_item, categories);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_categories = (Spinner) findViewById(R.id.manage_exercises_sp_categories);

        if (sp_categories != null) {
            sp_categories.setOnItemSelectedListener(ManageExercises.this);
            sp_categories.setAdapter(adapterCategories);
        }

        sp_exercises = (Spinner) findViewById(R.id.manage_exercises_sp_exercises);
        if (sp_exercises != null) {
            sp_exercises.setOnItemSelectedListener(ManageExercises.this);
            updateExerciseSpinner();
        }
    }

    public void updateExerciseSpinner() {
        exercises = exercisesDB.getExercises(category);
        adapterExercises = new ArrayAdapter<String>(ManageExercises.this,
                android.R.layout.simple_spinner_item, exercises);
        if (exercises.size() > 1) {
            exercises.remove("Other");
        }
        //remove "Other" option

        adapterExercises.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_exercises.setAdapter(adapterExercises);
    }

    public void removeExercise(View v) {
        if (fieldsFilledOut())
            exercisesDB.deleteEntry(exerciseName);
        updateExerciseSpinner();
        sp_exercises.setSelection(0);
    }

    public void addExercise(View v) {

        if (!category.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ManageExercises.this);
            builder.setTitle("Add New Exercise");
            builder.setMessage("Enter new exercise: ");
            final EditText input = new EditText(ManageExercises.this);
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
                            Toast.makeText(ManageExercises.this, "Exercise already exists", Toast.LENGTH_SHORT).show();

                        updateExerciseSpinner();
                        sp_exercises.setSelection(exercises.indexOf(newExercise));
                        exerciseName = newExercise;
                    }
                    d.dismiss();
                }
            });

            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int whichButton) {
                    d.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }
    }

    public boolean fieldsFilledOut() {
        //returns true if all fields are filled

        if (category.isEmpty() || exerciseName.isEmpty()) {
            Toast.makeText(ManageExercises.this,
                    "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    public void onPause(){
        exercisesDB.close();
        super.onPause();
    }
}
