package com.nnsookwon.workout_log;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nnsoo on 8/28/2016.
 */
public class ViewExerciseHistory extends AppCompatActivity {

    private ExerciseLogDB exerciseLog;
    TextView header;
    LinearLayout content;
    String exerciseName;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_exercise_history);
        header = (TextView) findViewById(R.id.exercise_history_header);
        content = (LinearLayout) findViewById(R.id.exercise_history_content);
        exerciseName = "";

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("exerciseName")) {
                exerciseName = bundle.getString("exerciseName");
            }
        }

        header.setText(exerciseName);
        exerciseLog = new ExerciseLogDB(ViewExerciseHistory.this);
        fillExerciseHistory();
    }

    public void fillExerciseHistory(){
        exerciseLog.open();
        ArrayList<Exercise> exercises = exerciseLog.getExerciseHistory(exerciseName);
        String result = "";

        TextView tv_date;
        TextView tv_sets;


        for (Exercise exercise: exercises){
            tv_date = (TextView) getLayoutInflater().inflate(R.layout.log_entry_exercise_name_template, null);
            tv_sets = (TextView) getLayoutInflater().inflate(R.layout.log_entry_exercise_sets_template, null);

            tv_date.setText(exercise.getDate());
            tv_sets.setText(exercise.getSetInfo()+"\n");
            content.addView(tv_date);
            content.addView(tv_sets);

        }

        exerciseLog.close();
    }

}
