package com.waterfall.wheresmytutor.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.models.Meeting;
import com.waterfall.wheresmytutor.utils.AddressFinder;
import com.waterfall.wheresmytutor.utils.DatabaseController;

public class ViewMeetingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseController db;
    private Meeting meeting;
    private String meetingId, userType;

    private TextInputEditText reasonTxt, messageTxt, dateTxt, timeSlotTxt, locationTxt, statusTxt;
    private Button viewInMapsBtn, setLocationBtn, acceptBtn, declineBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_meeting);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(ViewMeetingActivity.this);
        meetingId = getIntent().getStringExtra(getString(R.string.meetingid_intent));
        userType = getIntent().getStringExtra(getString(R.string.usertype_intent));
        setUpBackButton();
        initView();
        onMeetingInfoChanged();
    }

    private void onMeetingInfoChanged() {
        db.getMeeting(meetingId, returnMeeting -> {
            meeting = returnMeeting;
            setupMeetingInfo(meeting);
        });
    }

    private void setupMeetingInfo(Meeting meeting) {
        if(meeting != null)
        {
            reasonTxt.setText(meeting.getReason());
            messageTxt.setText(meeting.getMessage());
            dateTxt.setText(meeting.getDate());
            timeSlotTxt.setText(meeting.getTimeSlot());

            AddressFinder.getAddress(ViewMeetingActivity.this, meeting.getLatitude(), meeting.getLongitude(), new DatabaseController.StringCallback() {
                @Override
                public void onCallback(String address) {
                    locationTxt.setText(address);
                }
            });

            statusTxt.setText(meeting.getStatus());
            acceptBtn.setOnClickListener(ChangeMeetingStatusClickListener);
            declineBtn.setOnClickListener(ChangeMeetingStatusClickListener);

            if(userType.equals(getString(R.string.user_type_tutor)))
            {
                Intent setLocationIntent = new Intent(ViewMeetingActivity.this, MapsActivity.class);
                setLocationIntent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_tutor));
                setLocationIntent.putExtra(getString(R.string.tutorid_intent), mAuth.getCurrentUser().getUid());
                setLocationIntent.putExtra(getString(R.string.for_meeting_point_intent), true);
                setLocationIntent.putExtra(getString(R.string.meetingid_intent), meetingId);
                setLocationBtn.setOnClickListener(v -> startActivity(setLocationIntent));
            }
            else
            {
                viewInMapsBtn.setOnClickListener(viewInMapsClickListener);
            }
        }
        else
        {
            // Go back to home because something went wrong.
            Intent goBackToMainIntent = new Intent(ViewMeetingActivity.this, MainActivity.class);
            goBackToMainIntent.putExtra(getString(R.string.usertype_intent), userType);
            goBackToMainIntent.putExtra(getString(R.string.fragmentid_intent), R.id.studentListMyMeetingsFragment);
            startActivity(goBackToMainIntent);
        }
    }

    private final View.OnClickListener viewInMapsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(meeting.getLatitude() == -1 || meeting.getLongitude() == -1) // no location set
            {
                Toast.makeText(ViewMeetingActivity.this, getString(R.string.no_address_found_text), Toast.LENGTH_SHORT).show();
                return;
            }

            Intent viewInMapsIntent = new Intent(ViewMeetingActivity.this, MapsActivity.class);
            viewInMapsIntent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_student));
            viewInMapsIntent.putExtra(getString(R.string.isastaticlocationquery_indent), true);
            viewInMapsIntent.putExtra(getString(R.string.lat_intent), meeting.getLatitude());
            viewInMapsIntent.putExtra(getString(R.string.long_intent), meeting.getLongitude());
            startActivity(viewInMapsIntent);
        }
    };

    private final View.OnClickListener ChangeMeetingStatusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.acceptMeetBtn) {
                meeting.setStatus(getString(R.string.accepted_status));
            } else {
                meeting.setStatus(getString(R.string.declined_status));
            }
            db.updateMeeting(meeting);
        }
    };

    private void initView() {
        reasonTxt = findViewById(R.id.viewMeetReasonTxt);
        messageTxt = findViewById(R.id.viewMeetMessageTxt);
        dateTxt = findViewById(R.id.viewMeetDateTxt);
        timeSlotTxt = findViewById(R.id.viewMeetTimeSlotTxt);
        locationTxt = findViewById(R.id.viewMeetLocationTxt);
        statusTxt = findViewById(R.id.viewMeetStatusTxt);
        viewInMapsBtn = findViewById(R.id.viewMeetMapsBtn);
        setLocationBtn = findViewById(R.id.viewMeetSetLocationBtn);
        acceptBtn = findViewById(R.id.acceptMeetBtn);
        declineBtn = findViewById(R.id.declineMeetBtn);

        if(userType.equals(getString(R.string.user_type_tutor)))
        {
            viewInMapsBtn.setVisibility(View.GONE);
            setLocationBtn.setVisibility(View.VISIBLE);
            acceptBtn.setVisibility(View.VISIBLE);
            declineBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            viewInMapsBtn.setVisibility(View.VISIBLE);
            setLocationBtn.setVisibility(View.GONE);
            acceptBtn.setVisibility(View.GONE);
            declineBtn.setVisibility(View.GONE);
        }
    }

    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(ViewMeetingActivity.this, MainActivity.class);
                intent.putExtra(getString(R.string.usertype_intent), userType);
                intent.putExtra(getString(R.string.fragmentid_intent), R.id.studentListMyMeetingsFragment);
                startActivity(intent);
            }
        });
    }
}