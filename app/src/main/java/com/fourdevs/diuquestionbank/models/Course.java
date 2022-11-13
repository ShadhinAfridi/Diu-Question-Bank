package com.fourdevs.diuquestionbank.models;

import java.io.Serializable;
import java.util.Date;

public class Course implements Serializable {
    public String departmentName, courseName, semester, year, fileUrl, exam, courseId, userId, dateTime;
    public Boolean approved;
    public Date dateObject;
}
