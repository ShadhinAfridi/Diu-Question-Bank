package com.fourdevs.diuquestionbank.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.fourdevs.diuquestionbank.PrivacyPolicyActivity;
import com.fourdevs.diuquestionbank.WelcomeActivity;
import com.fourdevs.diuquestionbank.databinding.ActivitySignupBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseFirestore database ;
    private FirebaseAuth mAuth;
    private String userName, email, password, confirmPassword, userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        setListeners();
    }

    private void setListeners() {
        binding.buttonLogIn.setOnClickListener(view -> {
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
            finish();
        });
        binding.iconBack.setOnClickListener(view -> {
            Intent welcomeIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(welcomeIntent);
        });

        binding.buttonSignUp.setOnClickListener(view -> {
            userName = Objects.requireNonNull(binding.inputUserName.getText()).toString().trim();
            email = Objects.requireNonNull(binding.inputUserEmail.getText()).toString().trim();
            password = Objects.requireNonNull(binding.inputUserPassword.getText()).toString().trim();
            confirmPassword = Objects.requireNonNull(binding.inputUserConfirmPassword.getText()).toString().trim();

            if (isValidSignUpDetails()) {
                binding.buttonSignUp.setClickable(false);
                loading(true);
                signUp();
                clearAll();
            }
        });

        binding.imagePasswordInvisible.setOnClickListener(view -> showPassword());
        binding.imageConfirmPasswordInvisible.setOnClickListener(view -> showPassword());
        binding.imagePasswordVisible.setOnClickListener(view -> hidePassword());
        binding.imageConfirmPasswordVisible.setOnClickListener(view -> hidePassword());
        binding.termsAndPrivacy.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

    }

    private Boolean isValidSignUpDetails() {
        if (userName.isEmpty()) {
            binding.inputUserName.setError("This field is required");
            binding.inputUserName.requestFocus();
            return false;
        } else if (email.isEmpty()) {
            binding.inputUserEmail.setError("This field is required");
            binding.inputUserEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputUserEmail.setError("Invalid email address!");
            binding.inputUserEmail.requestFocus();
            return false;
        } else if (!email.matches("[a-zA-Z0-9._-]+@diu.edu.bd")) {
            if(!email.matches("[a-zA-Z0-9._-]+@+[a-zA-Z0-9._-]+diu.edu.bd")) {
                binding.inputUserEmail.setError("DIU email address only!");
                binding.inputUserEmail.requestFocus();
            }
            return false;
        } else if (password.isEmpty()) {
            binding.inputUserPassword.setError("This field is required");
            binding.inputUserPassword.requestFocus();
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.inputUserPassword.setError("Password not match");
            binding.inputUserPassword.requestFocus();
            binding.inputUserConfirmPassword.setError("Password not match");
            return false;
        } else if (password.length() < 6){
            binding.inputUserPassword.setError("Minimum 6 characters required");
            binding.inputUserPassword.requestFocus();
            return false;
        }
        return true;
    }


    private void signUp() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUserName();
                    }
                }).addOnFailureListener(e -> {
                    makeToast(e.getMessage());
                    loading(false);
                });
    }

    private void updateUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build();

        assert user != null;
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addToDatabase();
                    }
                });
    }

    private void addToDatabase() {
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_USER_ID, userId);
        user.put(Constants.KEY_NAME, userName);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_IS_ADMIN, false);
        user.put(Constants.KEY_PROFILE_PICTURE, null);
        user.put(Constants.KEY_IS_VERIFIED, false);
        user.put(Constants.KEY_UPLOAD_COUNT, "0");
        user.put(Constants.KEY_APPROVE_COUNT, "0");
        user.put(Constants.KEY_REJECT_COUNT, "0");
        database.collection(Constants.KEY_COLLECTION_USERS).document(userId).set(user)
                .addOnSuccessListener(documentReference -> {
                    sendVerificationEmail();
                    makeToast("We've sent a verification email to "+email+". Please verify your email and Login");
                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    makeToast(exception.getMessage());
                });
        loading(false);
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // email sent
                            // after email is sent just logout the user and finish this activity
                            loading(false);
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                        }
                    });
        }
    }

    private void showPassword() {
        binding.inputUserPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        binding.inputUserConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        binding.imagePasswordInvisible.setVisibility(View.INVISIBLE);
        binding.imagePasswordVisible.setVisibility(View.VISIBLE);
        binding.imageConfirmPasswordInvisible.setVisibility(View.INVISIBLE);
        binding.imageConfirmPasswordVisible.setVisibility(View.VISIBLE);
        binding.inputUserPassword.setSelection(binding.inputUserPassword.getText().length());
        binding.inputUserConfirmPassword.setSelection(binding.inputUserConfirmPassword.getText().length());
    }

    private void hidePassword() {
        binding.inputUserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        binding.inputUserConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        binding.imagePasswordVisible.setVisibility(View.INVISIBLE);
        binding.imagePasswordInvisible.setVisibility(View.VISIBLE);
        binding.imageConfirmPasswordVisible.setVisibility(View.INVISIBLE);
        binding.imageConfirmPasswordInvisible.setVisibility(View.VISIBLE);
        binding.inputUserPassword.setSelection(binding.inputUserPassword.getText().length());
        binding.inputUserConfirmPassword.setSelection(binding.inputUserConfirmPassword.getText().length());
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

    private void clearAll() {
        binding.inputUserPassword.setText("");
        binding.inputUserConfirmPassword.setText("");
        binding.inputUserName.setText("");
        binding.inputUserEmail.setText("");
    }
}