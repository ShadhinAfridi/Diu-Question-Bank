package com.fourdevs.diuquestionbank.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.activities.WelcomeActivity;
import com.fourdevs.diuquestionbank.databinding.ActivityResetPasswordBinding;
import com.fourdevs.diuquestionbank.viewmodel.AuthViewModel;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private String email;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setListeners();
    }

    private void setListeners() {
        binding.buttonResetPassword.setOnClickListener(view -> {
            email = Objects.requireNonNull(binding.inputUserEmail.getText()).toString().trim();
            if(isValidSignUpDetails()){
                loading(true);
                sendResetEmail();
            }
        });

        binding.iconBack.setOnClickListener(view -> {
            Intent welcomeIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(welcomeIntent);
            finish();
        });

    }

    private void sendResetEmail() {
        binding.buttonResetPassword.setClickable(false);
        authViewModel.sendResetPasswordEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        makeToast("A password reset email to "+ email);
                        binding.buttonResetPassword.setClickable(true);
                        binding.inputUserEmail.setText("");
                    } else {
                        makeToast(Objects.requireNonNull(task.getException()).getMessage());
                        binding.buttonResetPassword.setClickable(true);
                    }
                    loading(false);
                });
    }

    private Boolean isValidSignUpDetails() {
        if (email.isEmpty()) {
            binding.inputUserEmail.setError("This field is required");
            binding.inputUserEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputUserEmail.setError("Invalid email address!");
            binding.inputUserEmail.requestFocus();
            return false;
        }
        return true;
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void makeToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
}