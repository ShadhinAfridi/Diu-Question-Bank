package com.fourdevs.diuquestionbank.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fourdevs.diuquestionbank.repository.RewardRepository;
import com.google.android.gms.ads.interstitial.InterstitialAd;

public class RewardViewModel extends AndroidViewModel {
    private final RewardRepository rewardRepository;

    public RewardViewModel(@NonNull Application application) {
        super(application);
        rewardRepository = new RewardRepository(application);
    }

    public LiveData<InterstitialAd> getInterstitialAd() {
        return rewardRepository.getInterstitialAd();
    }


}
