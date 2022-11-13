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
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fourdevs.diuquestionbank.adapter.UpdateAdapter;
import com.fourdevs.diuquestionbank.databinding.ActivityProfileBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProfileActivity extends BaseActivity implements CourseListener {
    private ActivityProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String uploadCount, approveCount, rejectCount;
    private Integer pendingCount;
    private FirebaseUser mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        setListeners();
        setData();
        getCount();
        getUploadCourses();

    }

    private void setData() {
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.titleProfile.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
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

        binding.buttonChangePass.setOnClickListener(view -> checkPassword());
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
    private void getCount() {
        countProgress(true);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        uploadCount = documentSnapshot.getString(Constants.KEY_UPLOAD_COUNT);
                        approveCount = documentSnapshot.getString(Constants.KEY_APPROVE_COUNT);
                        rejectCount = documentSnapshot.getString(Constants.KEY_REJECT_COUNT);
                    }
                    countProgress(false);
                    pendingCount = Integer.parseInt(uploadCount) - Integer.parseInt(rejectCount) - Integer.parseInt(approveCount);
                    binding.uploadCount.setText(uploadCount);
                    binding.approvedCount.setText(approveCount);
                    binding.rejectedCount.setText(pendingCount.toString());
                });
    }

    private void getUploadCourses() {
        binding.uploadRecyclerProgress.setVisibility(View.VISIBLE);
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {

                    List<Course> courses = new ArrayList<>();

                    for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        Course course = new Course();
                        course.courseId = queryDocumentSnapshot.getId();
                        course.departmentName = queryDocumentSnapshot.getString(Constants.KEY_DEPARTMENT);
                        course.courseName = queryDocumentSnapshot.getString(Constants.KEY_COURSE_CODE);
                        course.semester = queryDocumentSnapshot.getString(Constants.KEY_SEMESTER);
                        course.year = queryDocumentSnapshot.getString(Constants.KEY_YEAR);
                        course.fileUrl = queryDocumentSnapshot.getString(Constants.KEY_PDF_URL);
                        course.exam = queryDocumentSnapshot.getString(Constants.KEY_EXAM);
                        course.userId = queryDocumentSnapshot.getString(Constants.KEY_USER_ID);
                        course.approved = queryDocumentSnapshot.getBoolean(Constants.KEY_IS_APPROVED);
                        course.dateTime =  getReadableDateTime(queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP));
                        course.dateObject = queryDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP);
                        courses.add(course);
                    }
                    courses.sort(Comparator.comparing(obj -> obj.dateObject));
                    Collections.reverse(courses);

                    if (courses.size() > 0) {
                        UpdateAdapter updateAdapter = new UpdateAdapter(courses, this);
                        binding.uploadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                        binding.uploadRecyclerView.setAdapter(updateAdapter);
                        binding.uploadRecyclerView.setVisibility(View.VISIBLE);
                        binding.uploadRecyclerProgress.setVisibility(View.GONE);
                    } else {
                        binding.notAvailable.setVisibility(View.VISIBLE);
                    }
                });
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy- hh:mm a", Locale.getDefault()).format(date);
    }

    private void checkPassword() {
        isLoading(true);
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        String currentPass = binding.currentPassword.getText().toString().trim();
        String newPass = binding.newPassword.getText().toString().trim();

        if(currentPass.isEmpty() && newPass.isEmpty()) {
            makeToast("All fields are required.");
            return;
        } else if (newPass.length() < 6){
            makeToast("Password length should be between 6-20 characters");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(preferenceManager.getString(Constants.KEY_EMAIL),currentPass);

        mAuth.reauthenticate(credential).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                changePassword(newPass);
            } else {
                makeToast("Incorrect password.");
                isLoading(false);
            }
        });
    }

    private void changePassword(String newPass) {
        mAuth.updatePassword(newPass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                makeToast("Password successfully changed.");
                clearAll();
            } else {
                makeToast("Cannot change password...");
            }
            isLoading(false);
        });
    }

    private void isLoading(Boolean load) {
        if(load) {
            binding.buttonChangePass.setVisibility(View.INVISIBLE);
            binding.progress.setVisibility(View.VISIBLE);
        } else {
            binding.buttonChangePass.setVisibility(View.VISIBLE);
            binding.progress.setVisibility(View.GONE);
        }
    }

    private void clearAll() {
        binding.currentPassword.setText("");
        binding.newPassword.setText("");
    }

    private void countProgress(Boolean load) {
        if(load) {
            binding.uploadProgress.setVisibility(View.VISIBLE);
            binding.approveProgress.setVisibility(View.VISIBLE);
            binding.pendingProgress.setVisibility(View.VISIBLE);
        } else {
            binding.uploadProgress.setVisibility(View.GONE);
            binding.approveProgress.setVisibility(View.GONE);
            binding.pendingProgress.setVisibility(View.GONE);
        }
    }


    private Boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void makeToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCourseClicked(Course course) {

    }
}