package com.example.applicationessect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import javax.annotation.Nullable;

public class AdminInterface extends AppCompatActivity {

    private EditText editText, editTextName, editTextNumber, editTextEmail, editTextPassword, editTextRole, editTextScheduleId, editTextSubject, editTextAbsences;
    private Button btn, buttonAddUser, buttonUpdateUser, buttonDeleteUser;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth; // FirebaseAuth instance

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_interface);

        // Initialize Firebase instances
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("UploadPDF");

        // Initialize UI components
        editText = findViewById(R.id.editText);
        btn = findViewById(R.id.buttonUploadPdf);
        editTextName = findViewById(R.id.editTextName);
        editTextNumber = findViewById(R.id.editTextNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextRole = findViewById(R.id.editTextRole);
        editTextScheduleId = findViewById(R.id.editTextScheduleId);
        editTextSubject = findViewById(R.id.editTextSubject);
        editTextAbsences = findViewById(R.id.editTextAbsences);

        buttonAddUser = findViewById(R.id.buttonAddUser);
        buttonUpdateUser = findViewById(R.id.buttonUpdateUser);
        buttonDeleteUser = findViewById(R.id.buttonDeleteUser);

        // Disable upload button initially
        btn.setEnabled(false);

        // Set up listeners
        editText.setOnClickListener(v -> selectPDF());
        btn.setOnClickListener(v -> uploadPDFFILEFirebase(editText.getText().toString()));

        buttonAddUser.setOnClickListener(v -> addUser());
        buttonUpdateUser.setOnClickListener(v -> updateUser());
        buttonDeleteUser.setOnClickListener(v -> deleteUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            btn.setEnabled(true);
            Uri pdfUri = data.getData();
            editText.setText(pdfUri.getLastPathSegment());
        }
    }

    private void selectPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF file"), 12);
    }

    private void uploadPDFFILEFirebase(String pdfPath) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File loading");
        progressDialog.show();

        Uri pdfUri = Uri.parse(pdfPath);
        StorageReference reference = storageReference.child("uploads/" + System.currentTimeMillis() + ".pdf");

        reference.putFile(pdfUri)
                .addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String pdfUrl = uri.toString();
                    putPDF pdfData = new putPDF(pdfPath, pdfUrl);
                    databaseReference.child(databaseReference.push().getKey()).setValue(pdfData);

                    progressDialog.dismiss();
                    Toast.makeText(AdminInterface.this, "PDF Uploaded Successfully", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(AdminInterface.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(AdminInterface.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(name, editTextNumber.getText().toString().trim(), email, password, editTextRole.getText().toString().trim(),
                editTextScheduleId.getText().toString().trim(), editTextSubject.getText().toString().trim(), Integer.parseInt(editTextAbsences.getText().toString().trim()));

        firestore.collection("users").document(email)
                .set(user)
                .addOnSuccessListener(aVoid -> auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AdminInterface.this, "User added and authenticated successfully", Toast.LENGTH_SHORT).show();
                                clearFields();
                            } else {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                                    Toast.makeText(AdminInterface.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AdminInterface.this, "Authentication error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }))
                .addOnFailureListener(e -> Toast.makeText(AdminInterface.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUser() {
        String email = editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(AdminInterface.this, "Please enter email to update user", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(email)
                .update("name", editTextName.getText().toString().trim(),
                        "number", editTextNumber.getText().toString().trim(),
                        "password", editTextPassword.getText().toString().trim(),
                        "role", editTextRole.getText().toString().trim(),
                        "scheduleId", editTextScheduleId.getText().toString().trim(),
                        "subject", editTextSubject.getText().toString().trim(),
                        "absences", Integer.parseInt(editTextAbsences.getText().toString().trim()))
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminInterface.this, "User updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminInterface.this, "Error updating user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteUser() {
        String email = editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(AdminInterface.this, "Please enter email to delete user", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(email)
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminInterface.this, "User deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminInterface.this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        editTextName.setText("");
        editTextNumber.setText("");
        editTextEmail.setText("");
        editTextPassword.setText("");
        editTextRole.setText("");
        editTextScheduleId.setText("");
        editTextSubject.setText("");
        editTextAbsences.setText("");
    }
    public class putPDF {
        private String pdfPath;
        private String pdfUrl;

        // Constructor
        public putPDF(String pdfPath, String pdfUrl) {
            this.pdfPath = pdfPath;
            this.pdfUrl = pdfUrl;
        }

        // Getter methods
        public String getPdfPath() {
            return pdfPath;
        }

        public String getPdfUrl() {
            return pdfUrl;
        }

        // Setter methods
        public void setPdfPath(String pdfPath) {
            this.pdfPath = pdfPath;
        }

        public void setPdfUrl(String pdfUrl) {
            this.pdfUrl = pdfUrl;
        }
    }

    // User data model
    public static class User {
        private String name;
        private String number;
        private String email;
        private String password;
        private String role;
        private String scheduleId;
        private String subject;
        private int absences;

        public User() {}

        public User(String name, String number, String email, String password, String role, String scheduleId, String subject, int absences) {
            this.name = name;
            this.number = number;
            this.email = email;
            this.password = password;
            this.role = role;
            this.scheduleId = scheduleId;
            this.subject = subject;
            this.absences = absences;
        }

        // Getters and setters for each field
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(String scheduleId) {
            this.scheduleId = scheduleId;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public int getAbsences() {
            return absences;
        }

        public void setAbsences(int absences) {
            this.absences = absences;
        }
    }
}
