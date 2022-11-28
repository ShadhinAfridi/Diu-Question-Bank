package com.fourdevs.diuquestionbank.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.fourdevs.diuquestionbank.utilities.Constants;
import java.io.Serializable;

@Entity(tableName = Constants.KEY_COLLECTION_QUESTIONS)
public class Course implements Serializable {

    public String courseName;
    public String departmentName;
    public String exam;
    public String semester;
    public String year;
    public String fileUrl;
    public String userId;
    public String dateTime;
    public Boolean approved;

    @PrimaryKey
    @NonNull
    public String courseId = null;

    public Course() {

    }

    public Course getCourse() {
        Course course = new Course();
        course.courseName = this.courseName;
        course.departmentName = this.departmentName;
        course.exam = this.exam;
        course.semester = this.semester;
        course.year = this.year;
        course.fileUrl = this.fileUrl;
        course.userId = this.userId;
        course.dateTime = this.dateTime;
        course.approved = this.approved;

        return course;
    }
}