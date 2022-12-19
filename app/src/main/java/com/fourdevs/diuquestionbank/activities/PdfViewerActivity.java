package com.fourdevs.diuquestionbank.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fourdevs.diuquestionbank.adapter.PdfAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityPdfViewerBinding;
import com.fourdevs.diuquestionbank.databinding.DialougeUploaderInfoBinding;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.viewmodel.SharedViewModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfViewerActivity extends BaseActivity {
    private ActivityPdfViewerBinding binding;
    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private DisplayMetrics metrics;
    private SharedViewModel sharedViewModel;
    private Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        loading(true);
        binding.textCourseName.setText(getIntentExtras());
        downloadActivity();
        setListener();
    }

    private String getIntentExtras() {
        course = (Course) getIntent().getSerializableExtra(Constants.KEY_NAME);
        return course.courseName+"_"+course.exam+"_"+course.semester+"_"+course.year;
    }



    private void setListener() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
        binding.imageInfo.setOnClickListener(view-> uploaderInfo());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void downloadActivity(){
        try{
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.create();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference islandRef = storageRef.child(course.fileUrl);
            File rootPath = new File(getBaseContext().getCacheDir().getPath()+"/Download");
            if(!rootPath.exists()) {
                rootPath.mkdirs();
            }
            final File localFile = new File(rootPath,course.fileUrl);
            if(localFile.exists()){
                loading(false);
                displayPdf(localFile);
            } else {
                islandRef.getFile(localFile).addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Loading... " + ((int) progress) + "%");
                    progressDialog.show();
                }).addOnSuccessListener(taskSnapshot -> {
                    loading(false);
                    displayPdf(localFile);
                    progressDialog.dismiss();
                }).addOnFailureListener(exception ->{
                    exception.getStackTrace();
                    Toast.makeText(getApplicationContext(),"Cannot open this file",Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } );
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
    }


    private void displayPdf(File file) {
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            pdfRenderer = new PdfRenderer(fileDescriptor);

        } catch (IOException e) {
            e.printStackTrace();
        }

        try{

            int numberOfPage = pdfRenderer.getPageCount();
            List<Bitmap> list = new ArrayList<>();

            for(int i=0; i<numberOfPage; i++){
                PdfRenderer.Page rendererPage = pdfRenderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(
                        metrics.widthPixels,
                        metrics.heightPixels,
                        Bitmap.Config.ARGB_8888);
                rendererPage.render(bitmap, null, null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                list.add(bitmap);
                rendererPage.close();
            }
            pdfRenderer.close();
            if(list.size() > 0){
                PdfAdapter pdfAdapter = new PdfAdapter(list);
                binding.pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                binding.pdfRecyclerView.setAdapter(pdfAdapter);
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.GONE);
        }
    }


    private void uploaderInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialougeUploaderInfoBinding viewBinding = DialougeUploaderInfoBinding.inflate(getLayoutInflater());
        builder.setView(viewBinding.getRoot());
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        builder.setNegativeButton("Ok", (dialog, which) -> alert.dismiss()).show();


        sharedViewModel.getUserData(course.userId).observe(this, it->{
            try{
                viewBinding.uploaderName.setText(it.userName);
                viewBinding.uploadDate.setText(course.dateTime);
            } catch (Exception e) {
                sharedViewModel.getOnlineUserData(course.userId);
            }
        });


    }

}