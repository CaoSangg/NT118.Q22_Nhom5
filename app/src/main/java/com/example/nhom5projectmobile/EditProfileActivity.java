package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide; // Thư viện để load ảnh
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private TextInputEditText etUsername, etAvatar;
    private ImageView imgPreview; // ImageView để hiển thị ảnh xem trước
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            // Xử lý trường hợp chưa login (nên finish activity)
            finish();
            return;
        }

        // Ánh xạ View (Đảm bảo ID khớp với XML)
        // Lưu ý: Bạn cần thêm ImageView có ID @+id/imgEditPreview vào XML
        imgPreview = findViewById(R.id.imgEditPreview);
        etUsername = findViewById(R.id.etEditUsername);
        etAvatar = findViewById(R.id.etEditAvatarUrl);
        MaterialButton btnSave = findViewById(R.id.btnSaveProfile);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        // 1. Tải dữ liệu hiện tại từ Firestore
        loadCurrentUserData();

        // 2. Thêm TextWatcher để XEM TRƯỚC ẢNH khi dán link
        setupAvatarPreview();

        // 3. Xử lý khi nhấn Lưu
        btnSave.setOnClickListener(v -> {
            saveProfileChanges();
        });

        // 4. Hủy bỏ
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadCurrentUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String currentName = documentSnapshot.getString("username");
                        String currentAvatar = documentSnapshot.getString("avatarUrl");

                        etUsername.setText(currentName);
                        etAvatar.setText(currentAvatar);

                        // Hiển thị ảnh hiện tại lên Preview
                        if (currentAvatar != null && !currentAvatar.isEmpty()) {
                            Glide.with(this).load(currentAvatar).into(imgPreview);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupAvatarPreview() {
        etAvatar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không dùng
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không dùng
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newUrl = s.toString().trim();

                // Kiểm tra sơ bộ link có hợp lệ không (bắt đầu bằng http)
                if (newUrl.startsWith("http://") || newUrl.startsWith("https://")) {
                    // Dùng Glide để load ảnh xem trước ngay lập tức
                    Glide.with(EditProfileActivity.this)
                            .load(newUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery) // Ảnh mặc định khi đang load
                            .error(android.R.drawable.stat_notify_error) // Ảnh báo lỗi khi link hỏng
                            .into(imgPreview);
                } else if (newUrl.isEmpty()) {
                    // Nếu ô trống, hiện ảnh mặc định
                    imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        });
    }

    private void saveProfileChanges() {
        String name = etUsername.getText().toString().trim();
        String avatar = etAvatar.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đóng gói dữ liệu cần cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("avatarUrl", avatar);

        // Ghi đè dữ liệu mới lên Document của User
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại Profile
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}