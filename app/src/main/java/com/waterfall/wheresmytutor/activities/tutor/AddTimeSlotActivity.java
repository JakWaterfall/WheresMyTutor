package com.waterfall.wheresmytutor.activities.tutor;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.MainActivity;
import com.waterfall.wheresmytutor.utils.DatabaseController;

import java.util.Collections;
import java.util.List;

public class AddTimeSlotActivity extends AppCompatActivity {

    DatabaseController db;
    FirebaseAuth mAuth;
    TimePicker startTime, endTime;
    Button acceptBtn;
    String day;
    List<String> timeSlotsOnDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_add_time_slot);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(AddTimeSlotActivity.this);
        day = getIntent().getStringExtra(getString(R.string.day_intent));
        initViews();
        initTimeSlots();
        setUpBackButton();
        getSupportActionBar().setTitle(day);
    }

    private void initTimeSlots() {
        db.getTutorTimeSlotsByDay(mAuth.getCurrentUser().getUid(), day, timeSlotList -> timeSlotsOnDay = timeSlotList);
    }

    private void initViews() {
        startTime = findViewById(R.id.startTimePicker);
        endTime = findViewById(R.id.endTimePicker);
        startTime.setHour(8);
        startTime.setMinute(0);
        endTime.setHour(9);
        endTime.setMinute(0);
        startTime.setIs24HourView(true);
        endTime.setIs24HourView(true);

        acceptBtn = findViewById(R.id.timeSlotAcceptBtn);
        acceptBtn.setOnClickListener(acceptClickListener);
    }

    private final View.OnClickListener acceptClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String startTimeHour = padWithZero(String.valueOf(startTime.getHour()));
            String startTimeMin = padWithZero(String.valueOf(startTime.getMinute()));
            String endTimeHour = padWithZero(String.valueOf(endTime.getHour()));
            String endTimeMin = padWithZero(String.valueOf(endTime.getMinute()));

            String fullStartTime = startTimeHour + ":" + startTimeMin;
            String fullEndTime = endTimeHour + ":" + endTimeMin;

            String timeSlotAsString = fullStartTime + " - " + fullEndTime;


            timeSlotsOnDay.add(timeSlotAsString);
            Collections.sort(timeSlotsOnDay);

            db.postTimeSlotsByDay(mAuth.getCurrentUser().getUid(), day, timeSlotsOnDay);

            Intent intent = new Intent(AddTimeSlotActivity.this, MainActivity.class);
            intent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_tutor));
            intent.putExtra(getString(R.string.fragmentid_intent), R.id.availabilityFragment);
            startActivity(intent);
        }
    };

    private String padWithZero(String time) {
        return time.length() < 2 ? "0" + time : time;
    }

    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Intent intent = new Intent(AddTimeSlotActivity.this, MainActivity.class);
                intent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_tutor));
                intent.putExtra(getString(R.string.fragmentid_intent), R.id.availabilityFragment);
                startActivity(intent);
            }
        });
    }
}