package com.fourdevs.diuquestionbank.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fourdevs.diuquestionbank.PdfViewerActivity;
import com.fourdevs.diuquestionbank.R;
import com.fourdevs.diuquestionbank.databinding.ItemContainerCoursesBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder>{

    private final List<Course> coursers;
    private final Context context;
    private final CourseListener courseListener;

    public CourseAdapter(List<Course> departments, Context context, CourseListener courseListener) {
        this.coursers = departments;
        this.context = context;
        this.courseListener = courseListener;
    }

    @NonNull
    @Override
    public CourseAdapter.CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerCoursesBinding itemContainerCoursesBinding = ItemContainerCoursesBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CourseViewHolder(itemContainerCoursesBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseAdapter.CourseViewHolder holder, int position) {
        holder.setCourseData(coursers.get(position));
    }

    @Override
    public int getItemCount() {
        return coursers.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder{
        ItemContainerCoursesBinding binding;

        CourseViewHolder(ItemContainerCoursesBinding itemContainerCoursesBinding) {
            super(itemContainerCoursesBinding.getRoot());
            binding = itemContainerCoursesBinding;
        }

        @SuppressLint("SetTextI18n")
        void setCourseData(Course course){
            binding.courseCode.setText(course.courseName);
            binding.semesterName.setText(course.semester+"("+course.year+")");
            binding.examName.setText(course.exam);
            binding.iconDownload.setOnClickListener(view -> {
                makeToast("Download unavailable.");
//                if (checkPermissions()) {
//                    binding.iconDownload.setVisibility(View.INVISIBLE);
//                    binding.progressBar.setVisibility(View.VISIBLE);
//                    downloadActivity(course.fileUrl);
//                    resetColor();
//                } else {
//                    makeToast("Storage permission denied");
//                }
            });
            binding.iconEye.setOnClickListener(view -> {
                goToPdfViewer(course.courseName, course.fileUrl, course.semester, course.year);
                binding.iconEye.setColorFilter(ContextCompat.getColor(context, R.color.primary),
                        PorterDuff.Mode.SRC_ATOP);
                resetColor();
            });

            binding.getRoot().setOnClickListener(view -> courseListener.onCourseClicked(course));
        }

        private void goToPdfViewer(String courseName, String fileUrl, String semester, String year) {
            Intent intent = new Intent(context, PdfViewerActivity.class);
            intent.putExtra(Constants.KEY_NAME, courseName+" "+semester+"("+year+")");
            intent.putExtra(Constants.KEY_PDF_URL, fileUrl);
            context.startActivity(intent);
        }

        public void resetColor(){
            new Handler().postDelayed(() -> {
                binding.iconEye.setColorFilter(ContextCompat.getColor(context, R.color.secondary_text),
                        PorterDuff.Mode.SRC_ATOP);
            }, 1000);
        }

        private void downloadActivity(String courseLink){
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference islandRef = storageRef.child(courseLink);
            File rootPath = new File(Environment.getExternalStorageDirectory(), "Download");
            if(!rootPath.exists()) {
                rootPath.mkdirs();
            }
            final File localFile = new File(rootPath,courseLink);
            if(localFile.exists()){
                makeToast("File Already Exists" + localFile);
            } else {
                islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        makeToast("Download Complete!");
                    }
                }).addOnFailureListener(exception -> makeToast("Download Failed!"));
            }
            binding.iconDownload.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    }

    private Boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void makeToast(String value) {
        Toast.makeText(context,value,Toast.LENGTH_SHORT).show();
    }

}
