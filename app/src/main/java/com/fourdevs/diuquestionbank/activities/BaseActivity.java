package com.fourdevs.diuquestionbank.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.authentication.LoginActivity;
import com.fourdevs.diuquestionbank.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getToken();
    }


    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null;
    }
    private void checkCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            logOut();
                        }
                    });
        } else {
            logOut();
        }
    }
    private void logOut() {
        authViewModel.deleteFcmToken()
                .addOnSuccessListener(unused -> {
                    authViewModel.logOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        authViewModel.updateUserActivity(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isInternetAvailable()){
            checkCurrentUser();
            authViewModel.updateUserActivity(1);
        }
    }
}
