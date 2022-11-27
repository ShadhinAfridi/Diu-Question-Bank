package com.fourdevs.diuquestionbank.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import com.fourdevs.diuquestionbank.repository.MainRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

public class MainViewModel extends AndroidViewModel {
    private final MainRepository repository;

    public MainViewModel(Application application) {
        super(application);
        this.repository = new MainRepository(application);
    }

    public Task<QuerySnapshot> updateUserData() {
        return repository.getUserData();
    }

    public Task<Void> logOut() {
        return repository.logOut();
    }

    public Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
