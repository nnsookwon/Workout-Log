package com.nnsookwon.workout_log;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {
    //intro image to be displayed only for as long as application loads

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MyApplication.class);
        startActivity(intent);
        finish();

    }
}
