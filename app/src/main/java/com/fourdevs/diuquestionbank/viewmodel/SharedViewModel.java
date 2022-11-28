package com.fourdevs.diuquestionbank.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.fourdevs.diuquestionbank.models.User;
import com.fourdevs.diuquestionbank.repository.SharedRepository;
import java.util.List;

public class SharedViewModel extends AndroidViewModel {
    private final SharedRepository sharedRepository;

    public SharedViewModel(Application application) {
        super(application);
        sharedRepository = new SharedRepository(application);
    }

    public void getOnlineUserData(String userId) {
        sharedRepository.getOnlineUserData(userId);
    }

    public LiveData<User> getUserData(String userId) {
        return sharedRepository.getUserData(userId);
    }
}
