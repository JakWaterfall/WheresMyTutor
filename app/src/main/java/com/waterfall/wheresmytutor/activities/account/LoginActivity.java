package com.waterfall.wheresmytutor.activities.account;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.activities.MainActivity;
import com.waterfall.wheresmytutor.utils.DatabaseController;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn, registerBtn, forgotPassBtn;
    private TextInputEditText emailTxt, passTxt;
    private FirebaseAuth mAuth;
    private DatabaseController db;
    private MaterialAlertDialogBuilder loginFailedDialog;
    private MaterialAlertDialogBuilder userNotVerifiedDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = new DatabaseController(LoginActivity.this);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setUpBackButton();
    }

    private void initViews() {
        emailTxt = findViewById(R.id.emailForgotPassTxt);
        passTxt = findViewById(R.id.passLoginTxt);

        loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(loginClickListener);

        registerBtn = findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        forgotPassBtn = findViewById(R.id.forgotPassBtn);
        forgotPassBtn.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        loginFailedDialog = new MaterialAlertDialogBuilder(LoginActivity.this)
                .setTitle(R.string.login_failed_dialog_title)
                .setMessage(R.string.login_failed_dialog_desc);

        userNotVerifiedDialog = new MaterialAlertDialogBuilder(LoginActivity.this)
                .setTitle(R.string.user_not_verified_title)
                .setMessage(R.string.user_not_verified_desc)
                .setPositiveButton(getString(R.string.yes_text), (dialog, which) -> mAuth.getCurrentUser().sendEmailVerification())
                .setNegativeButton(getString(R.string.no_text), (dialog, which) -> {
                    // no nothing
                });
    }

    private final View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailTxt.getText().toString().trim();
            String pass = passTxt.getText().toString().trim();

            if(!validInputs(email, pass))
                return;

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(signInCompleteListener);
        }
    };

    private final OnCompleteListener<AuthResult> signInCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (!task.isSuccessful()) {
                loginFailedDialog.show();
                clearPasswordInput();
                return;
            }

            if (!mAuth.getCurrentUser().isEmailVerified()) {
                userNotVerifiedDialog.show();
                clearPasswordInput();
                return;
            }
            navigateUserToMainActivity();
        }
    };

    /*
    * Navigates to the correct Activity based on user type
    * */
    private void navigateUserToMainActivity() {
        db.getUserType(mAuth.getCurrentUser().getUid(), userType -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(getString(R.string.usertype_intent), userType);
            startActivity(intent);
        });
    }

    private void clearPasswordInput() {
        passTxt.getText().clear();
    }

    private boolean validInputs(String email, String pass) {
        boolean isValid = true;

        if(email.isEmpty())
        {
            emailTxt.setError(getString(R.string.email_required_error));
            isValid = false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailTxt.setError(getString(R.string.valid_email_error));
            isValid = false;
        }

        if(pass.isEmpty())
        {
            passTxt.setError(getString(R.string.password_required_error));
            isValid = false;
        }

        return isValid;
    }

    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }
}