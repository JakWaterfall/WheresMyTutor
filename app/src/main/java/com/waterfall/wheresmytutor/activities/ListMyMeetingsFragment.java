package com.waterfall.wheresmytutor.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.adapters.MeetingsViewAdapter;
import com.waterfall.wheresmytutor.databinding.FragmentListMyMeetingsBinding;
import com.waterfall.wheresmytutor.models.Meeting;
import com.waterfall.wheresmytutor.utils.DatabaseController;

import java.util.List;

public class ListMyMeetingsFragment extends Fragment {

    private FragmentListMyMeetingsBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseController db;
    private List<Meeting> meetings;
    private MeetingsViewAdapter meetingsViewAdapter;
    private RecyclerView meetingsRecView;
    private String userType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(requireContext());
        updateMeetings();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListMyMeetingsBinding.inflate(inflater, container, false);
        userType = getArguments().getString(getString(R.string.usertype_intent));
        meetingsViewAdapter = new MeetingsViewAdapter(getActivity(), userType, deleteCallback);
        meetingsRecView = binding.meetingsRecView;
        meetingsRecView.setAdapter(meetingsViewAdapter);
        meetingsRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return binding.getRoot();
    }

    private final MeetingsViewAdapter.ItemDeleteCallback deleteCallback = new MeetingsViewAdapter.ItemDeleteCallback() {
        @Override
        public void onCallback(int position) {
            Meeting meetToDelete = meetings.get(position);
            meetings.remove(position);
            String otherUserId = userType.equals(getString(R.string.user_type_tutor)) ? meetToDelete.getStudentId() : meetToDelete.getTutorId();
            db.deleteMeetingRequest(meetToDelete, mAuth.getCurrentUser().getUid(), otherUserId, isSuccessful -> {
                if(isSuccessful)
                    updateMeetings();
            });
        }
    };

    private void updateMeetings() {
        db.getMyMeetings(mAuth.getCurrentUser().getUid(), returnedMeetings -> {
            meetings = returnedMeetings;
            meetingsViewAdapter.setMeetings(meetings);
        });
    }

}