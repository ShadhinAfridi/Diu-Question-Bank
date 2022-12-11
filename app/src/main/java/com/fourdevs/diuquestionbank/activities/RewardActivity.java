package com.fourdevs.diuquestionbank.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.databinding.ActivityRewardBinding;
import com.fourdevs.diuquestionbank.viewmodel.RewardViewModel;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;


@SuppressLint("SetTextI18n")
public class RewardActivity extends BaseActivity {
    private ActivityRewardBinding binding;
    private InterstitialAd interstitialAd;
    private RewardViewModel rewardViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRewardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        rewardViewModel = new ViewModelProvider(this).get(RewardViewModel.class);
        setAds();
        setListeners();
        MobileAds.initialize(this, initializationStatus -> loadInterstitialAd());
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });

        binding.rewardOne.setOnClickListener(view -> toastNotAvailable());
        binding.rewardTwo.setOnClickListener(view -> toastNotAvailable());
        binding.rewardThree.setOnClickListener(view -> toastNotAvailable());
    }

    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
    }

    private void loadInterstitialAd() {
        rewardViewModel.getInterstitialAd().observe(this, it->{
            if(it != null) {
                it.show(RewardActivity.this);
            }
        });
    }


    private void toastNotAvailable() {
        Toast.makeText(this, "Not available!", Toast.LENGTH_SHORT).show();
    }



}


