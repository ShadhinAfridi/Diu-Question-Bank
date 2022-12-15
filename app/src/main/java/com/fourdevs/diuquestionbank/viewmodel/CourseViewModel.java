package com.fourdevs.diuquestionbank.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.repository.CourseRepository;

import java.util.List;

public class CourseViewModel extends AndroidViewModel {

    private final CourseRepository repository;
    private final LiveData<List<Course>> allCourse;

    public CourseViewModel(Application application) {
        super(application);
        repository = new CourseRepository(application);
        allCourse = repository.getAllCourses();

    }

    public LiveData<List<Course>> getAllCourse() {
        return allCourse;
    }
    public LiveData<List<Course>> getCourse(String department, String exam) {
        return repository.getCourses(department, exam);
    }
    public LiveData<List<Course>> getSearchedCourse(String department, String courseCode) {
        return repository.getSearchedCourses(department, courseCode);
    }

    public void networkCourse(String department, String exam) {
        repository.networkCourse(department, exam);
    }

}
