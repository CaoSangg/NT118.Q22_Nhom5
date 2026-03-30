package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class StoryDetailActivity extends AppCompatActivity {

    private ImageView imgCover;
    private TextView tvTitle, tvAuthor, tvDescription;
    private FirebaseFirestore db;
    private String storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        // 1. Ánh xạ View
        imgCover = findViewById(R.id.imgDetailCover);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvDescription = findViewById(R.id.tvDetailDescription);
        db = FirebaseFirestore.getInstance();

        // 2. Nhận ID truyện từ HomeFragment gửi sang
        storyId = getIntent().getStringExtra("STORY_ID");

        if (storyId != null) {
            loadStoryDetails();
        }
    }

    private void loadStoryDetails() {
        // Truy vấn trực tiếp vào document của truyện đó
        db.collection("stories").document(storyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Đổ dữ liệu lên giao diện
                        tvTitle.setText(documentSnapshot.getString("title"));
                        tvAuthor.setText("Tác giả: " + documentSnapshot.getString("author"));
                        tvDescription.setText(documentSnapshot.getString("description"));

                        String imageUrl = documentSnapshot.getString("coverImage");
                        Glide.with(this).load(imageUrl).into(imgCover);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}