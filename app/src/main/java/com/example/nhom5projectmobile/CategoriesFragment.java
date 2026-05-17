package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class CategoriesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        RecyclerView rvCategories = view.findViewById(R.id.rvCategories);

        // Danh sách 24 thể loại bạn đã tổng hợp
        List<String> categories = Arrays.asList(
                "Manga", "Manhua", "Manhwa", "Action", "Adventure", "Fantasy",
                "Dark Fantasy", "Science Fiction", "Science Fantasy", "Comedy",
                "Drama", "Mystery", "Supernatural", "Historical",
                "Psychological", "Horror", "Magic", "Martial Arts",
                "Mecha", "Spy Fiction", "Slice of Life", "Family", "Epic", "Shonen"
        );

        CategoryAdapter adapter = new CategoryAdapter(getContext(), categories);

        // Chia làm 3 cột cho gọn gàng (có thể sửa thành 2 nếu bạn thấy chữ bị chật)
        rvCategories.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvCategories.setAdapter(adapter);

        return view;
    }
}