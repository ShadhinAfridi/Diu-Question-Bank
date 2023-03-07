package com.fourdevs.diuquestionbank.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.fourdevs.diuquestionbank.R;
import com.fourdevs.diuquestionbank.databinding.ActivityRewardBinding;
import com.fourdevs.diuquestionbank.viewmodel.RewardViewModel;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Random;


@SuppressLint("SetTextI18n")
public class RewardActivity extends BaseActivity {
    private ActivityRewardBinding binding;
    private RewardViewModel rewardViewModel;
    private InterstitialAd mInterstitialAd;

    private Random random;
    private int answer;
    private int score;
    private int questionCount;
    private Boolean loadQuestion;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRewardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setAds();
        loadInterstitialAd();
        rewardViewModel = new ViewModelProvider(this).get(RewardViewModel.class);
        setListeners();


        random = new Random();
        generateQuestion();

        loadQuestion = true;

        // Set up the submit button click listener
        binding.submitButton.setOnClickListener(v -> {
            if(binding.answerEditText.getText().toString().isEmpty()) {
                binding.answerEditText.setError("Answer the question.");
                binding.answerEditText.requestFocus();
            } else {
                int userAnswer = Integer.parseInt(binding.answerEditText.getText().toString());
                if (userAnswer == answer) {
                    // If the answer is correct, increment the score
                    score++;
                    makeToast("Correct answer!");
                    loadQuestion = true;

                } else {
                    // If the answer is incorrect, display an error message
                    makeToast("Sorry, that's incorrect.");
                    loadQuestion=false;
                }

                // Generate a new question
                questionCount++;
                if (loadQuestion) {
                    generateQuestion();
                } else {
                    // If 10 questions have been asked, show the results dialog
                    showInterstitialAd();
                    showResultsDialog();

                }
            }

        });


    }

    private void generateQuestion() {
        // Generate two random numbers
        int num1 = random.nextInt(20);
        int num2 = random.nextInt(20);

        // Calculate the answer
        answer = num1 + num2;

        // Display the question
        binding.questionTextView.setText(getString(R.string.question_template, num1, num2));
        binding.answerEditText.setText("");
    }

    private void showResultsDialog() {
        // Build the results dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.results_dialog_title);
        builder.setMessage(getString(R.string.results_dialog_message, score));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.results_dialog_button, (dialog, which) -> {
            // Reset the game
            score = 0;
            questionCount = 0;
            generateQuestion();
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });

    }























    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
    }

    private void loadInterstitialAd() {

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-2590640247128409/3439867813", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                Log.d(TAG, "Ad was clicked.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.e(TAG, "Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.");
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad showed fullscreen content.");
                                loadInterstitialAd();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                        loadInterstitialAd();
                    }
                });


    }

    private void showInterstitialAd () {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(RewardActivity.this);
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.");
        }
    }


    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }



}


