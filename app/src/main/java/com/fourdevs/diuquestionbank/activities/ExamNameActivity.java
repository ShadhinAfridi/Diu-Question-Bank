package com.fourdevs.diuquestionbank.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.fourdevs.diuquestionbank.databinding.ActivityExamNameBinding;
import com.fourdevs.diuquestionbank.utilities.AsyncTasks;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.viewmodel.CourseViewModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

public class ExamNameActivity extends AppCompatActivity {
    private ActivityExamNameBinding binding;
    private String departmentName;
    private CourseViewModel courseViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setAds();
        courseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        Intent intent = getIntent();
        departmentName = intent.getStringExtra(Constants.KEY_DEPARTMENT);
        binding.textDepartment.setText(departmentName);
        setListeners();
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view-> {
            onBackPressed();
            finish();
        });

        binding.cardMidterm.setOnClickListener(view-> goToCoursePage("Midterm"));

        binding.cardFinal.setOnClickListener(view-> goToCoursePage("Final"));
    }

    private void netCallCourse(String nameExam) {
        new AsyncTasks() {
            @Override
            public void doInBackground() {
                courseViewModel.networkCourse(departmentName, nameExam);
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();
    }

    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);

        binding.adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        });
    }


    private void goToCoursePage(String exam) {
        Intent intent = new Intent(getApplicationContext(), CoursesActivity.class);
        intent.putExtra(Constants.KEY_DEPARTMENT, departmentName);
        intent.putExtra(Constants.KEY_EXAM, exam);
        netCallCourse(exam);
        startActivity(intent);
    }
}