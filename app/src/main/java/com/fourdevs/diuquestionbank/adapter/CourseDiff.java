package com.fourdevs.diuquestionbank.adapter;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.fourdevs.diuquestionbank.models.Course;

public class CourseDiff extends DiffUtil.ItemCallback<Course> {

    @Override
    public boolean areItemsTheSame(@NonNull Course oldItem, @NonNull Course newItem) {
        return oldItem == newItem;
    }

    @SuppressLint("DiffUtilEquals")
    @Override
    public boolean areContentsTheSame(@NonNull Course oldItem, @NonNull Course newItem) {
        return oldItem.getCourse().equals(newItem.getCourse());
    }
}
