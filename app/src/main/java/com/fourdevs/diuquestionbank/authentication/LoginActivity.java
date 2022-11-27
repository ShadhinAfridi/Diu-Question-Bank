package com.fourdevs.diuquestionbank.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.MainActivity;
import com.fourdevs.diuquestionbank.WelcomeActivity;
import com.fourdevs.diuquestionbank.databinding.ActivityLoginBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.fourdevs.diuquestionbank.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private String email, password;
    private PreferenceManager preferenceManager;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());
        binding.textResetPassword.setClickable(true);
        setListeners();
    }

    private void setListeners() {
        binding.buttonSignUp.setOnClickListener(view -> {
            Intent signUpIntent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(signUpIntent);
            finish();
        });
        binding.iconBack.setOnClickListener(view -> {
            Intent welcomeIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(welcomeIntent);
            finish();
        });
        binding.buttonLogIn.setOnClickListener(view -> {
            email = Objects.requireNonNull(binding.inputUserEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.inputUserPassword.getText()).toString().trim();

            if (isValidSignInDetails()) {
                binding.buttonLogIn.setClickable(false);
                loading(true);
                logIn();
            }
        });
        binding.imagePasswordInvisible.setOnClickListener(view -> {
            binding.inputUserPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            binding.inputUserPassword.setTransformationMethod(null);
            binding.imagePasswordInvisible.setVisibility(View.INVISIBLE);
            binding.imagePasswordVisible.setVisibility(View.VISIBLE);
            binding.inputUserPassword.setSelection(Objects.requireNonNull(binding.inputUserPassword.getText()).length());
        });
        binding.imagePasswordVisible.setOnClickListener(view -> {
            binding.inputUserPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.inputUserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            binding.imagePasswordVisible.setVisibility(View.INVISIBLE);
            binding.imagePasswordInvisible.setVisibility(View.VISIBLE);
            binding.inputUserPassword.setSelection(Objects.requireNonNull(binding.inputUserPassword.getText()).length());
        });

        binding.textResetPassword.setOnClickListener(view ->
                startActivity(new Intent(this, ResetPasswordActivity.class))
        );


    }

    private void logIn() {
        authViewModel.logIn(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user!=null) {
                        preferenceManager.putSting(Constants.KEY_USER_ID, user.getUid());
                        preferenceManager.putSting(Constants.KEY_EMAIL, email);

                        if(user.isEmailVerified()) {
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putBoolean(Constants.KEY_READ_ONCE, false);
                            preferenceManager.putBoolean(Constants.KEY_COUNT_ONCE, false);
                            makeToast("Successfully Logged In");
                            checkIsVerified();
                            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainActivityIntent);
                        }
                        else {
                            preferenceManager.putBoolean(Constants.KEY_IS_VERIFICATION_PAGE, true);
                            Intent intent = new Intent(getApplicationContext(), VerificationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }
                    loading(false);
                }).addOnFailureListener(e -> {
                    makeToast(e.getMessage());
                    loading(false);
                    binding.buttonLogIn.setClickable(true);
                });
    }

    private Boolean isValidSignInDetails() {
        if (email.isEmpty()) {
            binding.inputUserEmail.setError("This field is required");
            binding.inputUserEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputUserEmail.setError("Invalid email address!");
            binding.inputUserEmail.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            binding.inputUserPassword.setError("This field is required");
            binding.inputUserPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void checkIsVerified() {
        authViewModel.checkIsVerified();
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