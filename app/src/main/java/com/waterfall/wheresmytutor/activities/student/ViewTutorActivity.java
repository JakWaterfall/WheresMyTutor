package com.waterfall.wheresmytutor.activities.student;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.MainActivity;
import com.waterfall.wheresmytutor.activities.MapsActivity;
import com.waterfall.wheresmytutor.models.Tutor;
import com.waterfall.wheresmytutor.utils.AddressFinder;
import com.waterfall.wheresmytutor.utils.DatabaseController;

public class ViewTutorActivity extends AppCompatActivity {
    DatabaseController db;
    TextInputEditText locationTxt, statusTxt;
    Button viewInMapsBtn, requestMeetingBtn;
    Tutor tutor;
    String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view_tutor);
        db = new DatabaseController(ViewTutorActivity.this);
        tutorId = getIntent().getStringExtra(getString(R.string.tutorid_intent));
        setUpBackButton();
        initView();
        onTutorInfoChanged();
    }

    private void initView() {
        locationTxt = findViewById(R.id.tutorCurrentLocationTxt);
        statusTxt = findViewById(R.id.tutorStatusTxt);

        requestMeetingBtn = findViewById(R.id.requestMeetingBtn);
        Intent requestIntent = new Intent(ViewTutorActivity.this, RequestMeetingActivity.class);
        requestIntent.putExtra(getString(R.string.tutorid_intent), tutorId);
        requestMeetingBtn.setOnClickListener(v -> startActivity(requestIntent));

        viewInMapsBtn = findViewById(R.id.viewinMapsBtn);
        viewInMapsBtn.setOnClickListener(viewInMapsClickListener);
    }

    private final View.OnClickListener viewInMapsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(tutor.getLatitude() == -1 || tutor.getLongitude() == -1) // no location set
            {
                Toast.makeText(ViewTutorActivity.this, getString(R.string.no_address_found_text), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent mapIntent = new Intent(ViewTutorActivity.this, MapsActivity.class);
            mapIntent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_student));
            mapIntent.putExtra(getString(R.string.tutorid_intent), tutorId);
            startActivity(mapIntent);
        }
    };

    private void onTutorInfoChanged() {
        db.getTutor(tutorId, returnedTutor -> {
            if(returnedTutor == null)
            {                         // return to home page if tutor doesn't exist
                Intent intent = new Intent(ViewTutorActivity.this, MainActivity.class);
                intent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_student));
                startActivity(intent);
            }

            tutor = returnedTutor;

            getSupportActionBar().setTitle(tutor.getFirstName() + " " + tutor.getLastName());
            AddressFinder.getAddress(ViewTutorActivity.this, tutor.getLatitude(), tutor.getLongitude(), new DatabaseController.StringCallback() {
                @Override
                public void onCallback(String address) {
                    locationTxt.setText(address);
                }
            });
            statusTxt.setText(tutor.getStatus());
        });
    }

    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(ViewTutorActivity.this, MainActivity.class);
                intent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_student));
                startActivity(intent);
            }
        });
    }
}