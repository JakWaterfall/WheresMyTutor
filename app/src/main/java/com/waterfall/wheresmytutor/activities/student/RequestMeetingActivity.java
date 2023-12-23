package com.waterfall.wheresmytutor.activities.student;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.models.Meeting;
import com.waterfall.wheresmytutor.models.Student;
import com.waterfall.wheresmytutor.models.Tutor;
import com.waterfall.wheresmytutor.utils.DatabaseController;
import com.waterfall.wheresmytutor.utils.CustomDateValidator;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class RequestMeetingActivity extends AppCompatActivity {
    private DatabaseController db;
    private FirebaseAuth mAuth;
    private MaterialDatePicker materialDatePicker;
    private AutoCompleteTextView timeSlotDropDown;
    private TextInputEditText dateTxt, reasonTxt, messageTxt;
    private Button sendBtn, clearBtn;
    private List<String> currentTimeSlots;
    private List<Integer> availableDaysAsCalenderEnum;
    private String tutorId;
    private Student student;
    private Tutor tutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_request_meeting);
        db = new DatabaseController(RequestMeetingActivity.this);
        mAuth = FirebaseAuth.getInstance();
        tutorId = getIntent().getStringExtra(getString(R.string.tutorid_intent));
        InitView();
        setUpBackButton();
        onStudentInfoChanged();
        onTutorInfoChanged();
    }

    private void onTutorInfoChanged() {
        db.getTutor(tutorId, tutor -> this.tutor = tutor);
    }

    private void onStudentInfoChanged() {
        db.getStudent(mAuth.getCurrentUser().getUid(), student -> this.student = student);
    }

    private void InitView() {
        timeSlotDropDown = findViewById(R.id.timeSlotsDropdown);

        sendBtn = findViewById(R.id.sendMeetingRequestBtn);
        clearBtn = findViewById(R.id.clearMeetingRequestBtn);

        sendBtn.setOnClickListener(sendBtnClickListener);
        clearBtn.setOnClickListener(clearBtnClickListener);

        reasonTxt = findViewById(R.id.requestReasonTxt);
        messageTxt = findViewById(R.id.requestMessageTxt);

        dateTxt = findViewById(R.id.requestMeetingDateTxt);
        dateTxt.setKeyListener(null);
        dateTxt.setOnClickListener(v -> {
            if(materialDatePicker == null)
                buildDatePicker();

            materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });

        db.getDaysOfAvailableTimeSlots(tutorId, daysOfAvailableTimeSlots -> availableDaysAsCalenderEnum = daysOfAvailableTimeSlots);
    }

    private final View.OnClickListener sendBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String reason = reasonTxt.getText().toString().trim();
            String message = messageTxt.getText().toString().trim();
            String date = dateTxt.getText().toString().trim();
            String timeSlot = timeSlotDropDown.getText().toString().trim();

            if(validInputs(reason, date, timeSlot))
            {
                String newMeetingId = UUID.randomUUID().toString();
                if(student.getMeetingIds() == null)
                    student.setMeetingIds(new ArrayList<>());

                if(tutor.getMeetingIds() == null)
                    tutor.setMeetingIds(new ArrayList<>());


                student.getMeetingIds().add(newMeetingId);
                tutor.getMeetingIds().add(newMeetingId);
                db.postMeetingRequest(newMeetingId, new Meeting(reason, message, date, timeSlot, student.getFullName(), tutor.getFullName(), student.getUserId(), tutor.getUserId(), newMeetingId));
                db.postTutor(tutorId, tutor);
                db.postStudent(student.getUserId(), student);

                Intent intent = new Intent(RequestMeetingActivity.this, ViewTutorActivity.class);
                intent.putExtra(getString(R.string.tutorid_intent), tutorId);
                startActivity(intent);
            }
        }
    };

    private boolean validInputs(String reason, String date, String timeSlot) {
        boolean isValid = true;

        if(reason.isEmpty())
        {
            reasonTxt.setError(getString(R.string.reason_required_error));
            isValid = false;
        }

        if(date.isEmpty())
        {
            dateTxt.setError(getString(R.string.date_required_error));
            isValid = false;
        }

        if(timeSlot.isEmpty())
        {
            timeSlotDropDown.setError(getString(R.string.time_slot_required_error));
            isValid = false;
        }

        return isValid;
    }

    private final View.OnClickListener clearBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            reasonTxt.getText().clear();
            messageTxt.getText().clear();
            dateTxt.getText().clear();
            timeSlotDropDown.getText().clear();
            timeSlotDropDown.setAdapter(null);
        }
    };

    private void buildDatePicker() {
        MaterialDatePicker.Builder<Long> materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText(R.string.pick_a_date_for_the_meeting_text);

        CalendarConstraints.Builder calConstraints = new CalendarConstraints.Builder();

        ArrayList<CalendarConstraints.DateValidator> listValidators = new ArrayList<CalendarConstraints.DateValidator>() {{
            add(DateValidatorPointForward.now());
            add(new CustomDateValidator(availableDaysAsCalenderEnum));
        }};
        CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);
        calConstraints.setValidator(validators);
        materialDateBuilder.setCalendarConstraints(calConstraints.build());

        materialDatePicker = materialDateBuilder.build();
        materialDatePicker.addOnPositiveButtonClickListener(datePickerListener);
    }

    private final MaterialPickerOnPositiveButtonClickListener<Long> datePickerListener = selection -> {
        buildTimeSlotPicker(selection);
        Date date = new Date(selection);
        dateTxt.setText(date.toString());
    };

    private void buildTimeSlotPicker(Long selection) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(selection);

        String day;
        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case Calendar.MONDAY:
                day = getString(R.string.day_monday);
                break;
            case Calendar.TUESDAY:
                day = getString(R.string.day_tuesday);
                break;
            case Calendar.WEDNESDAY:
                day = getString(R.string.day_wednesday);
                break;
            case Calendar.THURSDAY:
                day = getString(R.string.day_thursday);
                break;
            default:
                day = getString(R.string.day_friday);
        }

        db.getTutorTimeSlotsByDay(tutorId, day, timeSlots -> {
            currentTimeSlots = timeSlots;

            timeSlotDropDown.getText().clear();
            ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(RequestMeetingActivity.this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, currentTimeSlots);
            timeSlotDropDown.setAdapter(daysAdapter);

            if(currentTimeSlots.size() > 0)
                timeSlotDropDown.setText(daysAdapter.getItem(0), false);
        });
    }
    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(RequestMeetingActivity.this, ViewTutorActivity.class);
                intent.putExtra(getString(R.string.tutorid_intent), tutorId);
                startActivity(intent);
            }
        });
    }
}