package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        TextView btnXepHang = findViewById(R.id.btn_xep_hang);
        // 1. Hiển thị màn hình Home mặc định khi vừa mở app
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        // 2. Xử lý sự kiện khi bấm vào các icon trên BottomNavigationView
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                // Tạm thời vẫn mở HomeFragment nếu bạn chưa tạo SearchFragment
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_library) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
                return true;
            }

            return false;
        });
        btnXepHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khởi tạo Fragment Xếp hạng
                RankingFragment rankingFrag = new RankingFragment();

                // Lệnh chuyển Fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, rankingFrag) // Thay thế vùng chứa bằng RankingFragment
                        .addToBackStack(null) // Nhấn nút Back trên điện thoại sẽ quay lại trang chủ
                        .commit();
            }
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
        findViewById(R.id.btn_truyen_hot).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HotStoriesFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // Hàm dùng để thay đổi màn hình (Fragment)
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}