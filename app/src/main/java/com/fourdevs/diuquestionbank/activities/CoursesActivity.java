package com.fourdevs.diuquestionbank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.fourdevs.diuquestionbank.adapter.CourseAdapter;
import com.fourdevs.diuquestionbank.adapter.CourseDiff;
import com.fourdevs.diuquestionbank.databinding.ActivityCoursesBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.AsyncTasks;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.viewmodel.CourseViewModel;

public class CoursesActivity extends BaseActivity implements CourseListener {

    private ActivityCoursesBinding binding;
    private String departmentName, examName;
    private CourseViewModel courseViewModel;
    private CourseAdapter courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        courseViewModel = new ViewModelProvider(this).get(CourseViewModel.class);
        Intent intent = getIntent();
        departmentName = intent.getStringExtra(Constants.KEY_DEPARTMENT);
        examName = intent.getStringExtra(Constants.KEY_EXAM);
        setListeners();
        setAdapter();
    }



    private void setAdapter() {
        courseAdapter = new CourseAdapter(new CourseDiff(),this,this);
        binding.courseRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.courseRecyclerView.setAdapter(courseAdapter);
        courseViewModel.getCourse(departmentName, examName).observe(this, it->{
            courseAdapter.submitList(it);
            if(it.size()>=1) {
                binding.nothingFound.setVisibility(View.GONE);
                binding.courseProgressBar.setVisibility(View.GONE);
            } else {
                binding.nothingFound.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
        binding.iconFilter.setOnClickListener(view -> toggle(true));
        binding.searchButton.setOnClickListener(view-> {
            String courseCode = binding.courseCodeEt.getText().toString().trim().toUpperCase();
            if(courseCode.isEmpty()) {
                binding.courseCodeEt.setError("Enter a course code");
                binding.courseCodeEt.requestFocus();
            } else {
                filter(courseCode);
            }
        });
        binding.courseRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(binding.constraintLayout2.getVisibility() == View.VISIBLE) {
                    toggle(false);
                }
            }
        });
    }

    private void toggle(boolean show) {
        Transition transition = new Slide(Gravity.TOP);
        transition.setDuration(500);
        transition.addTarget(binding.constraintLayout2);
        TransitionManager.beginDelayedTransition(binding.constraintLayout1, transition);
        binding.constraintLayout2.setVisibility(show ? View.VISIBLE : View.GONE);
        if(show) {
            binding.iconFilter.setVisibility(View.INVISIBLE);
        } else {
            new Handler().postDelayed(() -> binding.iconFilter.setVisibility(View.VISIBLE),500);
        }
    }



    private void filter(String courseCode) {
        courseViewModel.getSearchedCourse(departmentName, courseCode).observe(this, courseAdapter::submitList);
    }


    @Override
    public void onCourseClicked(Course course) {
        Intent intent = new Intent(getApplicationContext(), PdfViewerActivity.class);
        intent.putExtra(Constants.KEY_NAME, course.courseName+" "+course.semester+"("+course.year+")");
        intent.putExtra(Constants.KEY_PDF_URL, course.fileUrl);
        intent.putExtra(Constants.KEY_UPLOAD_DATE, course.dateTime);
        intent.putExtra(Constants.KEY_USER_ID, course.userId);
        startActivity(intent);
    }
}