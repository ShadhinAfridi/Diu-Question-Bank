package com.fourdevs.diuquestionbank.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.databinding.ActivityVerificationBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.fourdevs.diuquestionbank.viewmodel.AuthViewModel;

public class VerificationActivity extends AppCompatActivity {
    private ActivityVerificationBinding binding;
    private PreferenceManager preferenceManager;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        binding.userName.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        setListeners();
    }

    private void setListeners() {
        binding.buttonResendVerification.setOnClickListener(v -> {
            binding.buttonResendVerification.setClickable(false);
            counter();
            sendVerificationEmail();
        });
        binding.iconLogout.setOnClickListener(v-> logOut());
    }


    private void counter() {
        authViewModel.countDownStart();
        authViewModel.time().observe(this, time -> binding.buttonResendVerification.setText(time));
        authViewModel.finished().observe(this, aBoolean -> {
            if(aBoolean) {
                makeClickable();
            }
        });
    }


    private void sendVerificationEmail() {
        authViewModel.sendVerificationEmail()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        makeToast("We've sent a verification email to "+preferenceManager.getString(Constants.KEY_EMAIL)+". Please verify your email");
                        logOut();
                    } else {
                        makeClickable();
                        makeToast("Unable to send verification email.");
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void makeClickable() {
        binding.buttonResendVerification.setClickable(true);
        binding.buttonResendVerification.setText("Resend");
    }

    private void logOut() {
        authViewModel.logOut();
        preferenceManager.clear();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
}