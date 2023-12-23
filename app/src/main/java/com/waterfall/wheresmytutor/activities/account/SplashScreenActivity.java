package com.waterfall.wheresmytutor.activities.account;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.MainActivity;
import com.waterfall.wheresmytutor.services.NetworkService;
import com.waterfall.wheresmytutor.utils.DatabaseController;

public class SplashScreenActivity extends AppCompatActivity {
    private final int TIME_DELAY_MILLI = 1000;
    private FirebaseAuth mAuth;
    private DatabaseController db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(SplashScreenActivity.this);
        startService(new Intent(SplashScreenActivity.this, NetworkService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(() -> {
            if(mAuth.getCurrentUser() == null) {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                return;
            }

            db.getUserType(mAuth.getCurrentUser().getUid(), userType -> {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.putExtra(getString(R.string.usertype_intent), userType);
                startActivity(intent);
            });
        }, TIME_DELAY_MILLI);
    }
}