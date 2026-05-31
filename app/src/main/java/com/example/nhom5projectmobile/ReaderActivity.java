package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReaderActivity extends AppCompatActivity {

    private RecyclerView rvReader;
    private ReaderAdapter adapter;
    private List<String> pageList;
    private FirebaseFirestore db;
    private String storyId, chapterId;

    private ImageView btnReaderBack, btnPrevChapter, btnNextChapter;
    private TextView tvReaderChapterTitle;
    private Spinner spinnerChapters;

    private List<String> chapterIdList;
    private List<String> chapterTitleList;
    private ArrayAdapter<String> spinnerAdapter;
    private int currentChapterIndex = -1;
    private boolean isUserSelecting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Ánh xạ View
        rvReader = findViewById(R.id.rvReader);
        btnReaderBack = findViewById(R.id.btnReaderBack);
        btnPrevChapter = findViewById(R.id.btnPrevChapter);
        btnNextChapter = findViewById(R.id.btnNextChapter);
        tvReaderChapterTitle = findViewById(R.id.tvReaderChapterTitle);
        spinnerChapters = findViewById(R.id.spinnerChapters);

        db = FirebaseFirestore.getInstance();
        storyId = getIntent().getStringExtra("STORY_ID");
        chapterId = getIntent().getStringExtra("CHAPTER_ID");

        pageList = new ArrayList<>();
        adapter = new ReaderAdapter(this, pageList);
        rvReader.setLayoutManager(new LinearLayoutManager(this));
        rvReader.setAdapter(adapter);

        chapterIdList = new ArrayList<>();
        chapterTitleList = new ArrayList<>();

        // 2. Setup Nút Quay Lại
        btnReaderBack.setOnClickListener(v -> finish());

        // 3. Setup Spinner Adapter
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_centered, chapterTitleList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChapters.setAdapter(spinnerAdapter);

        // Bắt sự kiện khi người dùng chọn 1 chương từ Spinner
        spinnerChapters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentChapterIndex != position && isUserSelecting) {
                    currentChapterIndex = position;
                    chapterId = chapterIdList.get(currentChapterIndex);
                    updateUIAndLoadChapter();
                }
                isUserSelecting = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 4. Setup Nút Qua Chương Cũ / Mới
        btnPrevChapter.setOnClickListener(v -> {
            if (currentChapterIndex > 0) {
                currentChapterIndex--;
                chapterId = chapterIdList.get(currentChapterIndex);
                isUserSelecting = false;
                spinnerChapters.setSelection(currentChapterIndex);
                updateUIAndLoadChapter();
            } else {
                Toast.makeText(ReaderActivity.this, "Đây là chương đầu tiên!", Toast.LENGTH_SHORT).show();
            }
        });

        btnNextChapter.setOnClickListener(v -> {
            if (currentChapterIndex < chapterIdList.size() - 1) {
                currentChapterIndex++;
                chapterId = chapterIdList.get(currentChapterIndex);
                isUserSelecting = false;
                spinnerChapters.setSelection(currentChapterIndex);
                updateUIAndLoadChapter();
            } else {
                Toast.makeText(ReaderActivity.this, "Bạn đã đọc đến chương mới nhất!", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Kiểm tra xem đang đọc Online hay Offline
        boolean isOffline = getIntent().getBooleanExtra("IS_OFFLINE", false);
        if (isOffline) {
            loadOfflineData(); // Gọi hàm đọc từ SQLite
        } else {
            if (storyId != null && chapterId != null) {
                fetchAllChapters(); // Chạy online từ Firebase cũ
            } else {
                Toast.makeText(this, "Lỗi: Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // ====================================================================
    // CÁC HÀM XỬ LÝ CHUNG
    // ====================================================================

    private void updateUIAndLoadChapter() {
        tvReaderChapterTitle.setText(chapterTitleList.get(currentChapterIndex));

        // Luôn lưu lịch sử đọc cục bộ vào SharedPreferences (chạy cả online và offline)
        if (storyId != null && chapterId != null) {
            getSharedPreferences("ReadingHistory", MODE_PRIVATE)
                    .edit()
                    .putString(storyId, chapterId)
                    .apply();
        }

        // Nếu offline thì lôi ảnh trong máy ra, ngược lại thì gọi Firebase
        if (getIntent().getBooleanExtra("IS_OFFLINE", false)) {
            loadOfflineChapterPages();
        } else {
            loadChapterPages();
            saveReadingProgress(chapterId); // Chỉ lưu lịch sử Firebase khi có mạng
            incrementStoryViews(); // Tự động tăng view khi đọc online
        }
    }

    // ====================================================================
    // CÁC HÀM ĐỌC ONLINE (TỪ FIREBASE)
    // ====================================================================

    private void fetchAllChapters() {
        db.collection("stories").document(storyId).collection("chapters")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chapterIdList.clear();
                    chapterTitleList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        chapterIdList.add(doc.getId());

                        String cTitle = doc.getString("title");
                        if (cTitle == null || cTitle.isEmpty()) {
                            cTitle = "Chương " + doc.getId();
                        }
                        chapterTitleList.add(cTitle);
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    currentChapterIndex = chapterIdList.indexOf(chapterId);
                    if (currentChapterIndex != -1) {
                        isUserSelecting = false;
                        spinnerChapters.setSelection(currentChapterIndex);
                        updateUIAndLoadChapter();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh sách chương", Toast.LENGTH_SHORT).show());
    }

    private void loadChapterPages() {
        db.collection("stories").document(storyId)
                .collection("chapters").document(chapterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> urls = (List<String>) documentSnapshot.get("pages");
                        if (urls != null && !urls.isEmpty()) {
                            pageList.clear();
                            pageList.addAll(urls);
                            adapter.notifyDataSetChanged();
                            rvReader.scrollToPosition(0);
                        } else {
                            pageList.clear();
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "Chương này chưa có ảnh", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveReadingProgress(String currentChapterId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || storyId == null || currentChapterId == null) return;

        String userId = currentUser.getUid();
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("lastReadChapterId", currentChapterId);
        historyData.put("timestamp", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("history").document(storyId)
                .set(historyData)
                .addOnFailureListener(e -> {});
    }

    private void incrementStoryViews() {
        if (storyId == null) return;

        db.collection("stories").document(storyId)
                .update(
                        "viewsCount", FieldValue.increment(1),
                        "dailyViews", FieldValue.increment(1),
                        "viewsWeek", FieldValue.increment(1),
                        "viewsMonth", FieldValue.increment(1)
                )
                .addOnFailureListener(e -> {
                    android.util.Log.e("ReaderActivity", "Lỗi tăng lượt xem: " + e.getMessage());
                });
    }

    // ====================================================================
    // CÁC HÀM ĐỌC OFFLINE (TỪ BỘ NHỚ MÁY)
    // ====================================================================

    private void loadOfflineData() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        chapterIdList.clear();
        chapterTitleList.clear();

        // 1. Đọc toàn bộ danh sách chương offline nạp vào Spinner để chuyển chương
        List<Chapter> offlineChaps = dbHelper.getOfflineChapters(storyId);
        for (Chapter c : offlineChaps) {
            chapterIdList.add(c.getChapterId());
            chapterTitleList.add(c.getTitle());
        }
        spinnerAdapter.notifyDataSetChanged();

        // 2. Định vị chương hiện tại đang đọc
        currentChapterIndex = chapterIdList.indexOf(chapterId);
        if (currentChapterIndex != -1) {
            isUserSelecting = false;
            spinnerChapters.setSelection(currentChapterIndex);
            updateUIAndLoadChapter();
        }
    }

    private void loadOfflineChapterPages() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<String> localPaths = dbHelper.getOfflineChapterPages(chapterId); // Lấy list đường dẫn ảnh cục bộ

        pageList.clear();
        if (localPaths != null && !localPaths.isEmpty()) {
            pageList.addAll(localPaths);
            adapter.notifyDataSetChanged();
            rvReader.scrollToPosition(0); // Cuộn lên đầu trang
        } else {
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Không tìm thấy dữ liệu ảnh offline của chương này", Toast.LENGTH_SHORT).show();
        }
    }
}