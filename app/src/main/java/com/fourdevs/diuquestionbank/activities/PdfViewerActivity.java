package com.fourdevs.diuquestionbank.activities;

import android.content.Intent;
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
    private String courseLink, uploadDate, uploaderId;
    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private DisplayMetrics metrics;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        loading(true);
        Intent intent = getIntent();
        String courseName = intent.getStringExtra(Constants.KEY_NAME);
        courseLink = intent.getStringExtra(Constants.KEY_PDF_URL);
        uploadDate = intent.getStringExtra(Constants.KEY_UPLOAD_DATE);
        uploaderId = intent.getStringExtra(Constants.KEY_USER_ID);

        binding.textCourseName.setText(courseName);
        downloadActivity();
        setListener();
    }



    private void setListener() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
        binding.imageInfo.setOnClickListener(view-> uploaderInfo());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void downloadActivity(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference islandRef = storageRef.child(courseLink);
        File rootPath = new File(getBaseContext().getCacheDir().getPath()+"/Download");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }
        final File localFile = new File(rootPath,courseLink);
        if(localFile.exists()){
            loading(false);
            displayPdf(localFile);
        } else {
            islandRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                loading(false);
                displayPdf(localFile);
            }).addOnFailureListener(exception ->{
                Log.d("file not created", exception.getMessage());
                makeToast("Cannot open this file");
            } );
        }
    }


    private void displayPdf(File file) {
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            makeToast(e.getMessage());
        }

        try {
            pdfRenderer = new PdfRenderer(fileDescriptor);

        } catch (IOException e) {
            makeToast(e.getMessage());
        }

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
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    private void makeToast(String value) {
        Toast.makeText(getApplicationContext(),value,Toast.LENGTH_SHORT).show();
    }

    private void uploaderInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialougeUploaderInfoBinding viewBinding = DialougeUploaderInfoBinding.inflate(getLayoutInflater());
        builder.setView(viewBinding.getRoot());
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        builder.setNegativeButton("Ok", (dialog, which) -> alert.dismiss()).show();

        sharedViewModel.getOnlineUserData(uploaderId);

        sharedViewModel.getUserData(uploaderId).observe(this, it->{
            try{
                viewBinding.uploaderName.setText(it.userName);
                viewBinding.uploadDate.setText(uploadDate);
            } catch (Exception e) {
                sharedViewModel.getOnlineUserData(uploaderId);
            }
        });


    }

}