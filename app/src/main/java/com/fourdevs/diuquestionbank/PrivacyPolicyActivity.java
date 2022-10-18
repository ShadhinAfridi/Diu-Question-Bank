package com.fourdevs.diuquestionbank;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fourdevs.diuquestionbank.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private ActivityPrivacyPolicyBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.web.loadUrl("https://www.privacypolicygenerator.info/live.php?token=ABpvrhrx6HpPRaeVLDdCtP9usRF32jkj");
        binding.web.getSettings().setJavaScriptEnabled(true);
        binding.web.setWebViewClient(new WebViewClient());

        binding.iconBack.setOnClickListener(view -> onBackPressed());
    }
}