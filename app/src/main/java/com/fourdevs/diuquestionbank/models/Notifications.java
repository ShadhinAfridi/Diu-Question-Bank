package com.fourdevs.diuquestionbank.models;

import java.io.Serializable;
import java.util.Date;

public class Notifications implements Serializable {
    public String title, message, link, dateTime;
    public Date dateObject;
}