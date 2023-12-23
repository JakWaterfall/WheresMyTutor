package com.waterfall.wheresmytutor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.account.SplashScreenActivity;

public class NoNetworkActivity extends AppCompatActivity {
    Button restartBtn, closeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);
        restartBtn = findViewById(R.id.restartBtn);
        closeBtn = findViewById(R.id.closeBtn);

        restartBtn.setOnClickListener(v -> {
            finishAffinity();
            startActivity(new Intent(NoNetworkActivity.this, SplashScreenActivity.class));
        });

        closeBtn.setOnClickListener(v -> finishAffinity());

    }
}