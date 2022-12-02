package com.fourdevs.diuquestionbank.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fourdevs.diuquestionbank.R;
import com.fourdevs.diuquestionbank.databinding.ItemContainerUploadBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import java.util.List;

public class UpdateAdapter extends ListAdapter<Course,UpdateAdapter.CourseViewHolder> {

    private final CourseListener courseListener;

    public UpdateAdapter(@NonNull DiffUtil.ItemCallback<Course> diffCallback,
                         CourseListener courseListener) {
        super(diffCallback);
        this.courseListener = courseListener;
    }

    @NonNull
    @Override
    public UpdateAdapter.CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUploadBinding itemContainerUploadBinding = ItemContainerUploadBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CourseViewHolder(itemContainerUploadBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateAdapter.CourseViewHolder holder, int position) {
        Course current = getItem(position);
        holder.setCourseData(current.getCourse());
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUploadBinding binding;

        CourseViewHolder(ItemContainerUploadBinding itemContainerUploadBinding) {
            super(itemContainerUploadBinding.getRoot());
            binding = itemContainerUploadBinding;
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        void setCourseData(Course course){
            binding.courseCode.setText(course.courseName);
            binding.department.setText(course.departmentName);
            binding.semesterName.setText(course.semester +" "+course.year);
            if(course.approved != null) {
                if(course.approved) {
                    binding.statusApproved.setVisibility(View.VISIBLE);
                } else {
                    binding.statusPending.setVisibility(View.VISIBLE);
                }
            } else {
                binding.statusRejected.setVisibility(View.VISIBLE);
            }

        }
    }

}
