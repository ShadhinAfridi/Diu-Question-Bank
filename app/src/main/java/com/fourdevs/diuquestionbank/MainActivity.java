package com.fourdevs.diuquestionbank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.fourdevs.diuquestionbank.authentication.LoginActivity;
import com.fourdevs.diuquestionbank.databinding.ActivityMainBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;


public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private static final int PERMISSION_REQUEST_CODE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        if(!preferenceManager.getBoolean(Constants.KEY_READ_ONCE)) {
            getUserData();
        }
        if(!checkPermissions()) {
            askPermission();
        }
        getToken();
        setUserData();
        setListeners();
    }

    private void setUserData() {
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        if(preferenceManager.getString(Constants.KEY_PROFILE_PICTURE) != null) {
            binding.profileImage.setImageBitmap(getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_PROFILE_PICTURE)));
        }
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void getUserData() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putSting(Constants.KEY_USER_ID, documentSnapshot.getString(Constants.KEY_USER_ID));
                        preferenceManager.putSting(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putSting(Constants.KEY_PROFILE_PICTURE, documentSnapshot.getString(Constants.KEY_PROFILE_PICTURE));
                        preferenceManager.putSting(Constants.KEY_COUNT_APPROVED, documentSnapshot.getString(Constants.KEY_COUNT_APPROVED));
                        preferenceManager.putSting(Constants.KEY_COUNT_REJECTED, documentSnapshot.getString(Constants.KEY_COUNT_REJECTED));
                        preferenceManager.putSting(Constants.KEY_COUNT_UPLOAD, documentSnapshot.getString(Constants.KEY_COUNT_UPLOAD));
                        preferenceManager.putBoolean(Constants.KEY_READ_ONCE, true);
                        setUserData();
                    }
                });
    }



    private void setListeners() {
        binding.cardQuestion.setOnClickListener(view -> {
            Intent questionIntent = new Intent(MainActivity.this, DepartmentActivity.class);
            startActivity(questionIntent);
        });
        binding.cardUpload.setOnClickListener(view -> {
            Intent uploadIntent = new Intent(MainActivity.this, QuestionUploadActivity.class);
            startActivity(uploadIntent);
        });
        binding.iconLogout.setOnClickListener(view -> logOut());

        binding.profileImage.setOnClickListener(view -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        binding.userName.setOnClickListener(view -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        binding.userEmail.setOnClickListener(view -> {
            Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        });

        binding.cardReward.setOnClickListener(view -> {
            //Intent intent = new Intent(MainActivity.this, RewardActivity.class);
            //startActivity(intent);
            Toast.makeText(this, "Coming Soon....", Toast.LENGTH_SHORT).show();
        });
        binding.cardHelp.setOnClickListener(view -> {
            Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(helpIntent);
        });
        binding.cardNotice.setOnClickListener(view -> {
            Intent noticeIntent = new Intent(MainActivity.this, NoticeActivity.class);
            startActivity(noticeIntent);
        });
    }

    private void logOut() {
        makeToast("Signing Out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    FirebaseAuth.getInstance().signOut();
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                });
    }

    private Boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeToast("Permission Granted");
            }
        } else {
            makeToast("Permission Denied");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database
                .collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );

        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> makeToast("Unable to update token"));
    }

    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
    }

}