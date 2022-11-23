package com.fourdevs.diuquestionbank.viewmodel;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthViewModel extends ViewModel {
    private final MutableLiveData<String> _time = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _finished = new MutableLiveData<>();

    public LiveData<String> time() {
        return _time;
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
}
