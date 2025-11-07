package com.example.kyaustudentresourceapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private CardView btnLogout, cardClassRoutine, cardNoticeBoard, cardResults, cardCourse;
    private TextView nameBox, batchBox, deptBox;
    private View noticeDot;

    private FirebaseFirestore db;
    private SharedPreferences prefs;
    private String currentStudentId, currentStudentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        prefs = getSharedPreferences("NoticePrefs", MODE_PRIVATE);

        // 🔹 Initialize UI
        nameBox = findViewById(R.id.tvStudentName);
        btnLogout = findViewById(R.id.cardLogout);
        cardClassRoutine = findViewById(R.id.cardClassRoutine);
        cardNoticeBoard = findViewById(R.id.cardNoticeBoard);
        cardResults=findViewById(R.id.cardResults);
        cardCourse = findViewById(R.id.cardCourseInfo);
        noticeDot = findViewById(R.id.redDotNotice);

        // 🔹 Receive student info
        String studentName = getIntent().getStringExtra("student_name");
        SharedPreferences prefs = getSharedPreferences("student_prefs", MODE_PRIVATE);
        currentStudentId = prefs.getString("student_id", "");
        currentStudentName = prefs.getString("student_name", "");

        if (studentName != null)
            nameBox.setText("Welcome, " + studentName + "!");

        // 🔴 Check latest notice update from Firestore
        db.collection("notices").document("latest_notice")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        long serverTimestamp = snapshot.getLong("timestamp") != null
                                ? snapshot.getLong("timestamp") : 0;
                        long localTimestamp = prefs.getLong("lastViewedNotice", 0);

                        noticeDot.setVisibility(serverTimestamp > localTimestamp ? View.VISIBLE : View.GONE);
                    }
                });

        // 🟢 Notice Board button
        cardNoticeBoard.setOnClickListener(v -> {
            noticeDot.setVisibility(View.GONE);
            prefs.edit().putLong("lastViewedNotice", System.currentTimeMillis()).apply();

            Intent intent = new Intent(MainActivity.this, NoticeBoardActivity.class);
            startActivity(intent);
        });
        cardResults.setOnClickListener(v -> {
            if (currentStudentId != null && !currentStudentId.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                intent.putExtra("student_id", currentStudentId);
                intent.putExtra("student_name", currentStudentName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Student information not found", Toast.LENGTH_SHORT).show();
            }
        });
        // 🟢 Class Routine button
        cardClassRoutine.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RoutineActivity.class);
            startActivity(intent);
        });

        // In your MainActivity, update the Course Info button click listener
        cardCourse.setOnClickListener(v -> {
            if (currentStudentId != null && !currentStudentId.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, CourseInfoActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Student information not found", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔵 Logout
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // ✅ Adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
