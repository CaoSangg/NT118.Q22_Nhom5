package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo kênh thông báo & yêu cầu quyền đẩy thông báo
        createNotificationChannel();
        requestNotificationPermission();
        registerFCMToken();

        // Khởi tạo các view
        TextView btnXepHang = findViewById(R.id.btn_xep_hang);
        ImageView btnMenuMore = findViewById(R.id.btnMenuMore);

        // Hiển thị màn hình Home mặc định khi vừa mở app
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        // Khai báo và ánh xạ nút Trang chủ
        TextView btnHome = findViewById(R.id.btn_home);
        // Khai báo và ánh xạ nút Thể Loại
        TextView btnTheLoai = findViewById(R.id.btn_the_loai);

        EditText etSearchMain = findViewById(R.id.etSearchMain);
        ImageView ivSearchIcon = findViewById(R.id.ivSearchIcon);

        // Khai báo một biến để lưu thời gian bấm cuối cùng (để ngay trên dòng Runnable)
        final long[] lastSearchTime = {0};

        Runnable performSearchAction = () -> {
            // Nếu khoảng cách giữa 2 lần bấm nhỏ hơn 1 giây (1000ms) thì bỏ qua lệnh thứ 2
            if (System.currentTimeMillis() - lastSearchTime[0] < 1000) {
                return;
            }
            lastSearchTime[0] = System.currentTimeMillis();

            String keyword = etSearchMain.getText().toString().trim();
            if (!keyword.isEmpty()) {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("SEARCH_KEYWORD", keyword);
                startActivity(intent);
            } else {
                android.widget.Toast.makeText(MainActivity.this, "Vui lòng nhập từ khóa!", android.widget.Toast.LENGTH_SHORT).show();
            }
        };

        // Bắt sk khi nhấn icon tìm kiếm
        ivSearchIcon.setOnClickListener(v -> performSearchAction.run());

        // Bắt sk nhấn enter
        etSearchMain.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN)) {

                performSearchAction.run();
                return true;
            }
            return false;
        });

        // Bắt sự kiện khi nhấn vào nút Trang chủ
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new HomeFragment());
            }
        });

        // Bắt sự kiện khi nhấn vào nút Xếp hạng
        btnXepHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RankingFragment rankingFrag = new RankingFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, rankingFrag)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Bắt sự kiện khi nhấn vào nút Tủ Sách
        findViewById(R.id.btn_truyen_hot).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LibraryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Gắn sự kiện mở PopupMenu
        btnMenuMore.setOnClickListener(v -> showPopupMenu(v));

        // Bắt sự kiện khi nhấn vào nút Thể Loại
        btnTheLoai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở CategoriesFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CategoriesFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }


    // Hàm hiển thị Menu
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.dropdown_menu, popupMenu.getMenu());

        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        android.view.Menu menu = popupMenu.getMenu();

        if (currentUser == null) {
            // CHƯA ĐĂNG NHẬP: Ẩn tất cả ngoại trừ Đăng nhập
            menu.findItem(R.id.nav_profile).setVisible(false);
            menu.findItem(R.id.nav_manage_stories).setVisible(false);
            menu.findItem(R.id.nav_manage_accounts).setVisible(false);
        } else {
            // ĐÃ ĐĂNG NHẬP: Ẩn nút Đăng nhập
            menu.findItem(R.id.nav_login).setVisible(false);

            // Mặc định ẩn 2 nút quản lý, chờ load từ Firestore
            menu.findItem(R.id.nav_manage_stories).setVisible(false);
            menu.findItem(R.id.nav_manage_accounts).setVisible(false);

            // Fetch quyền từ Firestore
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            Boolean canManage = doc.getBoolean("canManageStories");

                            if ("admin".equals(role)) {
                                menu.findItem(R.id.nav_manage_stories).setVisible(true);
                                menu.findItem(R.id.nav_manage_accounts).setVisible(true);
                            } else if (Boolean.TRUE.equals(canManage)) {
                                menu.findItem(R.id.nav_manage_stories).setVisible(true);
                            }
                        }
                    });
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_login) {
                // Mở đúng LoginActivity
                startActivity(new android.content.Intent(MainActivity.this, LoginActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Mở Hồ sơ
                replaceFragmentWithBackStack(new ProfileFragment());
                return true;
            } else if (itemId == R.id.nav_manage_stories) {
                replaceFragmentWithBackStack(new ManageStoriesFragment());
                return true;
            } else if (itemId == R.id.nav_manage_accounts) {
                // Nếu bạn dùng Fragment cho quản lý User thì gọi ở đây
                startActivity(new android.content.Intent(MainActivity.this, UserManagementActivity.class));
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Hàm thay thế Fragment (không lưu lại lịch sử Back) - Dùng cho Home
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    // Hàm thay thế Fragment (CÓ lưu lại lịch sử Back để người dùng bấm nút back quay lại được)
    private void replaceFragmentWithBackStack(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "new_chapter_channel";
            CharSequence name = "Thông báo chương mới";
            String description = "Nhận thông báo khi truyện theo dõi ra chap mới";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void registerFCMToken() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            FirebaseFirestore.getInstance().collection("users").document(userId)
                                    .update("fcmToken", token)
                                    .addOnFailureListener(e -> Log.e("MainActivity", "Lỗi lưu FCM Token: " + e.getMessage()));
                        }
                    });
        }
    }

}