package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RankingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        RecyclerView rv = view.findViewById(R.id.rvRanking);
        List<StoryRanking> data = new ArrayList<>();

        // Thêm vài cuốn truyện giả định
        data.add(new StoryRanking("Đại Quản Gia Là Ma Hoàng", "Lượt xem: 582K", 1, R.drawable.tag_hot));
        data.add(new StoryRanking("Võ Luyện Đỉnh Phong", "Lượt xem: 551K", 2, R.drawable.tag_update));
        data.add(new StoryRanking("Ta Có 90 Tỷ Tiền Liếm Cẩu", "Lượt xem: 286K", 3, R.drawable.tag_hot));
        data.add(new StoryRanking("Toàn Chức Pháp Sư", "Lượt xem: 249K", 4, R.drawable.tag_update));

        RankingAdapter adapter = new RankingAdapter(data);
        rv.setAdapter(adapter);

        return view;
    }
}