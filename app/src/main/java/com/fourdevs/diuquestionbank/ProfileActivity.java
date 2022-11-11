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
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private String uploadCount, approveCount, rejectCount;
    private Integer pendingCount;

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

        getCount();

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
    private void getCount() {
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
                    pendingCount = Integer.parseInt(uploadCount) - Integer.parseInt(rejectCount) - Integer.parseInt(approveCount);
                    binding.uploadCount.setText(uploadCount);
                    binding.approvedCount.setText(approveCount);
                    binding.rejectedCount.setText(pendingCount.toString());
                });
    }


    private Boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void makeToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

}