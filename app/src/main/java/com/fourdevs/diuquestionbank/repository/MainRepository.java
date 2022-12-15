package com.fourdevs.diuquestionbank.repository;

import android.app.Application;

import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class MainRepository {
    private final PreferenceManager preferenceManager;

    public MainRepository(Application application){
        this.preferenceManager = new PreferenceManager(application.getApplicationContext());
    }

    public Task<QuerySnapshot> getUserData() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        return database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putSting(Constants.KEY_USER_ID, documentSnapshot.getString(Constants.KEY_USER_ID));
                        preferenceManager.putSting(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putSting(Constants.KEY_PROFILE_PICTURE, documentSnapshot.getString(Constants.KEY_PROFILE_PICTURE));
                        preferenceManager.putBoolean(Constants.KEY_READ_ONCE, true);
                    }
                });
    }
}
