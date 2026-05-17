package com.example.nhom5projectmobile;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStoryActivity extends AppCompatActivity {

    private ImageView imgAddCover;
    private EditText etAddId, etAddTitle, etAddAuthor, etAddCategory, etAddKeywords, etAddDescription;
    private Spinner spinnerAddStatus;
    private Button btnPickCover, btnSubmitStory;

    private Uri selectedCoverUri = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        // Đổi tên Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm truyện mới");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 1. Ánh xạ View
        imgAddCover = findViewById(R.id.imgAddCover);
        etAddId = findViewById(R.id.etAddId);
        etAddTitle = findViewById(R.id.etAddTitle);
        etAddAuthor = findViewById(R.id.etAddAuthor);
        etAddCategory = findViewById(R.id.etAddCategory);
        etAddKeywords = findViewById(R.id.etAddKeywords);
        etAddDescription = findViewById(R.id.etAddDescription);
        spinnerAddStatus = findViewById(R.id.spinnerAddStatus);
        btnPickCover = findViewById(R.id.btnPickCover);
        btnSubmitStory = findViewById(R.id.btnSubmitStory);

        // 2. Setup Spinner cho Trạng thái
        String[] statuses = {"Đang ra", "Hoàn thành", "Tạm ngưng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerAddStatus.setAdapter(adapter);

        // 3. Setup Trình chọn ảnh
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedCoverUri = uri;
                imgAddCover.setImageURI(uri); // Hiển thị ảnh xem trước
            }
        });

        btnPickCover.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 4. Bắt sự kiện tạo truyện
        btnSubmitStory.setOnClickListener(v -> handleSubmittingStory());
    }

    private void handleSubmittingStory() {
        String id = etAddId.getText().toString().trim();
        String title = etAddTitle.getText().toString().trim();

        if (id.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, "Mã truyện và Tên truyện không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCoverUri != null) {
            uploadCoverAndSave(id, title, selectedCoverUri);
        } else {
            // Nếu không chọn ảnh, dùng ảnh mặc định
            saveToFirestore(id, title, "https://via.placeholder.com/150");
        }
    }

    private void uploadCoverAndSave(String id, String title, Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải ảnh bìa...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("story_covers/" + id + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressDialog.dismiss();
                    saveToFirestore(id, title, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi tải ảnh bìa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToFirestore(String id, String title, String coverUrl) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu dữ liệu truyện...");
        progressDialog.show();

        // Xử lý tách chuỗi thành mảng
        String author = etAddAuthor.getText().toString().trim();
        String desc = etAddDescription.getText().toString().trim();

        String categoryRaw = etAddCategory.getText().toString().trim();
        List<String> categories = categoryRaw.isEmpty() ? Arrays.asList("Chưa phân loại") : Arrays.asList(categoryRaw.split("\\s*,\\s*"));

        String keywordsRaw = etAddKeywords.getText().toString().trim();
        List<String> keywords = keywordsRaw.isEmpty() ? Arrays.asList(title.toLowerCase()) : Arrays.asList(keywordsRaw.split("\\s*,\\s*"));

        String status = spinnerAddStatus.getSelectedItem().toString();

        Map<String, Object> storyData = new HashMap<>();
        storyData.put("storyId", id);
        storyData.put("title", title);
        storyData.put("title_lowercase", title.toLowerCase());
        storyData.put("author", author.isEmpty() ? "Đang cập nhật" : author);
        storyData.put("description", desc.isEmpty() ? "Chưa có mô tả" : desc);
        storyData.put("coverImage", coverUrl);
        storyData.put("status", status);
        storyData.put("viewsCount", 0);
        storyData.put("dailyViews", 0);
        storyData.put("chaptersCount", 0);
        storyData.put("lastestChapterTitle", "Chưa có chương");
        storyData.put("category", categories);
        storyData.put("keywords", keywords);
        storyData.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("stories").document(id)
                .set(storyData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Thêm truyện mới thành công!", Toast.LENGTH_LONG).show();
                    finish(); // Tự động đóng màn hình sau khi lưu xong
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}