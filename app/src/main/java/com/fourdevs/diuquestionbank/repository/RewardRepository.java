package com.fourdevs.diuquestionbank.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class RewardRepository {
    private final Application application;
    private final MutableLiveData<InterstitialAd> _interstitialAd;

    public RewardRepository(Application application) {
        this.application = application;
        _interstitialAd = new MutableLiveData<>();
        loadInterstitialAd();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
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

    public LiveData<InterstitialAd> getInterstitialAd() {
        return _interstitialAd;
    }

}
