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
import com.waterfall.wheresmytutor.databinding.FragmentStudentListAllTutorsBinding;
import com.waterfall.wheresmytutor.models.Tutor;
import com.waterfall.wheresmytutor.utils.DatabaseController;

import java.util.List;

public class ListAllTutorsFragment extends Fragment {

    private FragmentStudentListAllTutorsBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseController db;
    private TutorsViewAdapter tutorsViewAdapter;
    private RecyclerView allTutorsRecView;
    private List<Tutor> allTutors;
    private List<String> myTutorsIds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentListAllTutorsBinding.inflate(inflater, container, false);
        tutorsViewAdapter = new TutorsViewAdapter(getActivity(), itemPressCallback);
        allTutorsRecView = binding.allTutorsRecView2;
        allTutorsRecView.setAdapter(tutorsViewAdapter);
        allTutorsRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTutorList();
    }

    private final TutorsViewAdapter.PositionCallback itemPressCallback = new TutorsViewAdapter.PositionCallback() {
        @Override
        public void onCallback(int position) {
            String tutorId = allTutors.get(position).getUserId();
            if(myTutorsIds.contains(tutorId))
            {
                myTutorsIds.remove(tutorId);
            }
            else
            {
                myTutorsIds.add(tutorId);
            }
            db.postMyTutorsIds(mAuth.getCurrentUser().getUid(), myTutorsIds);
        }
    };

    private void updateTutorList() {
        db.getMyTutorsIds(mAuth.getCurrentUser().getUid(), list -> {
            myTutorsIds = list;
            updateRecView();
        });

        db.getAllTutors(list -> {
            allTutors = list;
            updateRecView();
        });
    }

    private void updateRecView() {
        if(allTutors != null && myTutorsIds != null)
        {
            tutorsViewAdapter.setTutors(allTutors, myTutorsIds);
        }
    }
}