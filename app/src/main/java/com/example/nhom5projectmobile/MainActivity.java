package com.example.nhom5projectmobile;

import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Bắt sự kiện khi nhấn vào nút Truyện Hot
        findViewById(R.id.btn_truyen_hot).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HotStoriesFragment())
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
        // Khởi tạo PopupMenu gắn với View (nút btnMenuMore)
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.dropdown_menu, popupMenu.getMenu());

        boolean isAdmin = true;

        if (!isAdmin) {
            popupMenu.getMenu().findItem(R.id.nav_manage_stories).setVisible(false);
            popupMenu.getMenu().findItem(R.id.nav_manage_accounts).setVisible(false);
        }

        // Lắng nghe sự kiện ở các mục tương ứng trong Menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();

                    // Chuyển sang màn hình Đăng nhập
                if (itemId == R.id.nav_login) {
                    replaceFragmentWithBackStack(new ProfileFragment());
                    return true;

                    // Chuyển sang màn Profile
                } else if (itemId == R.id.nav_profile) {
                    return true;

                    // Chuyển sang màn hình Tủ truyện
                } else if (itemId == R.id.nav_library) {
                    return true;

                    // Chuyển sang màn hình Cài đặt
                } else if (itemId == R.id.nav_settings) {
                    return true;

                    // Chuyển sang màn hình Quản lý truyện
                } else if (itemId == R.id.nav_manage_stories) {
                    replaceFragmentWithBackStack(new ManageStoriesFragment());
                    return true;
                }

                return false;
            }
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

}