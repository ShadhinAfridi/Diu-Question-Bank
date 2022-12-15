package com.fourdevs.diuquestionbank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.R;
import com.fourdevs.diuquestionbank.adapter.CourseDiff;
import com.fourdevs.diuquestionbank.adapter.DepartmentAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityDepartmentBinding;
import com.fourdevs.diuquestionbank.listeners.DepartmentListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.AsyncTasks;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.viewmodel.CourseViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DepartmentActivity extends BaseActivity implements DepartmentListener{
    private ActivityDepartmentBinding binding;
    private DepartmentAdapter departmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepartmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        setAdapters();
        getData();
    }

    private void getData() {
        List<Course> courses = new ArrayList<>();
        String[] departmentArray = getResources().getStringArray(R.array.department_name);
        for (String s : departmentArray) {
            Course courseNew = new Course();
            courseNew.departmentName = s;
            courses.add(courseNew);
        }
        departmentAdapter.submitList(courses);
    }

    private void setAdapters() {
        departmentAdapter = new DepartmentAdapter(new CourseDiff(), this);
        binding.departmentRecyclerView.setAdapter(departmentAdapter);
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void onDepartmentClicked(String department) {
        Intent intent = new Intent(getApplicationContext(), ExamNameActivity.class);
        intent.putExtra(Constants.KEY_DEPARTMENT, department);
        startActivity(intent);
    }
}