package com.example.nhom5projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvUsername, tvEmail, tvGuestStatus;
    private MaterialButton btnLogin, btnLogout, btnEditProfile;
    private ImageView imgAvatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvGuestStatus = view.findViewById(R.id.tvGuestStatus);
        btnLogin = view.findViewById(R.id.btnGoToRegister);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        btnLogout.setOnClickListener(v -> { mAuth.signOut(); updateUI(); });

        // Mở màn hình chỉnh sửa
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(); // Cập nhật lại khi từ màn hình Edit quay về
    }

    private void updateUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvGuestStatus.setVisibility(View.GONE);
            btnLogin.setVisibility(View.GONE);
            tvUsername.setVisibility(View.VISIBLE);
            tvEmail.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnEditProfile.setVisibility(View.VISIBLE);

            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    tvUsername.setText(doc.getString("username"));
                    tvEmail.setText(doc.getString("email"));
                    String url = doc.getString("avatarUrl");
                    if (url != null && !url.isEmpty()) Glide.with(this).load(url).into(imgAvatar);
                }
            });
        } else {
            tvGuestStatus.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            tvUsername.setVisibility(View.GONE);
            tvEmail.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnEditProfile.setVisibility(View.GONE);
            imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }
}