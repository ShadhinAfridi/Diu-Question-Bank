package com.fourdevs.diuquestionbank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fourdevs.diuquestionbank.databinding.ItemContainerDepartmentBinding;
import com.fourdevs.diuquestionbank.listeners.DepartmentListener;
import com.fourdevs.diuquestionbank.models.Course;

public class DepartmentAdapter extends ListAdapter<Course, DepartmentAdapter.DepartmentViewHolder> {

    private final DepartmentListener departmentListener;

    public DepartmentAdapter(@NonNull DiffUtil.ItemCallback<Course> diffCallback,
                             DepartmentListener departmentListener)
    {
        super(diffCallback);
        this.departmentListener = departmentListener;
    }

    @NonNull
    @Override
    public DepartmentAdapter.DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerDepartmentBinding itemContainerDepartmentBinding = ItemContainerDepartmentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DepartmentViewHolder(itemContainerDepartmentBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentAdapter.DepartmentViewHolder holder, int position) {
        Course current = getItem(position);
        holder.setDepartmentData(current.getCourse());
    }



    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        ItemContainerDepartmentBinding binding;

        DepartmentViewHolder(ItemContainerDepartmentBinding itemContainerDepartmentBinding) {
            super(itemContainerDepartmentBinding.getRoot());
            binding = itemContainerDepartmentBinding;
        }

        void setDepartmentData(Course course){
            binding.textDepartment.setText(course.departmentName);
            binding.getRoot().setOnClickListener(view -> departmentListener.onDepartmentClicked(course.departmentName));
        }
    }
}