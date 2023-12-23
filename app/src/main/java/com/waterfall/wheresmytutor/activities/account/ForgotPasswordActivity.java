package com.waterfall.wheresmytutor.activities.account;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Button sendBtn, cancelBtn;
    private TextInputEditText emailTxt;
    private FirebaseAuth mAuth;
    private MaterialAlertDialogBuilder passwordLinkSentDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mAuth = FirebaseAuth.getInstance();
        initView();
        setUpBackButton();
    }

    private void initView() {
        sendBtn = findViewById(R.id.sendForgotPassBtn);
        cancelBtn = findViewById(R.id.cancelForgotPassBtn);

        sendBtn.setOnClickListener(sendForgotPassLinkClickListener);
        cancelBtn.setOnClickListener(v -> startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class)));

        emailTxt = findViewById(R.id.emailForgotPassTxt);

        passwordLinkSentDialog = new MaterialAlertDialogBuilder(ForgotPasswordActivity.this)
                .setTitle(R.string.change_password_dialog_title)
                .setMessage(R.string.change_password_dialog_desc);
    }

    private final View.OnClickListener sendForgotPassLinkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = emailTxt.getText().toString().trim();
            if (validateEmail(email))
            {
                mAuth.sendPasswordResetEmail(email);
                passwordLinkSentDialog.show();
            }
        }
    };

    private boolean validateEmail(String email) {
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

        return isValid;
    }

    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            }
        });
    }
}