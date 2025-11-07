package com.example.kyaustudentresourceapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class NoticeBoardActivity extends AppCompatActivity {

    private RecyclerView recyclerNotices;
    private NoticeAdapter adapter;
    private List<Notice> noticeList;
    private FirebaseFirestore firestore;

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
        noticeList = new ArrayList<>();
        adapter = new NoticeAdapter(this, noticeList);
        recyclerNotices.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        loadNoticesRealtime();
    }

    private void loadNoticesRealtime() {
        firestore.collection("notices")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(NoticeBoardActivity.this, "Failed to load notices", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        noticeList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            String title = doc.getString("title");
                            String body = doc.getString("body");
                            String date = doc.getString("date");
                            String category = doc.getString("category");

                            noticeList.add(new Notice(title, body, date, category));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
