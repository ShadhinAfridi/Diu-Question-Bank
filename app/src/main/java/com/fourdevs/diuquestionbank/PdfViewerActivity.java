package com.fourdevs.diuquestionbank;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fourdevs.diuquestionbank.adapter.PdfAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityPdfViewerBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfViewerActivity extends BaseActivity {

    private ActivityPdfViewerBinding binding;
    private String courseLink;
    private ParcelFileDescriptor fileDescriptor;
    private PdfRenderer pdfRenderer;
    private DisplayMetrics metrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        metrics = getApplicationContext().getResources().getDisplayMetrics();
        loading(true);
        Intent intent = getIntent();
        String courseName = intent.getStringExtra(Constants.KEY_NAME);
        courseLink = intent.getStringExtra(Constants.KEY_PDF_URL);
        binding.textCourseName.setText(courseName);
        downloadActivity();
        setListener();
    }



    private void setListener() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
    }

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
            //int rendererPageWidth = rendererPage.getWidth()*2;
            //int rendererPageHeight = rendererPage.getHeight()*2;
            Bitmap bitmap = Bitmap.createBitmap(
                    metrics.widthPixels,
                    metrics.heightPixels,
                    Bitmap.Config.ARGB_8888);
            rendererPage.render(bitmap, null, null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            list.add(bitmap);
            rendererPage.close();
        }
        if(list.size() > 0){
            PdfAdapter pdfAdapter = new PdfAdapter(list);
            binding.pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.pdfRecyclerView.setAdapter(pdfAdapter);
        }
        pdfRenderer.close();
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

}