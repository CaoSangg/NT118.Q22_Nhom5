package com.example.nhom5projectmobile;

import android.os.Bundle;

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
                replaceFragment(new HomeFragment());
                return true;
            }

            return false;
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