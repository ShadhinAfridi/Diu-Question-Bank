package com.fourdevs.diuquestionbank.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fourdevs.diuquestionbank.databinding.ItemContainerNotificationBinding;
import com.fourdevs.diuquestionbank.listeners.NotificationListener;
import com.fourdevs.diuquestionbank.models.Notifications;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{

    private final List<Notifications> notifications;
    private final NotificationListener NotificationListener;

    public NotificationAdapter(List<Notifications> notifications, NotificationListener NotificationListener) {
        this.notifications = notifications;
        this.NotificationListener = NotificationListener;

    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerNotificationBinding itemContainerNotificationBinding = ItemContainerNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NotificationViewHolder(itemContainerNotificationBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        holder.setNotificationData(notifications.get(position));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder{
        ItemContainerNotificationBinding binding;

        NotificationViewHolder(ItemContainerNotificationBinding itemContainerNotificationsBinding) {
            super(itemContainerNotificationsBinding.getRoot());
            binding = itemContainerNotificationsBinding;
        }

        @SuppressLint("SetTextI18n")
        void setNotificationData(Notifications Notification){
            binding.notificationTitle.setText(toLittleString(Notification.title, 22));
            binding.notificationMessage.setText(toLittleString(Notification.message, 48).replace("\n", ""));
            binding.notificationDateTime.setText((CharSequence) Notification.dateTime);
            binding.getRoot().setOnClickListener(view -> NotificationListener.onNotificationClicked(Notification));
        }

    }

    String toLittleString(String str, int length) {
        if (str.length() > length) {
            return str.substring(0, length) + "...";
        }
        return str;
    }

}
