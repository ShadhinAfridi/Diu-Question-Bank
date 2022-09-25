package com.fourdevs.diuquestionbank;

import androidx.recyclerview.widget.GridLayoutManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;
import com.fourdevs.diuquestionbank.adapter.CourseAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityCoursesBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CoursesActivity extends BaseActivity implements CourseListener {

    private ActivityCoursesBinding binding;
    private String departmentName;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoursesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        departmentName = intent.getStringExtra("departmentName");
        database = FirebaseFirestore.getInstance();

        getCourses();
        setSpinners();
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
        binding.buttonOk.setOnClickListener(view -> filterResults());
    }


    private void setSpinners() {
        Calendar calendar = Calendar.getInstance();
        List<String> date_list = new ArrayList<>();
        date_list.add("Select Year");
        int current_year = calendar.get(Calendar.YEAR);
        String add_year;


        for(int i=current_year; i>=2015; i--){
            add_year = Integer.toString(i);
            date_list.add(add_year);
        }

        ArrayAdapter<CharSequence> year_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, date_list);
        year_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.yearSpinner.setAdapter(year_adapter);

        ArrayAdapter<CharSequence> exam_adapter = ArrayAdapter
                .createFromResource(this, R.array.exam_name, android.R.layout.simple_spinner_item);
        exam_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.examSpinner.setAdapter(exam_adapter);
    }

    private void getCourses() {
        binding.courseProgressBar.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_DEPARTMENT, departmentName)
                .whereEqualTo(Constants.KEY_IS_APPROVED, true)
                .get()
                .addOnCompleteListener(task -> {
                    List<Course> courses = new ArrayList<>();

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

    private void filterResults() {
        String year = binding.yearSpinner.getSelectedItem().toString().trim();
        String exam = binding.examSpinner.getSelectedItem().toString().trim();
        String semester;
        if(binding.semesterGroup.getCheckedRadioButtonId() == -1){
            semester = "";
        } else {
            int selectedSemesterId = binding.semesterGroup.getCheckedRadioButtonId();
            RadioButton semesterBtn =  findViewById(selectedSemesterId);
            semester = semesterBtn.getText().toString().trim();
        }
        binding.constraintLayout2.setVisibility(View.GONE);
        binding.courseProgressBar.setVisibility(View.VISIBLE);

        if (year.equals("Select Year")){
            makeToast("Select all field!");
            return;
        }
        if (exam.equals("Select Exam")) {
            makeToast("Select all field!");
            return;
        }
        if (semester.equals("")){
            makeToast("Select all field!");
            return;
        }

        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_DEPARTMENT, departmentName)
                .whereEqualTo(Constants.KEY_YEAR, year)
                .whereEqualTo(Constants.KEY_SEMESTER, semester)
                .whereEqualTo(Constants.KEY_EXAM, exam)
                .get()
                .addOnCompleteListener(task -> {
                    List<Course> courses = new ArrayList<>();

                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Course course = new Course();
                        course.departmentName = queryDocumentSnapshot.getString(Constants.KEY_DEPARTMENT);
                        course.courseName = queryDocumentSnapshot.getString(Constants.KEY_COURSE_CODE);
                        course.semester = queryDocumentSnapshot.getString(Constants.KEY_SEMESTER);
                        course.year = queryDocumentSnapshot.getString(Constants.KEY_YEAR);
                        course.fileUrl = queryDocumentSnapshot.getString(Constants.KEY_PDF_URL);
                        courses.add(course);
                    }
                    courses.sort(Comparator.comparing(obj -> obj.fileUrl));
                    if (courses.size() > 0) {
                        CourseAdapter courseAdapter1 = new CourseAdapter(courses, this, this);
                        binding.courseRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
                        binding.courseRecyclerView.setAdapter(courseAdapter1);
                        binding.courseRecyclerView.setVisibility(View.VISIBLE);
                        binding.courseProgressBar.setVisibility(View.GONE);
                        binding.linearLayout.setVisibility(View.GONE);
                    } else {
                        binding.courseRecyclerView.setVisibility(View.GONE);
                        binding.courseProgressBar.setVisibility(View.GONE);
                        binding.linearLayout.setVisibility(View.VISIBLE);
                    }
                });

    }

    private void makeToast(String value) {
        Toast.makeText(getApplicationContext(),value,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCourseClicked(Course course) {
        Intent intent = new Intent(getApplicationContext(), PdfViewerActivity.class);
        intent.putExtra(Constants.KEY_NAME, course.courseName+" "+course.semester+"("+course.year+")");
        intent.putExtra(Constants.KEY_PDF_URL, course.fileUrl);
        startActivity(intent);
    }
}