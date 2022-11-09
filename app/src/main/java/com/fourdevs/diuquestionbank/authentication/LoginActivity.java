package com.fourdevs.diuquestionbank.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fourdevs.diuquestionbank.MainActivity;
import com.fourdevs.diuquestionbank.WelcomeActivity;
import com.fourdevs.diuquestionbank.databinding.ActivityLoginBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private String email, password;
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
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

            if (isValidSignUpDetails()) {
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
                startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class))
        );


    }

    private void logIn() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user!=null && user.isEmailVerified()) {
                        makeToast("Successfully Logged In");
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putSting(Constants.KEY_USER_ID, user.getUid());
                        preferenceManager.putSting(Constants.KEY_EMAIL, email);
                        preferenceManager.putBoolean(Constants.KEY_READ_ONCE, false);
                        preferenceManager.putBoolean(Constants.KEY_COUNT_ONCE, false);
                        checkIsVerified();
                        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainActivityIntent);
                    }
                    else {
                        makeToast("Please verify your email address and try again");
                        FirebaseAuth.getInstance().signOut();
                        binding.buttonLogIn.setClickable(true);
                    }
                    loading(false);
                }).addOnFailureListener(e -> {
                    makeToast(e.getMessage());
                    loading(false);
                    binding.buttonLogIn.setClickable(true);
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
        } else if (password.isEmpty()) {
            binding.inputUserPassword.setError("This field is required");
            binding.inputUserPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void checkIsVerified() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        if(Boolean.FALSE.equals(documentSnapshot.getBoolean(Constants.KEY_IS_VERIFIED))) {
                            updateVerificationInfo();
                        }
                    }
                });
    }

    private void updateVerificationInfo(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_IS_VERIFIED, true);
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