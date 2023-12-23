package com.waterfall.wheresmytutor.activities.student;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.adapters.TutorsViewAdapter;
import com.waterfall.wheresmytutor.databinding.FragmentStudentListMyTutorsBinding;
import com.waterfall.wheresmytutor.models.Tutor;
import com.waterfall.wheresmytutor.utils.DatabaseController;

import java.util.ArrayList;
import java.util.List;

public class ListMyTutorsFragment extends Fragment {

    private FragmentStudentListMyTutorsBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseController db;
    private TutorsViewAdapter tutorsViewAdapter;
    private RecyclerView myTutorsRecView;
    private List<Tutor> myTutors;
    private List<String> myTutorsIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMyTutorList();
    }

    private final TutorsViewAdapter.PositionCallback itemPressCallback = new TutorsViewAdapter.PositionCallback() {
        @Override
        public void onCallback(int position) {
            String tutorToBeAlteredId= myTutors.get(position).getUserId();
            myTutorsIds.remove(tutorToBeAlteredId);
            db.postMyTutorsIds(mAuth.getCurrentUser().getUid(), myTutorsIds);
        }
    };

    private void updateMyTutorList() {
        db.getMyTutors(mAuth.getCurrentUser().getUid(), tutors -> {
            if(tutors == null)
            {
                tutorsViewAdapter.setTutors(new ArrayList<>()); // empty list
                return;
            }
            myTutors = tutors;
            tutorsViewAdapter.setTutors(myTutors);
        });
        db.getMyTutorsIds(mAuth.getCurrentUser().getUid(), tutorIds -> myTutorsIds = tutorIds);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentListMyTutorsBinding.inflate(inflater, container, false);
        tutorsViewAdapter = new TutorsViewAdapter(getActivity(), itemPressCallback);
        myTutorsRecView = binding.TutorMeetingsRecView2;
        myTutorsRecView.setAdapter(tutorsViewAdapter);
        myTutorsRecView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return binding.getRoot();
    }
}