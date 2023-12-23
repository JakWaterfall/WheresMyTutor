package com.waterfall.wheresmytutor.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;

import com.waterfall.wheresmytutor.activities.account.LoginActivity;
import com.waterfall.wheresmytutor.databinding.ActivityMainBinding;
import com.waterfall.wheresmytutor.services.NetworkService;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private BottomNavigationView navView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(MainActivity.this, NetworkService.class));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        String userType = getIntent().getStringExtra(getString(R.string.usertype_intent));

        navView = binding.navView;
        navView.getMenu().clear();
        navController = Navigation.findNavController(this, R.id.fragmentHost);

        if(userType.equals(getString(R.string.user_type_student)))
        {
            navView.inflateMenu(R.menu.student_bottom_nav_menu);
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.listMyTutorsFragment, R.id.listAllTutorsFragment, R.id.studentListMyMeetingsFragment).build();
            navController.setGraph(R.navigation.student_navigation);
        }
        else
        {
            navView.inflateMenu(R.menu.tutor_bottom_nav_menu);
            appBarConfiguration = new AppBarConfiguration.Builder(R.id.controlPanelFragment, R.id.availabilityFragment, R.id.studentListMyMeetingsFragment).build();
            navController.setGraph(R.navigation.tutor_navigation);
        }

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navigateToCorrectFragment();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }

    private void navigateToCorrectFragment() {
        int fragmentId = getIntent().getIntExtra(getString(R.string.fragmentid_intent), -1);

        if(fragmentId == -1)
            return;

        navView.setSelectedItemId(fragmentId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logoutMenuItem) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}