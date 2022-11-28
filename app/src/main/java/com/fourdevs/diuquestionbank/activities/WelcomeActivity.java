package com.fourdevs.diuquestionbank.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.fourdevs.diuquestionbank.authentication.LoginActivity;
import com.fourdevs.diuquestionbank.authentication.SignupActivity;
import com.fourdevs.diuquestionbank.authentication.VerificationActivity;
import com.fourdevs.diuquestionbank.databinding.ActivityWelcomeBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else if(preferenceManager.getBoolean(Constants.KEY_IS_VERIFICATION_PAGE)) {
            Intent intent = new Intent(getApplicationContext(), VerificationActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }
    private void setListeners() {
        binding.buttonLogIn.setOnClickListener(view -> {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });

        binding.buttonSignUp.setOnClickListener(view -> {
            Intent signupIntent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(signupIntent);
            finish();
        });
    }
}