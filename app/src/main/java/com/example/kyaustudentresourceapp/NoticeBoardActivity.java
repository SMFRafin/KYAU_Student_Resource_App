package com.example.kyaustudentresourceapp;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NoticeBoardActivity extends AppCompatActivity {

    private RecyclerView recyclerNotices;
    private NoticeAdapter adapter;
    private List<Notice> noticeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notice_board);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.noticeBoard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerNotices = findViewById(R.id.recyclerNotices);
        recyclerNotices.setLayoutManager(new LinearLayoutManager(this));

        // Dummy Data
        noticeList = new ArrayList<>();
        noticeList.add(new Notice(
                "Midterm Exam Schedule Released",
                "The midterm exams will start from Oct 25. Check the routine section for details.",
                "Oct 12, 2025",
                "Exams"));

        noticeList.add(new Notice(
                "Department Picnic Registration",
                "Students can now register for the annual picnic. Limited seats available!",
                "Oct 10, 2025",
                "Events"));

        noticeList.add(new Notice(
                "New Course Material Uploaded",
                "Lecture notes for Data Structures have been updated in the Course Info section.",
                "Oct 9, 2025",
                "Academics"));

        noticeList.add(new Notice(
                "Library Timing Change",
                "The library will remain open until 8 PM on weekdays from this month.",
                "Oct 7, 2025",
                "General"));

        noticeList.add(new Notice(
                "Workshop on AI and Ethics",
                "Join us for a workshop conducted by the CSE Department on Oct 20.",
                "Oct 5, 2025",
                "Seminar"));

        adapter = new NoticeAdapter(this, noticeList);
        recyclerNotices.setAdapter(adapter);
    }
}
