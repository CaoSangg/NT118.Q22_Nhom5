package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        findViewById(R.id.btnClose).setOnClickListener(v -> {
            finish(); // Lệnh này sẽ đóng màn hình hiện tại và quay về màn hình trước đó
        });

        // 1. Cấu hình hệ thống (Padding cho thanh trạng thái/điều hướng)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. Ánh xạ các View
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 3. Xử lý sự kiện click
        // Chuyển sang trang Đăng ký
        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Chuyển sang trang Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}