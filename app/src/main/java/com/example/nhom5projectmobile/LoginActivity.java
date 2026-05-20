package com.example.nhom5projectmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private ProgressDialog progressDialog;

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle, btnFacebook;
    private TextView tvRegisterLink, tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");

        // Ánh xạ các View từ activity_login.xml
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Cấu hình mạng xã hội Google (Ép chọn tài khoản)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Cấu hình mạng xã hội Facebook (Ép đăng xuất cache cũ)
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleSocialLogin(FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken()), "facebook");
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Hủy đăng nhập Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 1. Đăng nhập bằng Email/Password thường
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Vui lòng nhập đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // KIỂM TRA XEM USER ĐÃ BẤM VÀO LINK TRONG EMAIL CHƯA
                                if (user.isEmailVerified()) {
                                    // Đúng quy trình -> Cập nhật trạng thái emailVerified thành true lên Firestore
                                    db.collection("users").document(user.getUid())
                                            .update("emailVerified", true)
                                            .addOnCompleteListener(uTask -> {
                                                progressDialog.dismiss();
                                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finishAffinity(); // Đóng tất cả các màn hình cũ, ẩn nút đăng nhập hoàn toàn
                                            });
                                } else {
                                    progressDialog.dismiss();
                                    mAuth.signOut(); // Chưa xác thực link mail thì đá văng ra lại
                                    Toast.makeText(LoginActivity.this, "Tài khoản chưa kích hoạt! Hãy kiểm tra hòm thư Email để xác thực.", Toast.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Lỗi đăng nhập: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 2. Click nút Google
        btnGoogle.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });

        // 3. Click nút Facebook
        btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logOut();
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
        });

        // Chuyển hướng sang trang đăng ký
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Chuyển hướng sang trang quên mật khẩu (Sửa lỗi quên mật khẩu bị đứng hình)
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            });
        }

        // Nút tắt góc màn hình close (nếu layout có)
        if (findViewById(R.id.btnClose) != null) {
            findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleSocialLogin(GoogleAuthProvider.getCredential(account.getIdToken(), null), "google");
            } catch (ApiException e) {
                Toast.makeText(this, "Lỗi đăng nhập Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSocialLogin(AuthCredential credential, String provider) {
        progressDialog.setMessage("Đang xác thực liên kết...");
        progressDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Mạng xã hội thì tự động coi như đã verify
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userId", user.getUid());
                            userData.put("email", user.getEmail() != null ? user.getEmail() : "");
                            userData.put("username", user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
                            userData.put("avatarUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                            userData.put("role", "user");
                            userData.put("canManageStories", false);
                            userData.put("authProvider", provider);
                            userData.put("emailVerified", true);
                            userData.put("createdAt", Timestamp.now());

                            // Dùng merge để không làm mất chức năng Admin của nick cũ nếu lỡ ấn đăng nhập lại bằng GG/FB
                            db.collection("users").document(user.getUid())
                                    .set(userData, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finishAffinity();
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Xác thực tài khoản liên kết thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}