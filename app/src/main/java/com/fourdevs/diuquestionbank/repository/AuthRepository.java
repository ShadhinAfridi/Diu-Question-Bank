package com.fourdevs.diuquestionbank.repository;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AuthRepository {

    private final FirebaseFirestore database;
    private final PreferenceManager preferenceManager;
    private final FirebaseAuth auth;
    private FirebaseUser user;


    public AuthRepository(Application application) {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(application.getApplicationContext());
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();

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

    public FirebaseUser getCurrentUser() {
        return user;
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

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public Task<Void> sendResetPasswordEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }



}
