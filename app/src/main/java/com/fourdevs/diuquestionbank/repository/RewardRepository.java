package com.fourdevs.diuquestionbank.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;

public class RewardRepository {
    private final Application application;
    private final MutableLiveData<InterstitialAd> _interstitialAd;
    private final MutableLiveData<RewardedAd> _rewardedAdOne;
    private final MutableLiveData<RewardedAd> _rewardedAdTwo;
    private final MutableLiveData<RewardedAd> _rewardedAdThree;
    private final AdRequest adRequest;

    public RewardRepository(Application application) {
        this.application = application;
        _interstitialAd = new MutableLiveData<>();
        _rewardedAdOne = new MutableLiveData<>();
        _rewardedAdTwo = new MutableLiveData<>();
        _rewardedAdThree = new MutableLiveData<>();
        adRequest = new AdRequest.Builder().build();
        loadInterstitialAd();
    }

    private void loadInterstitialAd() {
        InterstitialAd.load(
                application.getApplicationContext(),
                "ca-app-pub-2590640247128409/3439867813",
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        _interstitialAd.setValue(ad);
                    }
                });
    }

    private void loadRewardedAdOne() {

    }

    public LiveData<InterstitialAd> getInterstitialAd() {
        return _interstitialAd;
    }

}
