package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Bấm để quay lại trang Đăng nhập

        findViewById(R.id.btnResetPassword).setOnClickListener(v -> {
            // Sau này bạn sẽ viết code Firebase gửi mail ở đây
            Toast.makeText(this, "Yêu cầu đã được gửi! Kiểm tra email của bạn.", Toast.LENGTH_SHORT).show();
        });
    }
}