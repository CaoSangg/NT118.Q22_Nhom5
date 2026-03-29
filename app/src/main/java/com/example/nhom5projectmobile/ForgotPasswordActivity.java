package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private MaterialButton btnReset;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ View
        etEmail = findViewById(R.id.etEmail);
        btnReset = findViewById(R.id.btnResetPassword);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Quay lại trang Đăng nhập
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút gửi yêu cầu khôi phục
        btnReset.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gửi email khôi phục mật khẩu thông qua Firebase
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Link khôi phục đã được gửi! Kiểm tra email của bạn.",
                                    Toast.LENGTH_LONG).show();
                            finish(); // Đóng màn hình sau khi gửi thành công
                        } else {
                            // Hiển thị lỗi nếu email không tồn tại hoặc lỗi mạng
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Lỗi: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}