package com.fourdevs.diuquestionbank;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fourdevs.diuquestionbank.adapter.NotificationAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityNoticeBinding;
import com.fourdevs.diuquestionbank.listeners.NotificationListener;
import com.fourdevs.diuquestionbank.models.Notifications;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeActivity extends BaseActivity implements NotificationListener {
    private ActivityNoticeBinding binding;
    private FirebaseFirestore database;
    private List<Notifications> notifications;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
        findNotification();
    }

    private void init() {
        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(
                notifications,
                this
        );
        binding.notificationRecyclerView.setAdapter(notificationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void findNotification() {
        database.collection(Constants.KEY_COLLECTION_NOTIFICATIONS)
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error !=null){
            return;
        }
        if (value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    Notifications notification = new Notifications();
                    notification.title = documentChange.getDocument().getString(Constants.KEY_SUBJECT);
                    notification.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    notification.link= documentChange.getDocument().getString(Constants.KEY_LINK);
                    notification.dateObject= documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    notification.dateTime= getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    notifications.add(notification);
                }
            }

            notifications.sort(Comparator.comparing(obj -> obj.dateObject));

            if (notifications.size() == 0) {
                notificationAdapter.notifyDataSetChanged();
                binding.notificationEmpty.setVisibility(View.VISIBLE);
            } else {
                notificationAdapter.notifyItemRangeInserted(notifications.size(), notifications.size());
                binding.notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,true));
                binding.notificationRecyclerView.smoothScrollToPosition(notifications.size()-1);
                binding.notificationEmpty.setVisibility(View.GONE);
            }
            binding.notificationRecyclerView.setVisibility(View.VISIBLE);
            binding.notificationProgressBar.setVisibility(View.GONE);
        }
    };


    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM hh:mm a", Locale.getDefault()).format(date);
    }


    @Override
    public void onNotificationClicked(Notifications notification) {
        Intent intent = new Intent(getApplicationContext(), NoticeViewActivity.class);
        intent.putExtra(Constants.KEY_SUBJECT, notification.title);
        intent.putExtra(Constants.KEY_MESSAGE, notification.message);
        intent.putExtra(Constants.KEY_TIMESTAMP, notification.dateTime);
        intent.putExtra(Constants.KEY_LINK, notification.link);
        startActivity(intent);
    }
}