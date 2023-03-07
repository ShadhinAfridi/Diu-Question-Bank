package com.fourdevs.diuquestionbank.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;

import java.util.List;

@Dao
public interface QuestionsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void Insert(Course course);

    @Update
    void Update(Course course);

    @Delete
    void Delete(Course course);

    @Query("DELETE FROM "+ Constants.KEY_COLLECTION_QUESTIONS)
    void DeleteAllCourse();

    @Query("SELECT * FROM "+ Constants.KEY_COLLECTION_QUESTIONS +" where departmentName = :department and exam = :exam and approved == 1 order by courseName")
    LiveData<List<Course>> getCourses(String department, String exam);

    @Query("SELECT courseName FROM "+ Constants.KEY_COLLECTION_QUESTIONS +" where courseId= :id")
    String getCourseId(String id);

    @Query("SELECT * FROM "+ Constants.KEY_COLLECTION_QUESTIONS+" where approved == 1 order by courseName")
    LiveData<List<Course>> getAllCourses();

    @Query("SELECT * FROM "+ Constants.KEY_COLLECTION_QUESTIONS +" where userId = :userId order by dateTime")
    LiveData<List<Course>> getUserUploads(String userId);

    @Query("SELECT * FROM "+ Constants.KEY_COLLECTION_QUESTIONS
            +" where departmentName = :department and approved == 1 and courseName Like '%' || :courseCode || '%' order by courseName")
    LiveData<List<Course>> getSearchedCourses(String department, String courseCode);
}


