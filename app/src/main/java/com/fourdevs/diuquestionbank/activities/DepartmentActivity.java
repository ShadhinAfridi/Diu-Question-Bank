package com.fourdevs.diuquestionbank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.adapter.CourseDiff;
import com.fourdevs.diuquestionbank.adapter.DepartmentAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityDepartmentBinding;
import com.fourdevs.diuquestionbank.listeners.DepartmentListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.AsyncTasks;
import com.fourdevs.diuquestionbank.viewmodel.CourseViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DepartmentActivity extends BaseActivity implements DepartmentListener{
    private ActivityDepartmentBinding binding;
    private CourseViewModel courseViewModel;
    private DepartmentAdapter departmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepartmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        courseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);

        callNetwork();
        setListeners();
        setAdapters();
        getData();
    }

    private void callNetwork() {
        binding.departmentProgressBar.setVisibility(View.VISIBLE);

        new AsyncTasks() {
            @Override
            public void doInBackground() {
                courseViewModel.networkCourse();
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();
    }

    private void getData() {
        courseViewModel.getAllCourse().observe(this, it->{
            Course course;
            List <String> departments = new ArrayList<>();
            List<Course> courses = new ArrayList<>();

            for(int i=0; i<it.size(); i++) {
                course = it.get(i);
                departments.add(course.departmentName);
            }
            Set<String> setDepartments = new HashSet<>(departments);
            departments.clear();
            departments.addAll(setDepartments);
            Collections.sort(departments);

            if(it.size()>0) {
                binding.departmentProgressBar.setVisibility(View.GONE);
            }

            for(int i=0; i<departments.size(); i++) {
                Course courseNew = new Course();
                courseNew.departmentName = departments.get(i);
                courses.add(courseNew);
            }
            departmentAdapter.submitList(courses);
        });

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
        Intent intent = new Intent(getApplicationContext(), CoursesActivity.class);
        intent.putExtra("departmentName",department);
        startActivity(intent);
    }
}