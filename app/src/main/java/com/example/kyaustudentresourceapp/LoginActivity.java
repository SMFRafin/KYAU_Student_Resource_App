package com.example.kyaustudentresourceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etID, etPass;
    private Button btnLogin;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etID = findViewById(R.id.etID);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String userID = etID.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            final int MIN_ID_LENGTH = 12;
            final int MIN_PASSWORD_LENGTH = 6;

            // Validation checks
            if (userID.isEmpty()) {
                Toast.makeText(this, "Please enter your Student ID.", Toast.LENGTH_LONG).show();
                return;
            }

            if (userID.length() < MIN_ID_LENGTH) {
                Toast.makeText(this, "Student ID must be at least " + MIN_ID_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password.", Toast.LENGTH_LONG).show();
                return;
            }

            if (password.length() < MIN_PASSWORD_LENGTH) {
                Toast.makeText(this, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.", Toast.LENGTH_LONG).show();
                return;
            }

            // Check credentials
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            if (dbHelper.isValidStudent(userID, password)) {
                String name = dbHelper.getStudentName(userID);

                Toast.makeText(this, "Login Successful! Welcome " + name, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("student_name", name);
                intent.putExtra("student_id", userID);
                startActivity(intent);
                finish();
            } else {
                // Check if student ID exists but password is wrong
                if (dbHelper.isValidStudentId(userID)) {
                    Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Invalid Student ID. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Registration button click listener
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}