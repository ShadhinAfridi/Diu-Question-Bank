package com.fourdevs.diuquestionbank;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import com.fourdevs.diuquestionbank.adapter.CourseAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityCoursesBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CoursesActivity extends BaseActivity implements CourseListener {

    private ActivityCoursesBinding binding;
    private String departmentName;
    private FirebaseFirestore database;
    private List<Course> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        departmentName = intent.getStringExtra("departmentName");
        database = FirebaseFirestore.getInstance();
        getCourses();
        setListeners();
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
        binding.iconFilter.setOnClickListener(view -> {
            if (binding.constraintLayout2.getVisibility() == View.GONE) {
                binding.constraintLayout2.setVisibility(View.VISIBLE);
            } else {
                binding.constraintLayout2.setVisibility(View.GONE);
            }
        });
        binding.courseCodeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filter(charSequence.toString());
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void getCourses() {
        binding.courseProgressBar.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_DEPARTMENT, departmentName)
                .whereEqualTo(Constants.KEY_IS_APPROVED, true)
                .get()
                .addOnCompleteListener(task -> {
                    courses = new ArrayList<>();

                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Course course = new Course();
                        course.departmentName = queryDocumentSnapshot.getString(Constants.KEY_DEPARTMENT);
                        course.courseName = queryDocumentSnapshot.getString(Constants.KEY_COURSE_CODE);
                        course.semester = queryDocumentSnapshot.getString(Constants.KEY_SEMESTER);
                        course.year = queryDocumentSnapshot.getString(Constants.KEY_YEAR);
                        course.fileUrl = queryDocumentSnapshot.getString(Constants.KEY_PDF_URL);
                        course.exam = queryDocumentSnapshot.getString(Constants.KEY_EXAM);
                        courses.add(course);
                    }
                    courses.sort(Comparator.comparing(obj -> obj.fileUrl));
                    if (courses.size() > 0) {
                        CourseAdapter courseAdapter = new CourseAdapter(courses, this,this);
                        binding.courseRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                        binding.courseRecyclerView.setAdapter(courseAdapter);
                        binding.courseRecyclerView.setVisibility(View.VISIBLE);
                        binding.courseProgressBar.setVisibility(View.GONE);

                    }
                });
    }

    private void filter(String text) {
        List<Course> filterCourses = new ArrayList<>();
        for (Course course : courses) {
            if (course.courseName.toLowerCase().contains(text.toLowerCase())) {
                filterCourses.add(course);
            } else if (course.year.toLowerCase().contains(text.toLowerCase())) {
                filterCourses.add(course);
            } else if (course.exam.toLowerCase().contains(text.toLowerCase())) {
                filterCourses.add(course);
            } else if (course.semester.toLowerCase().contains(text.toLowerCase())) {
                filterCourses.add(course);
            }
        }
        if (filterCourses.isEmpty()) {
            makeToast();
        } else {
            CourseAdapter courseAdapter = new CourseAdapter(filterCourses, this,this);
            binding.courseRecyclerView.setAdapter(courseAdapter);
        }
    }



    private void makeToast() {
        Toast.makeText(getApplicationContext(), "No Data Found..",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCourseClicked(Course course) {
        Intent intent = new Intent(getApplicationContext(), PdfViewerActivity.class);
        intent.putExtra(Constants.KEY_NAME, course.courseName+" "+course.semester+"("+course.year+")");
        intent.putExtra(Constants.KEY_PDF_URL, course.fileUrl);
        startActivity(intent);
    }
}