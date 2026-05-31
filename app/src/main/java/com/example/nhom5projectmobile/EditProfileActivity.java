package com.example.nhom5projectmobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.app.ProgressDialog;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    private ImageView imgPreview;

    private TextInputEditText etUsername;

    private Uri imageUri;

    private FirebaseFirestore db;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();

        userId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        imgPreview = findViewById(R.id.imgEditPreview);

        etUsername = findViewById(R.id.etEditUsername);

        MaterialButton btnChoose =
                findViewById(R.id.btnChooseImage);

        MaterialButton btnSave =
                findViewById(R.id.btnSaveProfile);

        ImageButton btnBack =
                findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnChoose.setOnClickListener(v -> {

            Intent intent = new Intent();

            intent.setType("image/*");

            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> saveProfile());

        loadUser();
    }

    private void loadUser() {

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {

                    etUsername.setText(
                            doc.getString("username")
                    );

                    String avatar =
                            doc.getString("avatarUrl");

                    if (avatar != null) {

                        Glide.with(this)
                                .load(avatar)
                                .into(imgPreview);
                    }
                });
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_IMAGE &&
                resultCode == Activity.RESULT_OK &&
                data != null &&
                data.getData() != null) {

            imageUri = data.getData();

            imgPreview.setImageURI(imageUri);
        }
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Tên người dùng không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang lưu thông tin...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Map<String, Object> map = new HashMap<>();
        map.put("username", username);

        if (imageUri != null) {
            progressDialog.setMessage("Đang tải ảnh đại diện lên...");
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("avatars/" + userId + ".jpg");

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        map.put("avatarUrl", uri.toString());
                        updateFirestoreProfile(map, progressDialog);
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Lỗi tải ảnh đại diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateFirestoreProfile(map, progressDialog);
        }
    }

    private void updateFirestoreProfile(Map<String, Object> map, ProgressDialog progressDialog) {
        db.collection("users")
                .document(userId)
                .update(map)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi cập nhật Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}