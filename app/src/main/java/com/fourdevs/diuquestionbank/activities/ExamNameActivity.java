package com.fourdevs.diuquestionbank.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.fourdevs.diuquestionbank.databinding.ActivityExamNameBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;

public class ExamNameActivity extends AppCompatActivity {
    private ActivityExamNameBinding binding;
    private String departmentName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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


    private void goToCoursePage(String exam) {
        Intent intent = new Intent(getApplicationContext(), CoursesActivity.class);
        intent.putExtra(Constants.KEY_DEPARTMENT, departmentName);
        intent.putExtra(Constants.KEY_EXAM, exam);
        startActivity(intent);
    }
}