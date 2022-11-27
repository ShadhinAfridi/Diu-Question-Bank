package com.fourdevs.diuquestionbank.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.room.AppDatabase;
import com.fourdevs.diuquestionbank.room.QuestionsDao;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
    public LiveData<List<Course>> getCourses(String department) {
        return questionsDao.getCourses(department);
    }
    public LiveData<List<Course>> getSearchedCourses(String department, String courseCode) {
        return questionsDao.getSearchedCourses(department, courseCode);
    }
    public LiveData<List<Course>> getUserUploads(String userId) {
        return questionsDao.getUserUploads(userId);
    }

    public void insert(Course course) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            questionsDao.Insert(course);
        });
    }

    public void networkCourse() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_IS_APPROVED, true)
                .get()
                .addOnCompleteListener(this::getData);
    }

    public void networkUserCourse(String userId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(this::getData);
    }

    private void getData(Task<QuerySnapshot> task) {
        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
            Course course = new Course();
            course.courseId = queryDocumentSnapshot.getId();
            course.departmentName = queryDocumentSnapshot.getString(Constants.KEY_DEPARTMENT);
            course.courseName = queryDocumentSnapshot.getString(Constants.KEY_COURSE_CODE);
            course.semester = queryDocumentSnapshot.getString(Constants.KEY_SEMESTER);
            course.year = queryDocumentSnapshot.getString(Constants.KEY_YEAR);
            course.fileUrl = queryDocumentSnapshot.getString(Constants.KEY_PDF_URL);
            course.exam = queryDocumentSnapshot.getString(Constants.KEY_EXAM);
            course.userId = queryDocumentSnapshot.getString(Constants.KEY_USER_ID);
            course.dateTime = getReadableDateTime(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
            course.approved = queryDocumentSnapshot.getBoolean(Constants.KEY_IS_APPROVED);
            this.insert(course);
        }
    }


    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy- hh:mm a", Locale.getDefault()).format(date);
    }
}
