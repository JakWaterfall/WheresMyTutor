package com.waterfall.wheresmytutor.activities.tutor;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.MapsActivity;
import com.waterfall.wheresmytutor.databinding.FragmentTutorControlPanelBinding;
import com.waterfall.wheresmytutor.services.LocationService;
import com.waterfall.wheresmytutor.utils.AddressFinder;
import com.waterfall.wheresmytutor.utils.DatabaseController;

public class ControlPanelFragment extends Fragment {
    private FragmentTutorControlPanelBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseController db;
    private MaterialSwitch broadcastSwitch;
    private TextInputEditText locationTxt, statusTxt;
    private Button setLocationBtn, removeLocationBtn, changeStatusBtn;
    private PopupMenu changeStatusPopupMenu;
    private MaterialAlertDialogBuilder permissionsRejectedDialog;
    private MaterialAlertDialogBuilder keepLocationDialog;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTutorControlPanelBinding.inflate(inflater, container, false);
        locationTxt = binding.currentLocationTxt;
        statusTxt = binding.currentStatusHomeTxt;

        setLocationBtn = binding.setLocationHomeBtn;

        Intent setLocationIntent = new Intent(requireContext(), MapsActivity.class);
        setLocationIntent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_tutor));
        setLocationIntent.putExtra(getString(R.string.tutorid_intent), mAuth.getCurrentUser().getUid());
        setLocationBtn.setOnClickListener(v -> startActivity(setLocationIntent));

        removeLocationBtn = binding.removeLocationHomeBtn;
        removeLocationBtn.setOnClickListener(v -> db.postTutorLocation(mAuth.getCurrentUser().getUid(), -1, -1));

        changeStatusBtn = binding.changeStatusBtn;
        changeStatusBtn.setOnClickListener(v -> changeStatusPopupMenu.show());

        permissionsRejectedDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.location_permission_rejected_title)
                .setIcon(R.drawable.ic_location_pin_icon)
                .setMessage(R.string.location_permission_rejected_desc);

        keepLocationDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.keep_current_location_dialog_title)
                .setIcon(R.drawable.ic_location_pin_icon)
                .setMessage(R.string.keep_current_location_dialog_desc)
                .setPositiveButton(getString(R.string.yes_text), (dialog, which) -> {})
                .setCancelable(false)
                .setNegativeButton(getString(R.string.no_text), (dialog, which) -> db.postTutorLocation(mAuth.getCurrentUser().getUid(), -1, -1));

        changeStatusPopupMenu = new PopupMenu(getActivity(), changeStatusBtn);
        changeStatusPopupMenu.getMenuInflater().inflate(R.menu.status_menu, changeStatusPopupMenu.getMenu());
        changeStatusPopupMenu.setOnMenuItemClickListener(statusMenuItemClickedListener);
        initBroadcastLocation();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        onTutorInfoChanged();
        setBroadcastSwitchIfServiceRunning(broadcastSwitch); // reset the BroadcastSwitch when the user returns.
    }

    private void initBroadcastLocation() {
        broadcastSwitch = binding.broadcastSwitch;
        setBroadcastSwitchIfServiceRunning(broadcastSwitch);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    permissionsRejectedDialog.show();
                }});

        broadcastSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
            {
                launchLocationService();
            }
            else
            {
                boolean stoppedService = requireContext().stopService(new Intent(getActivity(), LocationService.class));
                if(stoppedService)
                    keepLocationDialog.show();
            }
        });
    }

    private void launchLocationService() {
        // if permissions granted launch location service other ask for permission.
        if (ContextCompat.checkSelfPermission( requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            requireActivity().startService(new Intent(requireActivity(), LocationService.class));
        }
        else
        {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void onTutorInfoChanged() {
        db.getTutor(mAuth.getCurrentUser().getUid(), tutor -> {
            if(tutor != null && statusTxt != null && locationTxt != null && getActivity() != null)
            {
                statusTxt.setText(tutor.getStatus());
                AddressFinder.getAddress(getActivity(), tutor.getLatitude(), tutor.getLongitude(), returnAddress -> locationTxt.setText(returnAddress));
            }
        });
    }

    private void setBroadcastSwitchIfServiceRunning(MaterialSwitch broadcastSwitch) {
        // sets the broadcast switch based on if the location service is currently running.
        boolean serviceRunning = false;
        ActivityManager activityManager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE))
        {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                serviceRunning = true; // Location service found
            }
        }
        broadcastSwitch.setChecked(serviceRunning);
    }

    private final PopupMenu.OnMenuItemClickListener statusMenuItemClickedListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String userId = mAuth.getCurrentUser().getUid();

            int itemId = item.getItemId();
            if (itemId == R.id.availableMenuItem) {
                db.postTutorStatus(userId, getString(R.string.available_status));
            } else if (itemId == R.id.busyMenuItem) {
                db.postTutorStatus(userId, getString(R.string.busy_status));
            } else if (itemId == R.id.inAMeetingMenuItem) {
                db.postTutorStatus(userId, getString(R.string.in_a_meeting_status));
            } else if (itemId == R.id.inASessionMenuItem) {
                db.postTutorStatus(userId, getString(R.string.in_a_session_status));
            } else if (itemId == R.id.offCampusMenuItem) {
                db.postTutorStatus(userId, getString(R.string.off_campus_status));
            } else {
                return false;
            }
            return true;
        }
    };
}