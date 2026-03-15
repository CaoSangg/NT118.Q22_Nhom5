package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Kết nối file Java này với file fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- PHẦN XỬ LÝ MENU XỔ XUỐNG ---
        // 1. Ánh xạ nút menu (id này phải trùng với id trong file XML tôi vừa sửa cho bạn)
        ImageView btnMenuMore = view.findViewById(R.id.btnMenuMore);

        // 2. Thiết lập sự kiện bấm
        btnMenuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });
        // -------------------------------

        // Ánh xạ RecyclerView từ XML
        RecyclerView recyclerView = view.findViewById(R.id.rvHomeStories);

        // 1. Tạo danh sách dữ liệu giả để hiển thị
        List<Story> list = new ArrayList<>();
        list.add(new Story("Võ Luyện Đỉnh Phong", "Chương 4856", "238", R.drawable.ic_launcher_background));
        list.add(new Story("Đường Môn Máu", "Chương 153", "364", R.drawable.ic_launcher_background));
        list.add(new Story("Kingdom", "Chương 860", "340", R.drawable.ic_launcher_background));
        list.add(new Story("Thanh Gươm Diệt Quỷ", "Chương 205", "308", R.drawable.ic_launcher_background));
        list.add(new Story("The Fragrant Flower", "Chương 180", "285", R.drawable.ic_launcher_background));
        list.add(new Story("Bá Vương Sủng Ái", "Chương 183", "394", R.drawable.ic_launcher_background));

        // 2. Thiết lập Adapter để đổ dữ liệu vào ô truyện
        StoryAdapter adapter = new StoryAdapter(list);

        // 3. Cấu hình hiển thị 3 cột (Grid)
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Hàm hiển thị PopupMenu
    private void showPopupMenu(View view) {
        // Khởi tạo PopupMenu
        PopupMenu popupMenu = new PopupMenu(getContext(), view);

        // Nạp file menu của bạn vào (Hãy kiểm tra tên file menu của bạn là gì, ví dụ: R.menu.main_menu)
        popupMenu.getMenuInflater().inflate(R.menu.bottom_nav_menu, popupMenu.getMenu());

        // Xử lý sự kiện click vào từng item trong menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Toast.makeText(getContext(), "Trang chủ", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_search) {
                    Toast.makeText(getContext(), "Tìm kiếm", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_library) {
                    Toast.makeText(getContext(), "Thư viện", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(getContext(), "Profile", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }
}