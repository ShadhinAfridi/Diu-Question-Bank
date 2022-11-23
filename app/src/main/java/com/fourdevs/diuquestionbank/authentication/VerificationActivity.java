package com.fourdevs.diuquestionbank.authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.fourdevs.diuquestionbank.MainActivity;
import com.fourdevs.diuquestionbank.R;
import com.fourdevs.diuquestionbank.databinding.ActivityVerificationBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.fourdevs.diuquestionbank.viewmodel.AuthViewModel;
import com.fourdevs.diuquestionbank.viewmodel.MainModelFactory;
import com.fourdevs.diuquestionbank.viewmodel.MainViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerificationActivity extends AppCompatActivity {
    private ActivityVerificationBinding binding;
    private AuthViewModel viewModel;
    private PreferenceManager preferenceManager;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null && user.isEmailVerified()) {
            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
            preferenceManager.putBoolean(Constants.KEY_READ_ONCE, false);
            preferenceManager.putBoolean(Constants.KEY_COUNT_ONCE, false);
            makeToast("Successfully Logged In");
            checkIsVerified();
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivityIntent);
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
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
        viewModel.countDownStart();
        viewModel.time().observe(this, time -> binding.buttonResendVerification.setText(time));
        viewModel.finished().observe(this, aBoolean -> {
            if(aBoolean) {
                makeClickable();
            }
        });
    }


    private void sendVerificationEmail() {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            makeToast("We've sent a verification email to "+preferenceManager.getString(Constants.KEY_EMAIL)+". Please verify your email");
                        } else {
                            makeClickable();
                            makeToast("Unable to send verification email.");
                        }
                    });
        }
    }

    @SuppressLint("SetTextI18n")
    private void makeClickable() {
        binding.buttonResendVerification.setClickable(true);
        binding.buttonResendVerification.setText("Resend");
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        preferenceManager.clear();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
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

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }
}