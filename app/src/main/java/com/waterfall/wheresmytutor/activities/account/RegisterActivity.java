package com.waterfall.wheresmytutor.activities.account;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.models.User;
import com.waterfall.wheresmytutor.utils.DatabaseController;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseController db;
    private TextInputEditText fNameTxt, lNameTxt, emailTxt, passTxt, confirmPassTxt;
    private String fName, lName, email, pass, confirmPass, userType;
    private Button signUpBtn, cancelBtn;
    private MaterialAlertDialogBuilder emailNotNtuDomainDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle(getString(R.string.register_text));
        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseController(RegisterActivity.this);
        initViews();
        setUpBackButton();
    }

    private void initViews() {
        fNameTxt = findViewById(R.id.firstNameRegTxt);
        lNameTxt = findViewById(R.id.lastNameRegTxt);
        emailTxt = findViewById(R.id.emailRegTxt);
        passTxt = findViewById(R.id.passRegTxt);
        confirmPassTxt = findViewById(R.id.confirmPassRegTxt);

        signUpBtn = findViewById(R.id.signUpBtn);
        cancelBtn = findViewById(R.id.cancelSignupBtn);

        signUpBtn.setOnClickListener(signUpClickListener);
        cancelBtn.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        emailNotNtuDomainDialog = new MaterialAlertDialogBuilder(RegisterActivity.this)
                .setTitle(R.string.email_not_ntu_domain_title)
                .setMessage(R.string.email_not_ntu_domain_desc);
    }

    private final View.OnClickListener signUpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fName = fNameTxt.getText().toString().trim();
            lName = lNameTxt.getText().toString().trim();
            email = emailTxt.getText().toString().trim();
            pass = passTxt.getText().toString().trim();
            confirmPass = confirmPassTxt.getText().toString().trim();

            if (validInputs(fName, lName, email, pass, confirmPass))
            {
                Pair<Boolean, String> ntuEmailValidationResult = ntuValidateEmailAndGetUserType(email);
                if (ntuEmailValidationResult.first)
                {
                    userType = ntuEmailValidationResult.second;
                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(createUserCompleteListener);
                }
                else
                {
                    emailNotNtuDomainDialog.show();
                }
            }
        }
    };

    private Pair<Boolean, String> ntuValidateEmailAndGetUserType(String email) {
        Pattern debugEmailTutorRegex = Pattern.compile("^(.+)@hotmail\\.co(.+)$");
        Pattern debugEmailTutorRegex2 = Pattern.compile("^(.+)@msn\\.co(.+)$");
        Pattern debugEmailStudentRegex = Pattern.compile("^(.+)@googlemail\\.co(.+)$"); // todo remove these
        Pattern debugEmailStudentRegex2 = Pattern.compile("^(.+)@gmail\\.co(.+)$");

        Pattern  ntuEmailTutorRegex= Pattern.compile("^(.+)@ntu\\.ac\\.uk$");
        Pattern  ntuEmailStudentRegex= Pattern.compile("^(.+)@my\\.ntu\\.ac\\.uk$");

        if(ntuEmailTutorRegex.matcher(email).matches()) // is tutor email
        {
            return new Pair<>(true, getString(R.string.user_type_tutor));
        }
        else if (ntuEmailStudentRegex.matcher(email).matches()) // is student email
        {
            return new Pair<>(true, getString(R.string.user_type_student));
        }
        else if (debugEmailTutorRegex.matcher(email).matches()) // todo remove debug email address
        {
            return new Pair<>(true, getString(R.string.user_type_tutor));
        }
        else if (debugEmailTutorRegex2.matcher(email).matches()) // todo remove debug email address
        {
            return new Pair<>(true, getString(R.string.user_type_tutor));
        }
        else if (debugEmailStudentRegex.matcher(email).matches()) // todo remove debug email address
        {
            return new Pair<>(true, getString(R.string.user_type_student));
        }
        else if (debugEmailStudentRegex2.matcher(email).matches()) // todo remove debug email address
        {
            return new Pair<>(true, getString(R.string.user_type_student));
        }
        else
        {
            return new Pair<>(false, "");
        }
    }

    private final OnCompleteListener<AuthResult> createUserCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if(task.isSuccessful())
            {
                task.getResult().getUser().sendEmailVerification();

                User newUser = new User(fName, lName, email , userType, mAuth.getCurrentUser().getUid());

                db.postUser(mAuth.getCurrentUser().getUid(), newUser, isSuccessful -> {
                    if(isSuccessful)
                    {
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, getString( R.string.database_error), Toast.LENGTH_LONG).show();
                    }

                });
            }
        }
    };

    private boolean validInputs(String fName, String lName, String email, String pass, String confirmPass) {
        boolean isValid = true;

        if(fName.isEmpty())
        {
            fNameTxt.setError(getString(R.string.first_name_required_error));
            isValid = false;
        }

        if(lName.isEmpty())
        {
            lNameTxt.setError(getString(R.string.last_name_required_error));
            isValid = false;
        }

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

        if(confirmPass.isEmpty())
        {
            confirmPassTxt.setError(getString(R.string.confirm_password_required_error));
            isValid = false;
        }

        if(pass.length() < 7)
        {
            passTxt.setError(getString(R.string.password_incorrect_length_error));
            isValid = false;
        }

        if(!pass.equals(confirmPass))
        {
            confirmPassTxt.setError(getString(R.string.confirm_password_match_error));
            isValid = false;
        }

        return isValid;
    }

    private void setUpBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }
}