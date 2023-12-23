package com.waterfall.wheresmytutor.activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.databinding.ActivityMapsBinding;
import com.waterfall.wheresmytutor.utils.AddressFinder;
import com.waterfall.wheresmytutor.utils.DatabaseController;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private DatabaseController db;
    private ActivityMapsBinding binding;
    private FloatingActionButton acceptBtn;
    private boolean isAStaticLocationQuery, cameraIsInPosition;
    private String tutorId, meetingId;
    private double staticLat, staticLong;
    private Marker marker;
    boolean forMeetingPoint, tutorIsCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = new DatabaseController(MapsActivity.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.studentMap);
        mapFragment.getMapAsync(this);

        acceptBtn = binding.mapAcceptBtn;
        acceptBtn.setVisibility(View.GONE);

        Intent intent = getIntent();
        String userType = intent.getStringExtra(getString(R.string.usertype_intent));
        tutorIsCurrentUser = userType.equals(getString(R.string.user_type_tutor));

        if(tutorIsCurrentUser) {
            setupTutorMapsActivity(intent);
        } else {
            setupStudentMapsActivity(intent);
        }
    }

    private void setupTutorMapsActivity(Intent intent)
    {
        tutorId = intent.getStringExtra(getString(R.string.tutorid_intent));
        forMeetingPoint = intent.getBooleanExtra(getString(R.string.for_meeting_point_intent), false);
        if(forMeetingPoint)
            meetingId = intent.getStringExtra(getString(R.string.meetingid_intent));
    }

    private void setupStudentMapsActivity(Intent intent)
    {
        isAStaticLocationQuery = intent.getBooleanExtra(getString(R.string.isastaticlocationquery_indent), false);
        if(isAStaticLocationQuery)
        {
            staticLat = intent.getDoubleExtra(getString(R.string.lat_intent), 0);
            staticLong = intent.getDoubleExtra(getString(R.string.long_intent), 0);
        }
        else
        {
            tutorId = intent.getStringExtra(getString(R.string.tutorid_intent));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if(tutorIsCurrentUser) {
            setTutorControls(googleMap);
        } else {
            setStudentControls(googleMap);
        }
    }

    private void setTutorControls(GoogleMap googleMap) {
        // place camera in Nottingham
        double nottsLat = 52.91156978830049, nottsLong = -1.1845536902546883;
        LatLng nottinghamLocation = new LatLng(nottsLat, nottsLong);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nottinghamLocation, 18));

        // On click add location marker
        googleMap.setOnMapClickListener(latLng -> {
            if(marker !=null)
                marker.remove();
            acceptBtn.setVisibility(View.VISIBLE);

            AddressFinder.getAddress(MapsActivity.this, latLng.latitude, latLng.longitude, new DatabaseController.StringCallback() {
                @Override
                public void onCallback(String address) {
                    String locationString = address;
                    locationString = locationString.length() > 31 ?  locationString.substring(0, 30) + "..." : locationString; // shorten title to 30 characters
                    marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(locationString));
                    marker.showInfoWindow();
                }
            });

        });

        // On accept add location to database
        acceptBtn.setOnClickListener(v -> {
            if (marker != null)
            {
                if(forMeetingPoint)
                {
                    db.postMeetingLocation(meetingId, marker.getPosition().latitude, marker.getPosition().longitude);
                    Intent returnIntent = new Intent(MapsActivity.this, ViewMeetingActivity.class);
                    returnIntent.putExtra(getString(R.string.meetingid_intent), meetingId);
                    returnIntent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_tutor));
                    startActivity(returnIntent);
                }
                else
                {
                    db.postTutorLocation(tutorId, marker.getPosition().latitude, marker.getPosition().longitude);
                    Intent returnIntent = new Intent(MapsActivity.this, MainActivity.class);
                    returnIntent.putExtra( getString(R.string.tutorid_intent), tutorId);
                    returnIntent.putExtra(getString(R.string.usertype_intent), getString(R.string.user_type_tutor));
                    startActivity(returnIntent);
                }
            }
        });

    }

    private void setStudentControls(GoogleMap googleMap)
    {
        if(isAStaticLocationQuery)
        {
            placeStaticLocationWithLatLon(googleMap);
        }
        else
        {
            cameraIsInPosition = false;
            placeTutorsCurrentLocation(googleMap);
        }
    }

    private void placeTutorsCurrentLocation(GoogleMap googleMap) {
        db.getTutor(tutorId, tutor -> {
            if(marker !=null)
                marker.remove();

            LatLng tutorLocation = new LatLng(tutor.getLatitude(), tutor.getLongitude());
            marker = googleMap.addMarker(new MarkerOptions().position(tutorLocation).title(tutor.getFullName()));
            marker.showInfoWindow();

            if(!cameraIsInPosition)
            {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tutorLocation, 18));
                cameraIsInPosition = true;
            }
        });
    }

    private void placeStaticLocationWithLatLon(GoogleMap googleMap) {
        LatLng staticLocation = new LatLng(staticLat, staticLong);
        marker = googleMap.addMarker(new MarkerOptions().position(staticLocation).title(getString(R.string.meeting_point_text)));
        marker.showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(staticLocation, 18));
    }
}