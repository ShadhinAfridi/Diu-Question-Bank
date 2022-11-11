package com.fourdevs.diuquestionbank.repository;

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

    public MainRepository(PreferenceManager preferenceManager){
        this.preferenceManager = preferenceManager;
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
                        preferenceManager.putSting(Constants.KEY_COUNT_APPROVED, documentSnapshot.getString(Constants.KEY_COUNT_APPROVED));
                        preferenceManager.putSting(Constants.KEY_COUNT_REJECTED, documentSnapshot.getString(Constants.KEY_COUNT_REJECTED));
                        preferenceManager.putSting(Constants.KEY_COUNT_UPLOAD, documentSnapshot.getString(Constants.KEY_COUNT_UPLOAD));
                        preferenceManager.putBoolean(Constants.KEY_READ_ONCE, true);
                    }
                });

    }

    public Task<Void> logOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        return documentReference.update(updates);
    }



}
