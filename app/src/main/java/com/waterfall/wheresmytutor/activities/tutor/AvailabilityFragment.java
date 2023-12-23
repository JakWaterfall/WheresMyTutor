package com.waterfall.wheresmytutor.activities.tutor;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.adapters.AvailabilityTimeSlotViewAdapter;
import com.waterfall.wheresmytutor.databinding.FragmentTutorAvailabilityBinding;
import com.waterfall.wheresmytutor.utils.DatabaseController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AvailabilityFragment extends Fragment {
    private FragmentTutorAvailabilityBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseController db;
    private AutoCompleteTextView daysDropDown;
    private AvailabilityTimeSlotViewAdapter timeSlotAdapter;
    private RecyclerView timeSlotRecView;
    private FloatingActionButton addTimeSlotBtn;
    private List<String> currentTimeSlots;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTutorAvailabilityBinding.inflate(inflater, container, false);
        timeSlotAdapter = new AvailabilityTimeSlotViewAdapter(requireContext(), itemDeleteCallback);
        timeSlotRecView = binding.timeSlotRecView;

        timeSlotRecView.setAdapter(timeSlotAdapter);
        timeSlotRecView.setLayoutManager(new LinearLayoutManager(requireContext()));

        addTimeSlotBtn = binding.gotToAddTimeSlotBtn;

        addTimeSlotBtn.setOnClickListener(v -> {
            Intent timeslotIntent = new Intent(requireContext(), AddTimeSlotActivity.class);
            timeslotIntent.putExtra(getString(R.string.day_intent), daysDropDown.getText().toString().trim());
            startActivity(timeslotIntent);
        });

        daysDropDown = binding.daysDropdown;
        initDaysOfWeekDropdown();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        initDaysOfWeekDropdown(); // re init on resume
    }

    private final AvailabilityTimeSlotViewAdapter.ItemDeleteCallback itemDeleteCallback = position -> {
        currentTimeSlots.remove(position);
        db.postTimeSlotsByDay(mAuth.getCurrentUser().getUid(), daysDropDown.getText().toString().trim(), currentTimeSlots);
    };

    private void updateTimeSlots() {
        db.getTutorTimeSlotsByDay(mAuth.getCurrentUser().getUid(), daysDropDown.getText().toString().trim(), list -> {
            currentTimeSlots = list;
            timeSlotAdapter.setTimeSlots(list);
        });
    }

    private void initDaysOfWeekDropdown() {
        ArrayList<String> daysOfTheWeek = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.days_of_week)));
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item, daysOfTheWeek);
        daysDropDown.setAdapter(daysAdapter);
        daysDropDown.setText(daysAdapter.getItem(0), false);
        daysDropDown.setOnItemClickListener((parent, view, position, id) -> updateTimeSlots()); // when a day is selected update the timeslots
        updateTimeSlots();
    }
}