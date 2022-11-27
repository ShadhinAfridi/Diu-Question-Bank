package com.fourdevs.diuquestionbank.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.CountDownTimer;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.repository.AuthRepository;
import com.fourdevs.diuquestionbank.repository.CourseRepository;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;

public class AuthViewModel extends AndroidViewModel {
    private final CourseRepository courseRepository;
    private final AuthRepository authRepository;
    private final MutableLiveData<String> _time;
    private final MutableLiveData<Boolean> _finished;



    public AuthViewModel(Application application) {
        super(application);
        courseRepository = new CourseRepository(application);
        authRepository = new AuthRepository(application);
        _time = new MutableLiveData<>();
        _finished = new MutableLiveData<>();
    }


    public LiveData<String> time() {
        return _time;
    }
    public LiveData<List<Course>> getUserUploads(String userId) {
        return courseRepository.getUserUploads(userId);
    }
    public LiveData<Boolean> finished() {
        return _finished;
    }

    public void countDownStart() {
        new CountDownTimer(180000, 100) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                _time.setValue(String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
            }
            @Override
            public void onFinish() {
                _finished.postValue(true);
            }
        }.start();
    }

    public void networkCourse(String userId) {
        courseRepository.networkUserCourse(userId);
    }

    //login
    public void checkIsVerified() {
        authRepository.checkIsVerified();
    }

    public Task<AuthResult> logIn(String email, String password) {
        return authRepository.logIn(email, password);
    }
    public Task<AuthResult> signUp(String email, String password) {
        return authRepository.signUp(email, password);
    }

    public Task<Void> addSignUpToDb(HashMap<String, Object> user, String userId) {
        return authRepository.addSignUpToDb(user, userId);
    }

    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public Task<Void> updateUserName(String userName) {
        return authRepository.updateUserName(userName);
    }

    public Task<Void> sendVerificationEmail() {
        return authRepository.sendVerificationEmail();
    }

    public void logOut() {
        authRepository.logOut();
    }

    public Task<Void> sendResetPasswordEmail(String email) {
        return authRepository.sendResetPasswordEmail(email);
    }

    public Task<Void> changePassword(String newPassword) {
        return authRepository.changePassword(newPassword);
    }

    public Task<Void> changeProfilePicture(String encodedImage) {
        return authRepository.changeProfilePicture(encodedImage);
    }

    public Task<Void> checkCurrentPassword(String currentPassword) {
        return authRepository.checkCurrentPassword(currentPassword);
    }

}
