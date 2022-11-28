package com.fourdevs.diuquestionbank.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.fourdevs.diuquestionbank.activities.PdfViewerActivity;
import com.fourdevs.diuquestionbank.R;
import com.fourdevs.diuquestionbank.databinding.ItemContainerCoursesBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;

public class CourseAdapter extends ListAdapter<Course, CourseAdapter.CourseViewHolder> {

    private final Context context;
    private final CourseListener courseListener;

    public CourseAdapter(@NonNull DiffUtil.ItemCallback<Course> diffCallback,
                         Context context,
                         CourseListener courseListener)
    {
        super(diffCallback);
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
        Course current = getItem(position);
        holder.setCourseData(current.getCourse());
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
                makeToast("Download not available.");
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



    }


    private void makeToast(String value) {
        Toast.makeText(context,value,Toast.LENGTH_SHORT).show();
    }

}
