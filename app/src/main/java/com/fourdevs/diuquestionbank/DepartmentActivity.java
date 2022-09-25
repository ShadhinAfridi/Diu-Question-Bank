package com.fourdevs.diuquestionbank;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.fourdevs.diuquestionbank.adapter.DepartmentAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityDepartmentBinding;
import com.fourdevs.diuquestionbank.listeners.DepartmentListener;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DepartmentActivity extends BaseActivity implements DepartmentListener{
    private ActivityDepartmentBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepartmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.departmentProgressBar.setVisibility(View.VISIBLE);
        getDepartments();
        setListeners();
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
    }

    private void getDepartments() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .get()
                .addOnCompleteListener(task -> {
                    List <String> departments = new ArrayList<>();

                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        String departmentName;
                        departmentName = queryDocumentSnapshot.getString(Constants.KEY_DEPARTMENT);
                        departments.add(departmentName);
                    }
                    Set<String> setDepartments = new HashSet<>(departments);
                    departments.clear();
                    departments.addAll(setDepartments);
                    Collections.sort(departments);

                    if(departments.size() > 0) {
                        DepartmentAdapter departmentAdapter = new DepartmentAdapter(departments, this);
                        binding.departmentRecyclerView.setAdapter(departmentAdapter);
                        binding.departmentRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        binding.linearLayout.setVisibility(View.VISIBLE);
                    }
                    binding.departmentProgressBar.setVisibility(View.GONE);
                });
    }


    @Override
    public void onDepartmentClicked(String department) {
        Intent intent = new Intent(getApplicationContext(), CoursesActivity.class);
        intent.putExtra("departmentName",department);
        startActivity(intent);
    }
}