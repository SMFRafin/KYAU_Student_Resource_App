package com.example.kyaustudentresourceapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private CardView btnLogout;
    private CardView cardClassRoutine;
    private CardView cardNoticeBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TextView nameBox = findViewById(R.id.tvStudentName);
        btnLogout = findViewById(R.id.cardLogout);
        cardClassRoutine = findViewById(R.id.cardClassRoutine);
        cardNoticeBoard = findViewById(R.id.cardNoticeBoard);

        String studentName = getIntent().getStringExtra("student_name");
        if (studentName != null) {
            nameBox.setText("Welcome, " + studentName + "!");
        } else {
            nameBox.setText("Welcome!");
        }

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardClassRoutine.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RoutineActivity.class);
            startActivity(intent);
        });

        cardNoticeBoard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoticeBoardActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
