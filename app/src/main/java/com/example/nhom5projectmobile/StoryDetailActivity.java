package com.example.nhom5projectmobile;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

// ĐÃ XÓA DÒNG IMPORT NHẦM org.w3c.dom.Comment Ở ĐÂY

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

    // Biến cho Bình luận
    private RecyclerView rvComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private EditText edtComment;
    private ImageButton btnSendComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông tin truyện");
        }

        // 1. Ánh xạ View cơ bản
        imgCover = findViewById(R.id.imgDetailCover);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvDescription = findViewById(R.id.tvDetailDescription);
        rvChapters = findViewById(R.id.rvChapters);
        btnReadNow = findViewById(R.id.btnReadNow);
        btnReadContinue = findViewById(R.id.btnReadContinue);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnDownload = findViewById(R.id.btnDownload);

        btnReadNow.setEnabled(false);
        btnReadContinue.setEnabled(false);

        // 2. Ánh xạ View cho Tab và Bình luận
        rvComments = findViewById(R.id.rvComments);
        edtComment = findViewById(R.id.edtComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        TabLayout tabLayoutStory = findViewById(R.id.tabLayoutStory);
        View layoutTabGioiThieu = findViewById(R.id.layoutTabGioiThieu);
        View layoutTabBinhLuan = findViewById(R.id.layoutTabBinhLuan);
        View layoutCommentInput = findViewById(R.id.layoutCommentInput);

        // 3. Cài đặt RecyclerView
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setHasFixedSize(true);

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        rvComments.setAdapter(commentAdapter);

        // 4. Lấy dữ liệu Intent
        db = FirebaseFirestore.getInstance();
        storyId = getIntent().getStringExtra("STORY_ID");
        boolean isOffline = getIntent().getBooleanExtra("IS_OFFLINE", false);

        chapterList = new ArrayList<>();
        chapterAdapter = new ChapterAdapter(this, chapterList, storyId);
        rvChapters.setAdapter(chapterAdapter);

        // 5. Cài đặt 2 Tab bằng Java
        tabLayoutStory.addTab(tabLayoutStory.newTab().setText("Giới thiệu"));
        tabLayoutStory.addTab(tabLayoutStory.newTab().setText("Bình luận"));

        tabLayoutStory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                layoutTabGioiThieu.setVisibility(View.GONE);
                layoutTabBinhLuan.setVisibility(View.GONE);
                layoutCommentInput.setVisibility(View.GONE);

                if (tab.getPosition() == 0) {
                    layoutTabGioiThieu.setVisibility(View.VISIBLE);
                } else if (tab.getPosition() == 1) {
                    layoutTabBinhLuan.setVisibility(View.VISIBLE);
                    if (!isOffline) {
                        layoutCommentInput.setVisibility(View.VISIBLE);
                        loadComments(); // Gọi load bình luận khi mở tab
                    }
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 6. Xử lý logic tải truyện
        if (storyId != null) {
            if (isOffline) {
                String offlineTitle = getIntent().getStringExtra("STORY_TITLE");
                String offlineCover = getIntent().getStringExtra("STORY_COVER");
                tvTitle.setText(offlineTitle);
                tvAuthor.setText("Tác giả: Ngoại tuyến");
                tvDescription.setText("Truyện được lưu cục bộ trên máy. Bạn có thể đọc không cần mạng Wifi/3G.");
                if (offlineCover != null && !offlineCover.isEmpty()) {
                    Glide.with(this).load(offlineCover).into(imgCover);
                }
                if (btnBookmark != null) btnBookmark.setVisibility(View.GONE);
                if (btnDownload != null) btnDownload.setVisibility(View.GONE);
                if (chapterAdapter != null) chapterAdapter.isOffline = true;

                loadOfflineChapters();
                checkReadingHistory();
            } else {
                loadStoryDetails();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    currentUserId = currentUser.getUid();
                    checkIfFollowing();
                }
                checkReadingHistory();
            }
        }

        // 7. Bắt các sự kiện Click
        btnSendComment.setOnClickListener(v -> postComment());

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
                for (int i = 0; i < chapterList.size(); i++) {
                    Chapter chapter = chapterList.get(i);
                    DownloadHelper.downloadChapter(this, storyId, storyTitleStr, storyCoverUrl, chapter.getChapterId(), chapter.getTitle(), i);
                }
            });
        }

        btnReadNow.setOnClickListener(v -> {
            if (chapterList != null && !chapterList.isEmpty()) {
                Chapter firstChapter = chapterList.get(0);
                Intent intent = new Intent(StoryDetailActivity.this, ReaderActivity.class);
                intent.putExtra("STORY_ID", storyId);
                intent.putExtra("CHAPTER_ID", firstChapter.getChapterId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Truyện này chưa có chương nào!", Toast.LENGTH_SHORT).show();
            }
        });

        btnReadContinue.setOnClickListener(v -> {
            if (lastReadChapterId != null) {
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
        if (storyId != null) {
            checkReadingHistory();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ================== CÁC HÀM XỬ LÝ DỮ LIỆU ==================

    private void loadStoryDetails() {
        db.collection("stories").document(storyId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        tvTitle.setText(title);
                        tvAuthor.setText("Tác giả: " + documentSnapshot.getString("author"));
                        tvDescription.setText(documentSnapshot.getString("description"));

                        String imageUrl = documentSnapshot.getString("coverImage");
                        Glide.with(this).load(imageUrl).into(imgCover);

                        storyTitleStr = title;
                        storyCoverUrl = imageUrl;

                        if (chapterAdapter != null) {
                            chapterAdapter.setStoryTitle(storyTitleStr);
                            chapterAdapter.setCoverUrl(storyCoverUrl);
                        }
                        loadChapters();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadChapters() {
        db.collection("stories").document(storyId).collection("chapters").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chapterList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        chapterList.add(doc.toObject(Chapter.class));
                    }
                    chapterList.sort((a, b) -> Long.compare(a.getOrderIndex(), b.getOrderIndex()));
                    chapterAdapter.notifyDataSetChanged();

                    if (!chapterList.isEmpty()) {
                        currentPdfUrl = chapterList.get(0).getContent();
                        btnReadNow.setEnabled(true);
                        btnReadContinue.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải chương: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadOfflineChapters() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        chapterList.clear();
        chapterList.addAll(dbHelper.getOfflineChapters(storyId));
        chapterAdapter.notifyDataSetChanged();

        if (!chapterList.isEmpty()) {
            btnReadNow.setEnabled(true);
            btnReadContinue.setEnabled(true);
        }
    }

    // ================== CÁC HÀM BÌNH LUẬN ==================

    private void loadComments() {
        if (storyId == null) return;

        db.collection("stories").document(storyId).collection("comments")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải bình luận", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        commentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            commentList.add(doc.toObject(Comment.class)); // Đây là class Comment của tụi mình
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void postComment() {
        String content = edtComment.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName();
        if (userName == null || userName.isEmpty()) {
            String email = currentUser.getEmail();
            userName = (email != null && email.contains("@")) ? email.split("@")[0] : "Người dùng ẩn danh";
        }

        Comment newComment = new Comment(userId, userName, content, com.google.firebase.Timestamp.now());

        db.collection("stories").document(storyId)
                .collection("comments")
                .add(newComment)
                .addOnSuccessListener(documentReference -> edtComment.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ================== CÁC HÀM KHÁC (Bookmark, Lịch sử) ==================

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
        isFollowing = !isFollowing;
        updateBookmarkUI();

        if (isFollowing) {
            storyRef.update("followers", FieldValue.arrayUnion(currentUserId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã theo dõi truyện", Toast.LENGTH_SHORT).show();
                        FirebaseMessaging.getInstance().subscribeToTopic("story_" + storyId)
                                .addOnFailureListener(e -> android.util.Log.e("StoryDetail", "Lỗi subscribe: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> {
                        isFollowing = false;
                        updateBookmarkUI();
                    });
        } else {
            storyRef.update("followers", FieldValue.arrayRemove(currentUserId))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã bỏ theo dõi", Toast.LENGTH_SHORT).show();
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("story_" + storyId)
                                .addOnFailureListener(e -> android.util.Log.e("StoryDetail", "Lỗi unsubscribe: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> {
                        isFollowing = true;
                        updateBookmarkUI();
                    });
        }
    }

    private void updateBookmarkUI() {
        if (btnBookmark == null) return;
        if (isFollowing) {
            btnBookmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        } else {
            btnBookmark.setImageTintList(ColorStateList.valueOf(Color.parseColor("#666666")));
        }
    }

    private void checkReadingHistory() {
        if (storyId == null) return;

        // Đoạn code cũ của bạn lấy lịch sử cục bộ
        String localLastRead = getSharedPreferences("ReadingHistory", MODE_PRIVATE).getString(storyId, null);
        if (localLastRead != null) {
            lastReadChapterId = localLastRead;
            btnReadContinue.setEnabled(true);
        }

        boolean isOffline = getIntent().getBooleanExtra("IS_OFFLINE", false);
        if (!isOffline && currentUserId != null) {
            db.collection("users").document(currentUserId)
                    .collection("history").document(storyId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fbLastRead = documentSnapshot.getString("lastReadChapterId");
                            if (fbLastRead != null) {
                                lastReadChapterId = fbLastRead;
                                btnReadContinue.setEnabled(true);
                                getSharedPreferences("ReadingHistory", MODE_PRIVATE).edit().putString(storyId, fbLastRead).apply();
                            }
                            List<String> fbReadChapters = (List<String>) documentSnapshot.get("readChapters");
                            if (fbReadChapters != null) {
                                android.content.SharedPreferences.Editor editor = getSharedPreferences("ReadChapters", MODE_PRIVATE).edit();
                                for (String chId : fbReadChapters) {
                                    // Lưu dấu vết từng chương đã đọc
                                    editor.putBoolean(storyId + "_" + chId, true);
                                }
                                editor.apply();
                            }

                            // Gọi hàm cập nhật Adapter
                            updateAdapterHistory();
                        } else {
                            updateAdapterHistory();
                        }
                    });
        } else {
            // Nếu là offline, vẫn cập nhật adapter bình thường
            updateAdapterHistory();
        }
    }

    // THÊM HÀM MỚI NÀY VÀO NGAY DƯỚI HÀM checkReadingHistory()
    private void updateAdapterHistory() {
        if (chapterAdapter == null) return;

        // Quét toàn bộ SharedPreferences để tìm các chương đã đọc của truyện này
        java.util.Set<String> readList = new java.util.HashSet<>();
        java.util.Map<String, ?> allEntries = getSharedPreferences("ReadChapters", MODE_PRIVATE).getAll();

        for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            // Nếu key chứa mã truyện này (ví dụ: "story123_chap1") -> cắt lấy mã chương
            if (key.startsWith(storyId + "_") && (Boolean) entry.getValue()) {
                String chId = key.replace(storyId + "_", "");
                readList.add(chId);
            }
        }

        // Bơm danh sách vào Adapter để đổi màu xanh + gắn Bookmark
        chapterAdapter.setReadHistory(lastReadChapterId, readList);
    }
}