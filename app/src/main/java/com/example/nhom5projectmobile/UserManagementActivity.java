package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagementActivity
        extends AppCompatActivity {

    private RecyclerView recyclerView;

    private UserAdapter adapter;

    private List<UserModel> list;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_user_management
        );

        db = FirebaseFirestore.getInstance();

        recyclerView =
                findViewById(R.id.recyclerUsers);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        list = new ArrayList<>();

        adapter = new UserAdapter(
                this,
                list,
                this::toggleManager,
                this::deleteUser
        );

        recyclerView.setAdapter(adapter);

        loadUsers();

        findViewById(R.id.btnBack)
                .setOnClickListener(v -> finish());
    }

    private void loadUsers() {

        db.collection("users")
                .get()
                .addOnSuccessListener(query -> {

                    list.clear();

                    list.addAll(
                            query.toObjects(UserModel.class)
                    );

                    adapter.notifyDataSetChanged();
                });
    }

    private void toggleManager(UserModel user) {

        boolean newValue =
                !Boolean.TRUE.equals(
                        user.getCanManageStories()
                );

        Map<String, Object> update =
                new HashMap<>();

        update.put("canManageStories",
                newValue);

        db.collection("users")
                .document(user.getUserId())
                .update(update)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Đã cập nhật quyền",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadUsers();
                });
    }

    private void deleteUser(UserModel user) {

        db.collection("users")
                .document(user.getUserId())
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Đã xóa user",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadUsers();
                });
    }
}