package com.fourdevs.diuquestionbank.repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.models.User;
import com.fourdevs.diuquestionbank.room.AppDatabase;
import com.fourdevs.diuquestionbank.room.UserDao;
import com.fourdevs.diuquestionbank.utilities.AsyncTasks;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SharedRepository {

    private final UserDao userDao;

    public SharedRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        userDao = database.userDao();
    }

    public LiveData<User> getUserData(String userId) {
        return userDao.getUserInfo(userId);
    }

    public void insert(User user) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.Insert(user);
        });
    }

    public void getOnlineUserData(String userId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            User user = new User();
                            user.userId = userId;
                            user.profilePicture = queryDocumentSnapshot.getString(Constants.KEY_PROFILE_PICTURE);
                            user.userName = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            insert(user);
                        }
                    }

                });
    }

}
