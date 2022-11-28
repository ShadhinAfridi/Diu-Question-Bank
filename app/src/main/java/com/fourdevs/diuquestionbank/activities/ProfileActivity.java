package com.fourdevs.diuquestionbank.activities;

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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fourdevs.diuquestionbank.adapter.CourseDiff;
import com.fourdevs.diuquestionbank.adapter.UpdateAdapter;
import com.fourdevs.diuquestionbank.authentication.LoginActivity;
import com.fourdevs.diuquestionbank.databinding.ActivityProfileBinding;
import com.fourdevs.diuquestionbank.databinding.DialogueChangePasswordBinding;
import com.fourdevs.diuquestionbank.listeners.CourseListener;
import com.fourdevs.diuquestionbank.models.Course;
import com.fourdevs.diuquestionbank.utilities.AsyncTasks;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.fourdevs.diuquestionbank.viewmodel.AuthViewModel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;

public class ProfileActivity extends BaseActivity implements CourseListener {
    private ActivityProfileBinding binding;
    private PreferenceManager preferenceManager;
    private int pendingCount = 0, approveCount = 0, rejectCount = 0;
    private AuthViewModel authViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        setData();
        getUploadCourses();
    }

    private void setData() {
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.titleProfile.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        if(preferenceManager.getString(Constants.KEY_PROFILE_PICTURE) != null) {
            binding.profileImage.setImageBitmap(getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_PROFILE_PICTURE)));
        }
        new AsyncTasks() {
            @Override
            public void doInBackground() {
                authViewModel.networkCourse(preferenceManager.getString(Constants.KEY_USER_ID));
            }

            @Override
            public void onPostExecute() {

            }
        }.execute();

    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> {
            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainIntent);
            finish();
        });

        binding.iconLogOut.setOnClickListener(view-> authViewModel.deleteFcmToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                authViewModel.logOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else {
                makeToast("Something went wrong!");
            }
        }));

        binding.iconSetting.setOnClickListener(view-> {
            if(binding.settingOptions.getVisibility() == View.VISIBLE) {
                binding.settingOptions.setVisibility(View.GONE);
            } else {
                binding.settingOptions.setVisibility(View.VISIBLE);
            }
        });

        binding.changePasswordBar.setOnClickListener(view-> showChangePasswordDialog());

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
        authViewModel.changeProfilePicture(encodedImage).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        makeToast("successfully changed!");
                    } else {
                        makeToast(Objects.requireNonNull(task.getException()).getMessage());
                        preferenceManager.putBoolean(Constants.KEY_READ_ONCE, false);
                    }
                });
    }

    private void getUploadCourses() {
        binding.uploadRecyclerProgress.setVisibility(View.VISIBLE);
        UpdateAdapter updateAdapter = new UpdateAdapter(new CourseDiff(),this);
        binding.uploadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.uploadRecyclerView.setAdapter(updateAdapter);
        binding.uploadRecyclerView.setVisibility(View.VISIBLE);

        authViewModel.getUserUploads(preferenceManager.getString(Constants.KEY_USER_ID)).observe(this, it->{
            updateAdapter.submitList(it);
            if(it.size()==0) {
                binding.notAvailable.setVisibility(View.VISIBLE);
            }
            binding.uploadRecyclerProgress.setVisibility(View.GONE);

            for(int i=0; i<it.size(); i++) {
                Course course = it.get(i);
                binding.uploadCount.setText(String.valueOf(it.size()));
                if(course.approved!=null) {
                    if(course.approved) {
                        approveCount+=1;
                    } else {
                        pendingCount+=1;
                    }
                } else {
                    rejectCount+=1;
                }
                binding.approvedCount.setText(String.valueOf(approveCount));
                binding.pendingCount.setText(String.valueOf(pendingCount));
                binding.rejectedCount.setText(String.valueOf(rejectCount));
            }
        });
    }



    @SuppressLint("InflateParams")
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogueChangePasswordBinding viewBinding = DialogueChangePasswordBinding.inflate(getLayoutInflater());
        builder.setView(viewBinding.getRoot());
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        viewBinding.buttonChangePass.setOnClickListener(view -> {
            String currentPass = viewBinding.currentPassword.getText().toString().trim();
            String newPass = viewBinding.newPassword.getText().toString().trim();

            if(currentPass.isEmpty() && newPass.isEmpty()) {
                makeToast("All fields are required.");
                return;
            } else if (newPass.length() < 6){
                makeToast("Password length should be between 6-20 characters");
                return;
            }
            viewBinding.buttonChangePass.setVisibility(View.INVISIBLE);
            viewBinding.progress.setVisibility(View.VISIBLE);
            authViewModel.checkCurrentPassword(currentPass).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    authViewModel.changePassword(newPass).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            makeToast("Password successfully changed.");
                            viewBinding.currentPassword.setText("");
                            viewBinding.newPassword.setText("");
                            alert.dismiss();
                        } else {
                            makeToast("Cannot change password...");
                        }
                    });
                } else {
                    makeToast("Incorrect password.");
                }

            });

            viewBinding.buttonChangePass.setVisibility(View.VISIBLE);
            viewBinding.progress.setVisibility(View.GONE);
        });

        viewBinding.cancel.setOnClickListener(view-> alert.dismiss());


        alert.show();

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