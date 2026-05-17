package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoryDetailActivity extends AppCompatActivity {

    private ImageView imgCover;
    private TextView tvTitle, tvAuthor, tvDescription;
    private FirebaseFirestore db;
    private String storyId;
    private String currentPdfUrl;
    private RecyclerView rvChapters;
    private ChapterAdapter chapterAdapter;
    private List<Chapter> chapterList;
    private Button btnReadNow, btnReadContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        // 1. Cấu hình Nút quay lại (Phải nằm trong onCreate)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông tin truyện");
        }

        // 2. Ánh xạ View
        imgCover = findViewById(R.id.imgDetailCover);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvDescription = findViewById(R.id.tvDetailDescription);
        btnReadNow = findViewById(R.id.btnReadNow);
        btnReadNow.setEnabled(false);
        rvChapters = findViewById(R.id.rvChapters);
        btnReadNow = findViewById(R.id.btnReadNow);
        btnReadContinue = findViewById(R.id.btnReadContinue); // Ánh xạ nút mới

        btnReadNow.setEnabled(false);
        btnReadContinue.setEnabled(false); // Khóa luôn nút này chờ data

        // 3. Cấu hình RecyclerView
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setHasFixedSize(true);

        db = FirebaseFirestore.getInstance();
        storyId = getIntent().getStringExtra("STORY_ID");

        // 4. Thiết lập Adapter
        chapterList = new ArrayList<>();
        chapterAdapter = new ChapterAdapter(this, chapterList, storyId);
        rvChapters.setAdapter(chapterAdapter);

        if (storyId != null) {
            loadStoryDetails();
        }

        // BẮT SỰ KIỆN NÚT "ĐỌC TỪ ĐẦU"
        btnReadNow.setOnClickListener(v -> {
            if (chapterList != null && !chapterList.isEmpty()) {
                // Lấy chương đầu tiên trong danh sách (Vị trí 0)
                Chapter firstChapter = chapterList.get(0);

                Intent intent = new Intent(StoryDetailActivity.this, ReaderActivity.class);
                intent.putExtra("STORY_ID", storyId);
                intent.putExtra("CHAPTER_ID", firstChapter.getChapterId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Truyện này chưa có chương nào!", Toast.LENGTH_SHORT).show();
            }
        });

        // BẮT SỰ KIỆN NÚT "ĐỌC TIẾP"
        btnReadContinue.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng Đọc tiếp sẽ sớm ra mắt!", Toast.LENGTH_SHORT).show();
            // TODO: Nơi viết logic lấy Chapter ID đang đọc dở từ Firebase và mở ReaderActivity
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // Xử lý sự kiện khi nhấn nút back trên Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Thay toàn bộ 2 hàm loadStoryDetails() và loadChapters() bằng phần này

    private void loadStoryDetails() {
        db.collection("stories").document(storyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvTitle.setText(documentSnapshot.getString("title"));
                        tvAuthor.setText("Tác giả: " + documentSnapshot.getString("author"));
                        tvDescription.setText(documentSnapshot.getString("description"));

                        String imageUrl = documentSnapshot.getString("coverImage");
                        Glide.with(this).load(imageUrl).into(imgCover);

                        // Gọi load chương SAU khi có dữ liệu story
                        loadChapters();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadChapters() {
        db.collection("stories").document(storyId).collection("chapters")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chapterList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        chapterList.add(doc.toObject(Chapter.class));
                    }

                    // Sắp xếp thủ công, không cần Firestore index
                    chapterList.sort((a, b) -> Long.compare(a.getOrderIndex(), b.getOrderIndex()));

                    chapterAdapter.notifyDataSetChanged();

                    // Gán URL chương đầu tiên — đây là lúc currentPdfUrl mới có giá trị
                    if (!chapterList.isEmpty()) {
                        currentPdfUrl = chapterList.get(0).getContent();
                    }

                    // Kích hoạt nút Đọc ngay và Đọc tiếp
                    if (!chapterList.isEmpty()) {
                        btnReadNow.setEnabled(true);
                        btnReadContinue.setEnabled(true);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải chương: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}