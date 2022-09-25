package com.fourdevs.diuquestionbank;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.fourdevs.diuquestionbank.databinding.ActivityHelpBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;

public class HelpActivity extends BaseActivity {
    private ActivityHelpBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setUserData();
        setListeners();
    }

    private void setListeners() {
        binding.iconBack.setOnClickListener(view -> onBackPressed());
        binding.buttonSend.setOnClickListener(view -> {
            loading(true);
            updateToDatabase();
        });
    }

    private void setUserData() {
        binding.inputUserName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.inputUserEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
    }


    private void updateToDatabase() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> contact = new HashMap<>();
        contact.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        contact.put(Constants.KEY_SUBJECT, binding.inputSubject.getText().toString().trim());
        contact.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString().trim());
        contact.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CONTACTS)
                .add(contact)
                .addOnCompleteListener(task -> {
                    loading(false);
                    clearForm();
                    makeToast("Sent");
                }).addOnFailureListener(e -> makeToast(e.getMessage()));
    }

    private void makeToast(String value) {
        Toast.makeText(getApplicationContext(),value,Toast.LENGTH_SHORT).show();
    }

    private void clearForm() {
        binding.inputSubject.setText("");
        binding.inputMessage.setText("");
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.GONE);
        }
    }
}