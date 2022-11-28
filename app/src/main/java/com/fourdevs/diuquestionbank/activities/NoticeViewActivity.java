package com.fourdevs.diuquestionbank.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.fourdevs.diuquestionbank.databinding.ActivityNoticeViewBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;

public class NoticeViewActivity extends AppCompatActivity {
    private ActivityNoticeViewBinding binding;
    private String title, message, dateTime, link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoticeViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getIntentExtra();
        setData();
        setListeners();
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });
        binding.buttonOk.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });
        binding.link.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(link))));
    }

    private void getIntentExtra() {
        Intent intent = getIntent();
        title = intent.getStringExtra(Constants.KEY_SUBJECT);
        message = intent.getStringExtra(Constants.KEY_MESSAGE);
        dateTime = intent.getStringExtra(Constants.KEY_TIMESTAMP);
        link = intent.getStringExtra(Constants.KEY_LINK);
    }

    private void setData() {
        binding.title.setText(title);
        binding.dateTime.setText(dateTime);
        binding.message.setText(message);
        binding.link.setText(link);
    }
}