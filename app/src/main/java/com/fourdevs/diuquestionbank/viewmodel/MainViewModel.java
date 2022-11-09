package com.fourdevs.diuquestionbank.viewmodel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import androidx.lifecycle.ViewModel;
import com.fourdevs.diuquestionbank.repository.MainRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

public class MainViewModel extends ViewModel {
    private final MainRepository repository;

    public MainViewModel(MainRepository mainRepository) {
        this.repository = mainRepository;
    }

    public Task<QuerySnapshot> updateUserData() {
        return repository.getUserData();
    }

    public void getToken() {
        repository.getToken();
    }

    public Task<Void> logOut() {
        return repository.logOut();
    }

    public Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
