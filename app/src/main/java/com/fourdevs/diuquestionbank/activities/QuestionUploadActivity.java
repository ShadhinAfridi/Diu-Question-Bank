package com.fourdevs.diuquestionbank.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.fourdevs.diuquestionbank.R;
import com.fourdevs.diuquestionbank.databinding.ActivityQuestionUploadBinding;
import com.fourdevs.diuquestionbank.databinding.ProgressBinding;
import com.fourdevs.diuquestionbank.utilities.Constants;
import com.fourdevs.diuquestionbank.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class QuestionUploadActivity extends BaseActivity {

    private ActivityQuestionUploadBinding binding;
    private ProgressBinding progressBinding;
    private Uri pdfUri;
    private String department, courseCode, semester, year, exam;
    private Dialog dialog;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        progressBinding = ProgressBinding.inflate(getLayoutInflater());
        database = FirebaseFirestore.getInstance();
        builder.setView(progressBinding.getRoot());
        dialog = builder.create();
        initializeSpinner();
        setListeners();
        notificationChannel();
    }

    private void setListeners() {
        binding.selectPdf.setOnClickListener(view -> {
            if(checkPermissions()) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                pickPdf.launch(intent);
            } else {
                makeToast("Storage permission denied");
            }

        });
        binding.buttonUpload.setOnClickListener(view -> findData());
        binding.imageBack.setOnClickListener(view -> onBackPressed());
    }

    private void initializeSpinner() {
        Calendar calendar = Calendar.getInstance();
        List<String> date_list = new ArrayList<>();
        int current_year = calendar.get(Calendar.YEAR);
        String add_year;
        for(int i=current_year; i>=2015; i--){
            add_year = Integer.toString(i);
            date_list.add(add_year);
        }
        ArrayAdapter<String> year_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, date_list);
        year_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.yearSpinner.setAdapter(year_adapter);

        ArrayAdapter<CharSequence> dept_adapter = ArrayAdapter
                .createFromResource(this, R.array.department_name, android.R.layout.simple_spinner_item);
        dept_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.departmentSpinner.setAdapter(dept_adapter);

        ArrayAdapter<CharSequence> exam_adapter = ArrayAdapter
                .createFromResource(this, R.array.exam_name, android.R.layout.simple_spinner_item);
        exam_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.examSpinner.setAdapter(exam_adapter);
    }

    @SuppressLint("SetTextI18n")
    private final ActivityResultLauncher<Intent> pickPdf = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null) {
                        pdfUri = result.getData().getData();
                        binding.fileName.setText(" "+getFileName());
                        binding.fileName.setVisibility(View.VISIBLE);
                        binding.selectPdf.setText("Selected");
                    }
                }
            }
    );

    private String getFileName() {
        DocumentFile file = DocumentFile.fromSingleUri(this, pdfUri);
        assert file != null;
        return file.getName();
    }

    private void makeToast(String value) {
        Toast.makeText(getApplicationContext(),value,Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void uploadPdf(Uri uri) {
        setDialog(true);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference filepath = storageReference
                .child(
                        "("+new Date()+")"+courseCode+"_"+exam+"_"+semester+"_"+year+"_"+preferenceManager.getString(Constants.KEY_USER_ID)+"."+"pdf"
                );
        UploadTask uploadTask = filepath.putFile(uri);

        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            progressBinding.message.setText("Complete "+(int)progress+"%");

        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String pdfUrl = filepath.getName();
                updateQuestionsDetails(pdfUrl);
            } else {
                makeToast("Failed!");
            }
        });
    }


    private void findData() {
        department = binding.departmentSpinner.getSelectedItem().toString().trim();
        courseCode = binding.courseCode.getText().toString().trim().toUpperCase();
        year = binding.yearSpinner.getSelectedItem().toString().trim();
        exam = binding.examSpinner.getSelectedItem().toString().trim();

        if(binding.semesterGroup.getCheckedRadioButtonId() == -1){
            makeToast("Please Select Semester.");
            return;
        } else {
            int selectedSemesterId = binding.semesterGroup.getCheckedRadioButtonId();
            RadioButton semesterBtn =  findViewById(selectedSemesterId);
            semester = semesterBtn.getText().toString().trim();
        }
        if (courseCode.isEmpty()) {
            binding.courseCode.setError("Enter Course Code");
            binding.courseCode.requestFocus();
            return;
        }
        if (pdfUri == null){
            makeToast("Please Select PDF file.");
            return;
        }
        uploadPdf(pdfUri);
    }

    private void updateQuestionsDetails(String uri) {
        HashMap<String, Object> question = new HashMap<>();
        question.put(Constants.KEY_DEPARTMENT, department);
        question.put(Constants.KEY_EXAM, exam);
        question.put(Constants.KEY_COURSE_CODE, courseCode.toUpperCase());
        question.put(Constants.KEY_SEMESTER, semester);
        question.put(Constants.KEY_YEAR, year);
        question.put(Constants.KEY_PDF_URL, uri);
        question.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        question.put(Constants.KEY_IS_APPROVED, false);
        question.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_QUESTIONS)
                .add(question)
                .addOnCompleteListener(task -> {
                    clearForm();
                    setDialog(false);
                    makeToast("Successfully uploaded!");
                    createNotification("Done", "Your upload has been completed successfully.");
                    preferenceManager.putBoolean(Constants.KEY_COUNT_ONCE, false);
                }).addOnFailureListener(e -> {
                    makeToast(e.getMessage());
                    createNotification("Failed!", "Your upload has failed.");
                });
    }

    private Boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("SetTextI18n")
    private void clearForm() {
        binding.departmentSpinner.setSelection(0);
        binding.courseCode.setText("");
        binding.semesterGroup.clearCheck();
        binding.yearSpinner.setSelection(0);
        binding.examSpinner.setSelection(0);
        pdfUri = null;
        binding.selectPdf.setText("Select PDF");
        binding.fileName.setText("");
        binding.fileName.setVisibility(View.INVISIBLE);
    }

    private void setDialog(boolean show){
        if (show)dialog.show();
        else dialog.dismiss();
    }

    private void notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "question_upload",
                    "Question Upload",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "question_upload")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        Intent intent = new Intent(this, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }




}