package com.fourdevs.diuquestionbank.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.fourdevs.diuquestionbank.viewmodel.AuthViewModel;
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthViewModel authViewModel;
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.getToken();
    }
}
