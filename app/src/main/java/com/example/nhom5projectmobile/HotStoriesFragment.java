package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HotStoriesFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hot_stories, container, false);

        RecyclerView rv = view.findViewById(R.id.rvHotGrid);

        // Thiết lập Grid 3 cột
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Tạo dữ liệu test (Bạn có thể thêm nhiều hơn để thấy cuộn trang)
        List<StoryHot> data = new ArrayList<>();
        data.add(new StoryHot("Phá thân của nữ Hoàng Đế", "88", R.drawable.tag_hot));
        data.add(new StoryHot("Truyện Võ", "504", R.drawable.tag_update));
        data.add(new StoryHot("Bà xã tôi đến từ ngàn năm trước", "456", R.drawable.tag_hot));
        data.add(new StoryHot("Đăng Nhập 30 ngày, một quyền nổ tung tinh tú", "167", R.drawable.tag_update));
        data.add(new StoryHot("Vợ ơi, xin hãy ngoan ngoãn nhé", "Thông báo", R.drawable.tag_hot));
        data.add(new StoryHot("Ta Được Nuôi Dưỡng Bởi Nữ...", "129", R.drawable.tag_update));
        data.add(new StoryHot("Đại Quản Gia Là Ma Hoàng", "400", R.drawable.tag_hot));
        data.add(new StoryHot("Võ Luyện Đỉnh Phong", "3000", R.drawable.tag_update));
        data.add(new StoryHot("Ta Có 90 Tỷ Tiền Liếm Cẩu", "200", R.drawable.tag_hot));

        // Set Adapter
        HotAdapter adapter = new HotAdapter(data);
        rv.setAdapter(adapter);

        return view;
    }
}
