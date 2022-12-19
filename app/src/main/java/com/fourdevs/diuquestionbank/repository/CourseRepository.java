package com.fourdevs.diuquestionbank.repository;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.room.AppDatabase;
import com.fourdevs.diuquestionbank.room.QuestionsDao;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseRepository {

    private final QuestionsDao questionsDao;
    private final LiveData<List<Course>> allCourses;


    public CourseRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        questionsDao = database.questionsDao();
        allCourses = questionsDao.getAllCourses();
    }

    public LiveData<List<Course>> getAllCourses() {
        return allCourses;
    }
    public LiveData<List<Course>> getCourses(String department, String exam) {
        return questionsDao.getCourses(department, exam);
    }
    public LiveData<List<Course>> getSearchedCourses(String department, String courseCode) {
        return questionsDao.getSearchedCourses(department, courseCode);
    }
    public LiveData<List<Course>> getUserUploads(String userId) {
        return questionsDao.getUserUploads(userId);
    }

    public void insert(Course course) {
        AppDatabase.databaseWriteExecutor.execute(() -> questionsDao.Insert(course));
    }

    public void update(Course course) {
        AppDatabase.databaseWriteExecutor.execute(() -> questionsDao.Update(course));
    }

    public void networkCourse(String department, String exam) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_IS_APPROVED, true)
                .whereEqualTo(Constants.KEY_DEPARTMENT, department)
                .whereEqualTo(Constants.KEY_EXAM, exam)
                .addSnapshotListener(eventListener);
    }

    public void networkUserCourse(String userId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error !=null){
            return;
        }
        if(value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {

                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Course course = new Course();
                    course.courseId = documentChange.getDocument().getId();
                    course.departmentName = documentChange.getDocument().getString(Constants.KEY_DEPARTMENT);
                    course.courseName = documentChange.getDocument().getString(Constants.KEY_COURSE_CODE);
                    course.semester = documentChange.getDocument().getString(Constants.KEY_SEMESTER);
                    course.year = documentChange.getDocument().getString(Constants.KEY_YEAR);
                    course.fileUrl = documentChange.getDocument().getString(Constants.KEY_PDF_URL);
                    course.exam = documentChange.getDocument().getString(Constants.KEY_EXAM);
                    course.userId = documentChange.getDocument().getString(Constants.KEY_USER_ID);
                    course.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    course.approved = documentChange.getDocument().getBoolean(Constants.KEY_IS_APPROVED);
                    this.insert(course);
                    this.update(course);
                }
            }
        }
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date);
    }
}
