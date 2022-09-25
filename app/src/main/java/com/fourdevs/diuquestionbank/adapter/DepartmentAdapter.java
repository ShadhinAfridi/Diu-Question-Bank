package com.fourdevs.diuquestionbank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fourdevs.diuquestionbank.databinding.ItemContainerDepartmentBinding;
import com.fourdevs.diuquestionbank.listeners.DepartmentListener;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {

    private final List<String> departments;
    private final DepartmentListener departmentListener;

    public DepartmentAdapter(List<String> departments, DepartmentListener departmentListener) {
        this.departments = departments;
        this.departmentListener = departmentListener;
    }

    @NonNull
    @Override
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerDepartmentBinding itemContainerDepartmentBinding = ItemContainerDepartmentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DepartmentViewHolder(itemContainerDepartmentBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        holder.setDepartmentData(departments.get(position));
    }

    @Override
    public int getItemCount() {
        return departments.size();
    }


    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        ItemContainerDepartmentBinding binding;

        DepartmentViewHolder(ItemContainerDepartmentBinding itemContainerDepartmentBinding) {
            super(itemContainerDepartmentBinding.getRoot());
            binding = itemContainerDepartmentBinding;
        }

        void setDepartmentData(String department){
            binding.textDepartment.setText(department);
            binding.getRoot().setOnClickListener(view -> departmentListener.onDepartmentClicked(department));
        }
    }
}