package com.fourdevs.diuquestionbank.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fourdevs.diuquestionbank.utilities.Constants;

import java.io.Serializable;

@Entity(tableName = Constants.KEY_COLLECTION_USERS)
public class User implements Serializable {
    public String userName;
    public String profilePicture;

    @PrimaryKey
    @NonNull
    public String userId = null;

    public User() {

    }

    public User getUser() {
        User user = new User();
        user.userName = this.userName;
        user.profilePicture = this.profilePicture;
        user.userId = this.userId;
        return user;
    }
}
