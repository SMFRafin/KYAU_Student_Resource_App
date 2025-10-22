package com.example.kyaustudentresourceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etRegName, etRegID, etRegPassword, etRegConfirmPassword;
    private Button btnRegister, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        etRegName = findViewById(R.id.etRegName);
        etRegID = findViewById(R.id.etRegID);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnRegister.setOnClickListener(v -> {
            String name = etRegName.getText().toString().trim();
            String studentId = etRegID.getText().toString().trim();
            String password = etRegPassword.getText().toString().trim();
            String confirmPassword = etRegConfirmPassword.getText().toString().trim();

            // Validation constants
            final int MIN_ID_LENGTH = 12;
            final int MIN_PASSWORD_LENGTH = 6;
            final int MIN_NAME_LENGTH = 2;

            // Validation checks
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_LONG).show();
                return;
            }

            if (name.length() < MIN_NAME_LENGTH) {
                Toast.makeText(this, "Name must be at least " + MIN_NAME_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
                return;
            }

            if (studentId.isEmpty()) {
                Toast.makeText(this, "Please enter your Student ID.", Toast.LENGTH_LONG).show();
                return;
            }

            if (studentId.length() < MIN_ID_LENGTH) {
                Toast.makeText(this, "Student ID must be at least " + MIN_ID_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
                return;
            }

            // Check if Student ID starts with valid prefix (optional validation)
            if (!studentId.startsWith("0622")) {
                Toast.makeText(this, "Student ID must start with 0622.", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter a password.", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.length() < MIN_PASSWORD_LENGTH) {
                Toast.makeText(this, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
                return;
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please confirm your password.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            // Attempt registration
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            if (dbHelper.registerStudent(studentId, name, password)) {
                Toast.makeText(this, "Registration successful! You can now login.", Toast.LENGTH_LONG).show();

                // Clear form fields
                etRegName.setText("");
                etRegID.setText("");
                etRegPassword.setText("");
                etRegConfirmPassword.setText("");

                // Go back to login activity
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                // Put the registered ID in the intent so it can be pre-filled in login
                intent.putExtra("registered_id", studentId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Registration failed. Student ID might already exist.", Toast.LENGTH_LONG).show();
            }
        });

        btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}