package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging; // Cần thiết cho fcmToken
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View (Đã thêm etUsername)
        TextInputEditText etUsername = findViewById(R.id.etUsername);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etPhone = findViewById(R.id.etPhone);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        // Xử lý đăng ký
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Tạo tài khoản trên Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            // 2. Lấy fcmToken cho thiết bị này
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        String fcmToken = tokenTask.isSuccessful() ? tokenTask.getResult() : "";

                                        // 3. Đóng gói dữ liệu theo database_guide.md
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("userId", userId);
                                        user.put("username", username);
                                        user.put("email", email);
                                        user.put("phoneNumber", phone);
                                        user.put("avatarUrl", "");
                                        user.put("fcmToken", fcmToken);
                                        user.put("createdAt", com.google.firebase.Timestamp.now());

                                        // Khởi tạo trạng thái liên kết tài khoản
                                        Map<String, Boolean> linkedAccounts = new HashMap<>();
                                        linkedAccounts.put("google", false);
                                        linkedAccounts.put("facebook", false);
                                        user.put("linkedAccounts", linkedAccounts);

                                        // 4. Lưu vào Firestore collection "users"
                                        db.collection("users").document(userId).set(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvLoginLink.setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}