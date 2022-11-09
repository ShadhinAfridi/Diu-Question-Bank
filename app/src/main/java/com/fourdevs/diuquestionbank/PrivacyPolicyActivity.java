package com.fourdevs.diuquestionbank;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.fourdevs.diuquestionbank.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.fourdevs.diuquestionbank.databinding.ActivityPrivacyPolicyBinding binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.web.loadUrl("https://diuquestionbank.xyz/privacy-policy/");
        binding.web.getSettings().setJavaScriptEnabled(true);
        binding.web.setWebViewClient(new WebViewClient());

        binding.iconBack.setOnClickListener(view -> onBackPressed());
    }
}