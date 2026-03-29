package com.example.nhom5projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Nút đóng màn hình
        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // Xử lý Đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công, tiến hành cập nhật fcmToken mới cho thiết bị này
                            updateFCMToken();
                        } else {
                            Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Chuyển sang trang Đăng ký
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Chuyển sang trang Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void updateFCMToken() {
        String uid = mAuth.getCurrentUser().getUid();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();

                        // Cập nhật fcmToken vào document của user trong collection "users"
                        db.collection("users").document(uid)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid -> {
                                    // Sau khi cập nhật xong thì vào màn hình chính
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finishAffinity(); // Đóng tất cả các màn hình trước đó
                                })
                                .addOnFailureListener(e -> {
                                    // Ngay cả khi lỗi cập nhật Token vẫn cho vào app, nhưng báo lỗi
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finishAffinity();
                                });
                    }
                });
    }
}