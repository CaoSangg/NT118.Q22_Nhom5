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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReaderActivity extends AppCompatActivity {

    private RecyclerView rvReader;
    private ReaderAdapter adapter;
    private List<String> pageList;
    private FirebaseFirestore db;
    private String storyId, chapterId;

    private ImageView btnReaderBack, btnPrevChapter, btnNextChapter;
    private TextView tvReaderChapterTitle;
    private Spinner spinnerChapters;

    private List<String> chapterIdList;    // Lưu danh sách ID của các chương
    private List<String> chapterTitleList; // Lưu tên các chương để hiển thị lên Spinner
    private ArrayAdapter<String> spinnerAdapter;
    private int currentChapterIndex = -1;  // Vị trí chương đang đọc
    private boolean isUserSelecting = false; // Biến cờ để chống lỗi tự động nhảy chương của Spinner

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
                // Chỉ chuyển chương nếu người dùng thực sự tự tay bấm (isUserSelecting = true)
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
                isUserSelecting = false; // Tắt cờ để Spinner không bị kích hoạt 2 lần
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

        // 5. Thay vì chỉ load ảnh, ta sẽ load toàn bộ danh sách chương trước
        if (storyId != null && chapterId != null) {
            fetchAllChapters();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Lấy toàn bộ chương của truyện để nạp vào Spinner
    private void fetchAllChapters() {
        db.collection("stories").document(storyId).collection("chapters")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chapterIdList.clear();
                    chapterTitleList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        chapterIdList.add(doc.getId());

                        // Cố gắng lấy tên chương từ Firestore. Nếu không có biến title thì hiển thị tạm ID
                        String cTitle = doc.getString("title");
                        if (cTitle == null || cTitle.isEmpty()) {
                            cTitle = "Chương " + doc.getId();
                        }
                        chapterTitleList.add(cTitle);
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    // Tìm vị trí của chương hiện tại đang đọc
                    currentChapterIndex = chapterIdList.indexOf(chapterId);
                    if (currentChapterIndex != -1) {
                        isUserSelecting = false;
                        spinnerChapters.setSelection(currentChapterIndex);
                        updateUIAndLoadChapter();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải danh sách chương", Toast.LENGTH_SHORT).show());
    }

    // Hàm cập nhật giao diện (Tên chương trên thanh Menu) và load ảnh
    private void updateUIAndLoadChapter() {
        tvReaderChapterTitle.setText(chapterTitleList.get(currentChapterIndex));
        loadChapterPages();
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

                            // Tự động cuộn lên hình ảnh đầu tiên khi chuyển chương
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
}