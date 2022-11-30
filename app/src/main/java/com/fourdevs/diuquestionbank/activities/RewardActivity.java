package com.fourdevs.diuquestionbank.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.fourdevs.diuquestionbank.databinding.ActivityRewardBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/** Main Activity. Inflates main activity xml. */
@SuppressLint("SetTextI18n")
public class RewardActivity extends Activity {
    private static final String AD_UNIT_ID = "ca-app-pub-2590640247128409/7451059816";
    private static final String TAG = "RewardActivity";
    private static final long COUNTER_TIME = 10;
    private static final int GAME_OVER_REWARD = 1;

    private int coinCount;
    private CountDownTimer countDownTimer;
    private boolean gameOver;
    private boolean gamePaused;

    private RewardedAd rewardedAd;
    private long timeRemaining;
    boolean isLoading;

    private ActivityRewardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRewardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setAds();

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());

        MobileAds.initialize(this, initializationStatus -> {
        });

        loadRewardedAd();

        // Create the "retry" button, which tries to show a rewarded ad between game plays.
        binding.retryButton.setVisibility(View.INVISIBLE);
        binding.retryButton.setOnClickListener(view -> startGame());

        // Create the "show" button, which shows a rewarded video if one is loaded.
        binding.showVideoButton.setOnClickListener( view -> showRewardedVideo());

        // Display current coin count to user.
        coinCount = 0;
        binding.coinCountText.setText("Coins: " + coinCount);

        startGame();
        setListeners();

    }

    @Override
    public void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!gameOver && gamePaused) {
            resumeGame();
        }
    }

    private void pauseGame() {
        countDownTimer.cancel();
        gamePaused = true;
    }

    private void resumeGame() {
        createTimer(timeRemaining);
        gamePaused = false;
    }

    private void loadRewardedAd() {
        if (rewardedAd == null) {
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    AD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            rewardedAd = null;
                            RewardActivity.this.isLoading = false;
                            Toast.makeText(RewardActivity.this, "onAdFailedToLoad", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            RewardActivity.this.rewardedAd = rewardedAd;
                            Log.d(TAG, "onAdLoaded");
                            RewardActivity.this.isLoading = false;
                            Toast.makeText(RewardActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void addCoins(int coins) {
        coinCount += coins;
        binding.coinCountText.setText("Coins: " + coinCount);
    }

    private void startGame() {
        // Hide the retry button, load the ad, and start the timer.
        binding.retryButton.setVisibility(View.INVISIBLE);
        if (rewardedAd != null && !isLoading) {
            loadRewardedAd();
        }
        createTimer(COUNTER_TIME);
        gamePaused = false;
        gameOver = false;
    }

    // Create the game timer, which counts down to the end of the level
    // and shows the "retry" button.
    private void createTimer(long time) {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer =
                new CountDownTimer(time * 1000, 50) {
                    @Override
                    public void onTick(long millisUnitFinished) {
                        timeRemaining = ((millisUnitFinished / 1000) + 1);
                        binding.timer.setText("seconds remaining: " + timeRemaining);
                    }

                    @Override
                    public void onFinish() {
                        if (rewardedAd != null) {
                            binding.showVideoButton.setVisibility(View.VISIBLE);
                        }
                        binding.timer.setText("You Lose!");
                        addCoins(GAME_OVER_REWARD);
                        binding.retryButton.setVisibility(View.VISIBLE);
                        gameOver = true;
                    }
                };
        countDownTimer.start();
    }

    private void showRewardedVideo() {

        if (rewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }

        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "onAdShowedFullScreenContent");
                        Toast.makeText(RewardActivity.this, "onAdShowedFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        Toast.makeText(
                                        RewardActivity.this, "onAdFailedToShowFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");
                        Toast.makeText(RewardActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
                                .show();
                        // Preload the next rewarded ad.
                        RewardActivity.this.loadRewardedAd();
                    }
                });
        Activity activityContext = RewardActivity.this;
        rewardedAd.show(
                activityContext,
                rewardItem -> {
                    // Handle the reward.
                    Log.d("TAG", "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();

                    Log.d("Afiridi", rewardType+"="+rewardAmount);
                });
    }































    private void setListeners() {
        binding.showVideoButton.setOnClickListener(view ->{

        });

        binding.iconBack.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });
    }

    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
        loadAd();
    }

    private void loadAd() {

    }





}


