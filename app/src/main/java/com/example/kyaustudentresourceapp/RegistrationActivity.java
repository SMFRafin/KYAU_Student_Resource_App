package com.example.kyaustudentresourceapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etRegName, etRegID, etRegPassword, etRegConfirmPassword,etRegBatch,etRegDept;
    private Button btnRegister, btnBackToLogin;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        etRegName = findViewById(R.id.etRegName);
        etRegID = findViewById(R.id.etRegID);
        etRegBatch=findViewById(R.id.etRegBatch);
        etRegDept=findViewById(R.id.etRegDept);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnRegister.setOnClickListener(v -> registerStudent());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerStudent() {
        String name = etRegName.getText().toString().trim();
        String studentId = etRegID.getText().toString().trim();
        String batch=etRegBatch.getText().toString().trim();
        String dept=etRegDept.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();

        String confirmPassword = etRegConfirmPassword.getText().toString().trim();

        // Validation constants
        final int MIN_ID_LENGTH = 12;
        final int MIN_PASSWORD_LENGTH = 6;
        final int MIN_NAME_LENGTH = 2;

        // Input validation
        if (name.isEmpty() || studentId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_LONG).show();
            return;
        }
        if (name.length() < MIN_NAME_LENGTH) {
            Toast.makeText(this, "Name must be at least " + MIN_NAME_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
            return;
        }
        if (studentId.length() < MIN_ID_LENGTH) {
            Toast.makeText(this, "Student ID must be at least " + MIN_ID_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!studentId.startsWith("0622")) {
            Toast.makeText(this, "Student ID must start with 0622.", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // ✅ Check if ID already exists in Firestore
        firestore.collection("students").document(studentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Student ID already exists.", Toast.LENGTH_LONG).show();
                    } else {
                        // Prepare student data
                        Map<String, Object> student = new HashMap<>();
                        student.put("student_id", studentId);
                        student.put("name", name);
                        student.put("password", password);
                        student.put("batch", batch);
                        student.put("department",dept);

                        // ✅ Add to Firestore
                        firestore.collection("students").document(studentId)
                                .set(student)
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show();

                                    // (Optional) Cache locally for offline access
                                    DatabaseHelper dbHelper = new DatabaseHelper(this);
                                    dbHelper.registerStudent(studentId, name, password, batch,dept);

                                    // Clear form
                                    etRegName.setText("");
                                    etRegID.setText("");
                                    etRegPassword.setText("");
                                    etRegConfirmPassword.setText("");

                                    // Redirect to Login
                                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                    intent.putExtra("registered_id", studentId);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to check existing ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
