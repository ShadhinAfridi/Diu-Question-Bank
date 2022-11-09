package com.fourdevs.diuquestionbank;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.fourdevs.diuquestionbank.databinding.ActivityProfileBinding;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileActivity extends BaseActivity {
    private ActivityProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private int totalUpload, totalApproved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setListeners();
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.titleProfile.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));

        if(!preferenceManager.getBoolean(Constants.KEY_COUNT_ONCE)){
            getApprovedCount();
            getUploadCount();
            getRejectedData();
        } else {
            binding.uploadCount.setText(preferenceManager.getString(Constants.KEY_COUNT_UPLOAD));
            binding.approvedCount.setText(preferenceManager.getString(Constants.KEY_COUNT_APPROVED));
            binding.rejectedCount.setText(preferenceManager.getString(Constants.KEY_COUNT_REJECTED));
        }
        if(preferenceManager.getString(Constants.KEY_PROFILE_PICTURE) != null) {
            binding.profileImage.setImageBitmap(getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_PROFILE_PICTURE)));
        }
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> {
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainIntent);
            finish();
        });

        binding.editImage.setOnClickListener(view -> {
            if (checkPermissions()) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            } else {
                makeToast("permission denied");
            }

        });
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() !=null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            String encodedImage = encodeImage(bitmap);
                            updateProfilePicture(encodedImage);
                            preferenceManager.putSting(Constants.KEY_PROFILE_PICTURE, encodedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 200;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap , previewWidth, previewHeight, false);
        binding.profileImage.setImageBitmap(previewBitmap);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private void updateProfilePicture(String encodedImage){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID)
                );

        documentReference.update(Constants.KEY_PROFILE_PICTURE, encodedImage)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        makeToast("successfully changed!");
                    } else {
                        makeToast(Objects.requireNonNull(task.getException()).getMessage());
                        preferenceManager.putBoolean(Constants.KEY_READ_ONCE, false);
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void getApprovedCount() {
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_IS_APPROVED, true)
                .get()
                .addOnCompleteListener(task -> {
                    List<Course> courses = new ArrayList<>();
                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Course course = new Course();
                        course.departmentName = queryDocumentSnapshot.getString(Constants.KEY_DEPARTMENT);
                        course.courseName = queryDocumentSnapshot.getString(Constants.KEY_COURSE_CODE);
                        course.semester = queryDocumentSnapshot.getString(Constants.KEY_SEMESTER);
                        course.year = queryDocumentSnapshot.getString(Constants.KEY_YEAR);
                        course.fileUrl = queryDocumentSnapshot.getString(Constants.KEY_PDF_URL);
                        course.exam = queryDocumentSnapshot.getString(Constants.KEY_EXAM);
                        courses.add(course);
                    }
                    totalApproved = courses.size();
                    preferenceManager.putSting(Constants.KEY_COUNT_APPROVED, courses.size()+"");
                    binding.approvedCount.setText(courses.size()+"");
                });
    }

    @SuppressLint("SetTextI18n")
    private void getUploadCount() {
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    List<Course> courseTotal = new ArrayList<>();
                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Course course = new Course();
                        course.departmentName = queryDocumentSnapshot.getString(Constants.KEY_DEPARTMENT);
                        course.courseName = queryDocumentSnapshot.getString(Constants.KEY_COURSE_CODE);
                        course.semester = queryDocumentSnapshot.getString(Constants.KEY_SEMESTER);
                        course.year = queryDocumentSnapshot.getString(Constants.KEY_YEAR);
                        course.fileUrl = queryDocumentSnapshot.getString(Constants.KEY_PDF_URL);
                        course.exam = queryDocumentSnapshot.getString(Constants.KEY_EXAM);
                        courseTotal.add(course);
                    }
                    totalUpload = courseTotal.size();
                    preferenceManager.putSting(Constants.KEY_COUNT_UPLOAD, courseTotal.size()+"");
                    binding.uploadCount.setText(courseTotal.size()+"");

                });

    }

    @SuppressLint("SetTextI18n")
    private void getRejectedData() {
        int totalRejected = totalUpload-totalApproved;
        preferenceManager.putSting(Constants.KEY_COUNT_REJECTED, totalRejected+"");
        preferenceManager.putBoolean(Constants.KEY_COUNT_ONCE, true);
        binding.rejectedCount.setText(totalRejected+"");
    }



    private Boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void makeToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

}