package com.fourdevs.diuquestionbank.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.models.User;
import com.fourdevs.diuquestionbank.utilities.Constants;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void Insert(User user);

    @Delete
    void Delete(User user);

    @Query("DELETE FROM "+ Constants.KEY_COLLECTION_USERS)
    void DeleteAllUsers();

    @Query("SELECT * FROM "+ Constants.KEY_COLLECTION_USERS+" where userId = :userId ")
    LiveData<User> getUserInfo(String userId);

}
