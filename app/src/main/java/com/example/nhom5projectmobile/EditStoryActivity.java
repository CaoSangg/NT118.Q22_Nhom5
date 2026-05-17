package com.example.nhom5projectmobile;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditStoryActivity extends AppCompatActivity {

    private EditText etEditTitle, etEditAuthor, etEditCategory, etEditKeywords, etEditDescription;
    private Spinner spinnerEditStatus;
    private Button btnSubmitEdit;
    private String storyId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_story);

        // Ánh xạ View
        etEditTitle = findViewById(R.id.etEditTitle);
        etEditAuthor = findViewById(R.id.etEditAuthor);
        etEditCategory = findViewById(R.id.etEditCategory);
        etEditKeywords = findViewById(R.id.etEditKeywords);
        etEditDescription = findViewById(R.id.etEditDescription);
        spinnerEditStatus = findViewById(R.id.spinnerEditStatus);
        btnSubmitEdit = findViewById(R.id.btnSubmitEdit);

        // Setup Spinner
        String[] statuses = {"Đang ra", "Hoàn thành", "Tạm ngưng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerEditStatus.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Nhận ID truyện cần sửa
        storyId = getIntent().getStringExtra("STORY_ID");
        if (storyId != null) {
            loadExistingData();
        }

        // Bắt sự kiện cập nhật
        btnSubmitEdit.setOnClickListener(v -> updateStoryToFirestore());
    }

    private void loadExistingData() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang tải dữ liệu truyện...");
        pd.show();

        db.collection("stories").document(storyId).get().addOnSuccessListener(doc -> {
            pd.dismiss();
            if (doc.exists()) {
                etEditTitle.setText(doc.getString("title"));
                etEditAuthor.setText(doc.getString("author"));
                etEditDescription.setText(doc.getString("description"));

                // Chuyển mảng Thể loại thành chuỗi hiển thị
                List<String> cats = (List<String>) doc.get("category");
                if (cats != null) etEditCategory.setText(android.text.TextUtils.join(", ", cats));

                // Chuyển mảng Từ khóa thành chuỗi hiển thị
                List<String> keys = (List<String>) doc.get("keywords");
                if (keys != null) etEditKeywords.setText(android.text.TextUtils.join(", ", keys));

                // Cài đặt trạng thái Spinner
                String status = doc.getString("status");
                if ("Hoàn thành".equals(status)) spinnerEditStatus.setSelection(1);
                else if ("Tạm ngưng".equals(status)) spinnerEditStatus.setSelection(2);
                else spinnerEditStatus.setSelection(0);
            }
        }).addOnFailureListener(e -> {
            pd.dismiss();
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateStoryToFirestore() {
        String title = etEditTitle.getText().toString().trim();
        String author = etEditAuthor.getText().toString().trim();
        String desc = etEditDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Tên truyện không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chẻ lại chuỗi thành mảng MỚI
        String catRaw = etEditCategory.getText().toString().trim();
        List<String> categories = catRaw.isEmpty() ? Arrays.asList("Chưa phân loại") : Arrays.asList(catRaw.split("\\s*,\\s*"));

        String keyRaw = etEditKeywords.getText().toString().trim();
        List<String> keywords = keyRaw.isEmpty() ? Arrays.asList(title.toLowerCase()) : Arrays.asList(keyRaw.split("\\s*,\\s*"));

        String status = spinnerEditStatus.getSelectedItem().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("title_lowercase", title.toLowerCase()); // Rất quan trọng để Search không bị lỗi
        updates.put("author", author);
        updates.put("description", desc);
        updates.put("category", categories);
        updates.put("keywords", keywords);
        updates.put("status", status);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Đang cập nhật...");
        pd.show();

        db.collection("stories").document(storyId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Trở về bảng quản lý
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}