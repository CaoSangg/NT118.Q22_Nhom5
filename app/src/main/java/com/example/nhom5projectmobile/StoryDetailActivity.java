package com.example.nhom5projectmobile;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StoryDetailActivity extends AppCompatActivity {

    private ImageView imgCover;
    private ImageButton btnBookmark;
    private ImageButton btnDownload;
    private String storyTitleStr = "Truyện Offline";
    private String storyCoverUrl = "";
    private boolean isFollowing = false;
    private String currentUserId = null;
    private TextView tvTitle, tvAuthor, tvDescription;
    private FirebaseFirestore db;
    private String storyId;
    private String currentPdfUrl;
    private RecyclerView rvChapters;
    private ChapterAdapter chapterAdapter;
    private List<Chapter> chapterList;
    private Button btnReadNow, btnReadContinue;
    private String lastReadChapterId = null;

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

        btnBookmark = findViewById(R.id.btnBookmark);
        btnDownload = findViewById(R.id.btnDownload);

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
        boolean isOffline = getIntent().getBooleanExtra("IS_OFFLINE", false);
        if (storyId != null) {
            if (isOffline) {
                // CHẾ ĐỘ OFFLINE: Không thèm gọi Firebase, lấy thẳng từ SQLite
                String offlineTitle = getIntent().getStringExtra("STORY_TITLE");
                String offlineCover = getIntent().getStringExtra("STORY_COVER");
                tvTitle.setText(offlineTitle);
                tvAuthor.setText("Tác giả: Ngoại tuyến");
                tvDescription.setText("Truyện được lưu cục bộ trên máy. Bạn có thể đọc không cần mạng Wifi/3G.");
                if (offlineCover != null && !offlineCover.isEmpty()) {
                    Glide.with(this).load(offlineCover).into(imgCover);
                }
                if (btnBookmark != null) btnBookmark.setVisibility(View.GONE); // Ẩn nút theo dõi
                if (btnDownload != null) btnDownload.setVisibility(View.GONE); // Ẩn nút tải xuống

                // Bơm cờ offline vào Adapter để nó biết đường truyền tiếp sang màn hình đọc
                if (chapterAdapter != null) chapterAdapter.isOffline = true;

                loadOfflineChapters(); // Hàm tự viết ở dưới
            } else {
                // CHẾ ĐỘ ONLINE: Chạy bình thường như cũ
                loadStoryDetails();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    currentUserId = currentUser.getUid();
                    checkIfFollowing();
                    checkReadingHistory();
                }
            }
        }
        if (btnBookmark != null) {
            btnBookmark.setOnClickListener(v -> {
                if (currentUserId == null) {
                    Toast.makeText(this, "Bạn cần đăng nhập để theo dõi truyện!", Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleFollowStatus();
            });
        }
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> {
                if (chapterList == null || chapterList.isEmpty()) {
                    Toast.makeText(this, "Truyện chưa có chương nào để tải!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, "Bắt đầu tải toàn bộ " + chapterList.size() + " chương...", Toast.LENGTH_SHORT).show();

                // Vòng lặp: Lôi từng chương ra đưa cho anh công nhân tải ngầm
                for (int i = 0; i < chapterList.size(); i++) {
                    Chapter chapter = chapterList.get(i);

                    DownloadHelper.downloadChapter(
                            this,
                            storyId,
                            storyTitleStr,
                            storyCoverUrl,
                            chapter.getChapterId(),
                            chapter.getTitle(),
                            i // <-- Truyền thẳng biến i này vào làm số thứ tự
                    );
                }
            });
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
            if (lastReadChapterId != null) {
                // Mở ReaderActivity và truyền ID của chương đang đọc dở vào
                Intent intent = new Intent(StoryDetailActivity.this, ReaderActivity.class);
                intent.putExtra("STORY_ID", storyId);
                intent.putExtra("CHAPTER_ID", lastReadChapterId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Bạn chưa đọc truyện này, hãy nhấn Đọc từ đầu!", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null && storyId != null) {
            checkReadingHistory();
        }
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
                        String title = documentSnapshot.getString("title");

                        tvTitle.setText(title); // Truyền biến title vào đây
                        tvAuthor.setText("Tác giả: " + documentSnapshot.getString("author"));
                        tvDescription.setText(documentSnapshot.getString("description"));

                        String imageUrl = documentSnapshot.getString("coverImage");
                        Glide.with(this).load(imageUrl).into(imgCover);

                        storyTitleStr = title; // Lúc này máy đã hiểu title là gì
                        storyCoverUrl = imageUrl;

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
    private void checkIfFollowing() {
        if (storyId == null || currentUserId == null) return;

        db.collection("stories").document(storyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> followers = (List<String>) documentSnapshot.get("followers");
                        isFollowing = followers != null && followers.contains(currentUserId);
                        updateBookmarkUI();
                    }
                });
    }
    private void toggleFollowStatus() {
        if (storyId == null || currentUserId == null) return;

        DocumentReference storyRef = db.collection("stories").document(storyId);
        isFollowing = !isFollowing; // Đảo trạng thái hiển thị liền cho mượt
        updateBookmarkUI();

        if (isFollowing) {
            storyRef.update("followers", FieldValue.arrayUnion(currentUserId))
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã theo dõi truyện", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        isFollowing = false; // Rollback
                        updateBookmarkUI();
                    });
        } else {
            storyRef.update("followers", FieldValue.arrayRemove(currentUserId))
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã bỏ theo dõi", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        isFollowing = true; // Rollback
                        updateBookmarkUI();
                    });
        }
    }
    private void updateBookmarkUI() {
        if (btnBookmark == null) return;

        if (isFollowing) {
            // Đổi sang màu xanh khi đã theo dõi
            btnBookmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        } else {
            // Trở về màu xám khi chưa theo dõi
            btnBookmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
        }
    }
    private void checkReadingHistory() {
        if (currentUserId == null || storyId == null) return;

        db.collection("users").document(currentUserId)
                .collection("history").document(storyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Nếu có lịch sử, lấy ID chương ra
                        lastReadChapterId = documentSnapshot.getString("lastReadChapterId");
                    }
                });
    }
    private void loadOfflineChapters() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        chapterList.clear();
        chapterList.addAll(dbHelper.getOfflineChapters(storyId)); // Lấy danh sách chương từ SQLite
        chapterAdapter.notifyDataSetChanged();

        if (!chapterList.isEmpty()) {
            btnReadNow.setEnabled(true);
            btnReadContinue.setEnabled(true);
        }
    }
}