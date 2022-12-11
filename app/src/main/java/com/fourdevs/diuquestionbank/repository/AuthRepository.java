package com.fourdevs.diuquestionbank.repository;

import android.app.Application;

import com.fourdevs.diuquestionbank.room.AppDatabase;
import com.fourdevs.diuquestionbank.room.QuestionsDao;
import com.fourdevs.diuquestionbank.room.UserDao;
import com.fourdevs.diuquestionbank.utilities.AsyncTasks;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class AuthRepository {

    private final FirebaseFirestore database;
    private final PreferenceManager preferenceManager;
    private final FirebaseAuth auth;
    private FirebaseUser user;
    private final QuestionsDao questionsDao;
    private final UserDao userDao;


    public AuthRepository(Application application) {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(application.getApplicationContext());
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        questionsDao = appDatabase.questionsDao();
        userDao = appDatabase.userDao();

    }

    public Task<Void> checkCurrentPassword(String currentPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(preferenceManager.getString(Constants.KEY_EMAIL),currentPassword);
        return user.reauthenticate(credential);
    }

    public Task<Void> changePassword(String newPassword) {
        return user.updatePassword(newPassword);
    }

    public Task<Void> changeProfilePicture(String encodedImage) {
        return database
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID)
                ).update(Constants.KEY_PROFILE_PICTURE, encodedImage);
    }

    public void checkIsVerified() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        if(Boolean.FALSE.equals(documentSnapshot.getBoolean(Constants.KEY_IS_VERIFIED))) {
                            updateVerificationInfo();
                        }
                    }
                });
    }

    public void updateVerificationInfo(){
        database
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID)
                ).update(Constants.KEY_IS_VERIFIED, true);
    }

    public Task<AuthResult> logIn(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signUp(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> addSignUpToDb(HashMap<String, Object> user, String userId) {
        return database.collection(Constants.KEY_COLLECTION_USERS).document(userId).set(user);
    }

    public Task<Void> updateUserName(String userName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build();
        if(user==null) {
            user = auth.getCurrentUser();
        }
        assert user != null;
        return user.updateProfile(profileUpdates);
    }

    public Task<Void> sendVerificationEmail() {
        return user.sendEmailVerification();
    }

    public Task<Void> sendResetPasswordEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public Task<Void> deleteFcmToken() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        return documentReference.update(updates);
    }

    public void logOut() {
        new AsyncTasks() {
            @Override
            public void doInBackground() {
                questionsDao.DeleteAllCourse();
                userDao.DeleteAllUsers();
                preferenceManager.clear();
            }

            @Override
            public void onPostExecute() {
                FirebaseAuth.getInstance().signOut();
            }
        }.execute();
    }

    public void getToken(){
        new AsyncTasks() {
            @Override
            public void doInBackground() {
                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(AuthRepository.this::updateToken);
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();
    }

    public void updateToken(String token){
        new AsyncTasks() {
            @Override
            public void doInBackground() {
                database
                        .collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .update(Constants.KEY_FCM_TOKEN, token);
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();
    }

    public void updateUserActivity(int value) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        if(userId != null) {
            database.collection(Constants.KEY_COLLECTION_USERS).document(
                    preferenceManager.getString(Constants.KEY_USER_ID)
            ).update(Constants.KEY_AVAILABILITY, value);
        }
    }

}
