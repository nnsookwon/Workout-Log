package com.nnsookwon.workout_log;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by nnsoo on 8/17/2016.
 */
public class LogEntry extends LinearLayout {

    private Exercise exercise;

    public LogEntry(Context context, Exercise ex) {
        super(context);
        exercise = ex;
    }

    public LogEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LogEntry(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise ex) {
        exercise = ex;

    }
}
