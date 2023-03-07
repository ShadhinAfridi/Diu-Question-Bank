package com.fourdevs.diuquestionbank.activities;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.MyApplication;
import com.fourdevs.diuquestionbank.authentication.LoginActivity;
import com.fourdevs.diuquestionbank.databinding.ActivityMainBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.fourdevs.diuquestionbank.viewmodel.MainViewModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;


public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private static final int PERMISSION_REQUEST_CODE = 7;
    private MainViewModel viewModel;
    private Application application;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setAds();
        application = getApplication();
        if (!(application instanceof MyApplication)) {
            Log.e("Afridi", "Failed to cast application to MyApplication.");
            return;
        }
        ((MyApplication) application).loadAd(this);
        preferenceManager = new PreferenceManager(getApplicationContext());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        if(!preferenceManager.getBoolean(Constants.KEY_READ_ONCE)) {
            Task<QuerySnapshot> querySnapshotTask = viewModel.updateUserData();
            querySnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> setUserData())
                    .addOnFailureListener(e -> Log.d("DIU Update Data", e.getMessage()));
        }
        if(!checkPermissions()) {
            askPermission();
        }

        setUserData();
        setListeners();
        showAds();
    }

    private void loadReport() {

    }

    private void setUserData() {
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        if(preferenceManager.getString(Constants.KEY_PROFILE_PICTURE) != null) {
            binding.profileImage.setImageBitmap(
                    viewModel.getBitmapFromEncodedString(preferenceManager
                            .getString(Constants.KEY_PROFILE_PICTURE)));
        }
    }

    private void setListeners() {
        binding.cardQuestion.setOnClickListener(view -> {
            Intent questionIntent = new Intent(MainActivity.this, DepartmentActivity.class);
            startActivity(questionIntent);
        });
        binding.cardUpload.setOnClickListener(view -> {
            Intent uploadIntent = new Intent(MainActivity.this, QuestionUploadActivity.class);
            startActivity(uploadIntent);
        });
        binding.iconLogout.setOnClickListener(view -> logOut());

        binding.profileImage.setOnClickListener(view -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        binding.userName.setOnClickListener(view -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        binding.userEmail.setOnClickListener(view -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        binding.cardReward.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RewardActivity.class);
            startActivity(intent);
        });
        binding.cardHelp.setOnClickListener(view -> {
            Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(helpIntent);
        });
        binding.cardNotice.setOnClickListener(view -> makeToast("Coming Soon...."));
    }

    private void logOut() {
        makeToast("Signing Out...");
        viewModel.deleteFcmToken()
                .addOnSuccessListener(unused -> {
                    viewModel.logOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                });
    }

    private Boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeToast("Permission Granted");
            }
        } else {
            makeToast("Permission Denied");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
    }

    private void showAds() {
        binding.adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        });

        new CountDownTimer(8000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e("Afridi", ((MyApplication) application).isAdAvailable().toString());
            }

            @Override
            public void onFinish() {
                ((MyApplication) application)
                        .showAdIfAvailable(
                                MainActivity.this,
                                () -> loadReport());
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

}