package com.example.kyaustudentresourceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etID, etPass;
    private Button btnLogin;
    private Button btnRegister;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etID = findViewById(R.id.etID);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        dbHelper = new DatabaseHelper(this);
        firestore = FirebaseFirestore.getInstance();

        // Sync students from Firebase when login activity starts
        syncStudentsFromFirebase();

        btnLogin.setOnClickListener(v -> {
            String userID = etID.getText().toString().trim();
            String password = etPass.getText().toString().trim();

            // Admin login
            if (userID.equals("admin") && password.equals("admin123")) {
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
                return;
            }

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

            // Check credentials - first check local, then Firebase
            checkStudentCredentials(userID, password);
        });

        // Registration button click listener
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void syncStudentsFromFirebase() {
        firestore.collection("students")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        // Clear existing students (optional - you might not want to clear)
                        // db.execSQL("DELETE FROM students");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String studentId = document.getString("student_id");
                            String name = document.getString("name");
                            String password = document.getString("password");
                            String batch = document.getString("batch");
                            String department = document.getString("department");

                            // Check if student already exists in local DB
                            Cursor cursor = db.rawQuery("SELECT * FROM students WHERE student_id=?",
                                    new String[]{studentId});
                            if (cursor.getCount() == 0) {
                                // Student doesn't exist locally, add them
                                db.execSQL("INSERT INTO students (student_id, student_name, password, batch, department) VALUES (?,?,?,?,?)",
                                        new String[]{studentId, name, password, batch, department});
                            }
                            cursor.close();
                        }
                        db.close();
                        Log.d("LoginActivity", "Students synced from Firebase");
                    }
                });
    }

    private void checkStudentCredentials(String userID, String password) {
        // First check local database
        if (dbHelper.isValidStudent(userID, password)) {
            loginSuccess(userID);
        } else {
            // If not found locally, check Firebase directly
            checkFirebaseCredentials(userID, password);
        }
    }

    private void checkFirebaseCredentials(String userID, String password) {
        firestore.collection("students")
                .whereEqualTo("student_id", userID)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Valid credentials in Firebase - add to local DB and login
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String batch = document.getString("batch");
                            String department = document.getString("department");

                            // Add to local SQLite
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.execSQL("INSERT OR REPLACE INTO students (student_id, student_name, password, batch, department) VALUES (?,?,?,?,?)",
                                    new String[]{userID, name, password, batch, department});
                            db.close();

                            loginSuccess(userID);
                            return;
                        }
                    } else {
                        // Invalid credentials
                        if (dbHelper.isValidStudentId(userID)) {
                            Toast.makeText(this, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Invalid Student ID. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loginSuccess(String userID) {
        String name = dbHelper.getStudentName(userID);
        saveStudentSession(userID, name);
        Toast.makeText(this, "Login Successful! Welcome " + name, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("student_name", name);
        intent.putExtra("student_id", userID);
        startActivity(intent);
        finish();
    }

    // ✅ Method to save student session in SharedPreferences
    private void saveStudentSession(String studentId, String studentName) {
        SharedPreferences prefs = getSharedPreferences("student_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("student_id", studentId);
        editor.putString("student_name", studentName);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }
}