package com.example.nhom5projectmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private ProgressDialog progressDialog;

    private TextInputEditText etUsername, etEmail, etPassword;
    private MaterialButton btnRegister, btnGoogleRegister, btnFacebookRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý tạo tài khoản...");

        // Ánh xạ View đúng theo giao diện XML mới lược bỏ số điện thoại
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister);
        btnFacebookRegister = findViewById(R.id.btnFacebookRegister);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (tvLoginLink != null) tvLoginLink.setOnClickListener(v -> finish());

        // Thiết lập cấu hình nút mạng xã hội cho trang Đăng Ký
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleSocialRegister(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()), "facebook");
            }
            @Override
            public void onCancel() { Toast.makeText(RegisterActivity.this, "Hủy đăng ký Facebook", Toast.LENGTH_SHORT).show(); }
            @Override
            public void onError(FacebookException error) { Toast.makeText(RegisterActivity.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show(); }
        });

        // 1. Đăng ký bằng Email thông thường (Lưu liền vào Firestore với trạng thái false)
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Vui lòng nhập toàn bộ dữ liệu mẫu", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Gán tên hiển thị vào hệ thống Auth profile
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).build();
                                user.updateProfile(profileUpdates);

                                // LƯU LUÔN VÀO FIRESTORE (Thỏa mãn yêu cầu của bạn)
                                saveUserToFirestore(user, username, "email", false, () -> {
                                    // Lưu xong mới kích hoạt gửi mail xác thực
                                    user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(this, "Đăng ký thành công! Hãy vào mail xác nhận link kích hoạt để đăng nhập.", Toast.LENGTH_LONG).show();
                                        mAuth.signOut(); // Đăng xuất ngay để đưa họ về trang đăng nhập test
                                        finish();
                                    });
                                });
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Lỗi tạo tài khoản: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 2. Đăng ký bằng Google tại trang Register
        btnGoogleRegister.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });

        // 3. Đăng ký bằng Facebook tại trang Register
        btnFacebookRegister.setOnClickListener(v -> {
            LoginManager.getInstance().logOut();
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleSocialRegister(GoogleAuthProvider.getCredential(account.getIdToken(), null), "google");
            } catch (ApiException e) {
                Toast.makeText(this, "Lỗi liên kết Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSocialRegister(AuthCredential credential, String provider) {
        progressDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, user.getDisplayName(), provider, true, () -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Đăng ký tài khoản mạng xã hội thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finishAffinity();
                            });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Lỗi đăng ký qua mạng xã hội", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Interface callback hỗ trợ luồng đồng bộ tuần tự
    interface OnSaveSuccessListener { void onSuccess(); }

    private void saveUserToFirestore(FirebaseUser user, String username, String provider, boolean isVerified, OnSaveSuccessListener listener) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUid());
        userData.put("email", user.getEmail() != null ? user.getEmail() : "");
        userData.put("username", username != null ? username : "Người dùng");
        userData.put("avatarUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("role", "user");
        userData.put("canManageStories", false);
        userData.put("authProvider", provider);
        userData.put("emailVerified", isVerified);
        userData.put("createdAt", Timestamp.now());

        db.collection("users").document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Lỗi kết nối database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}