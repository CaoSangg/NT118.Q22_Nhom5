package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RankingFragment extends Fragment {

    private RecyclerView rv;
    private RankingAdapter adapter;
    private List<StoryRanking> data;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        rv = view.findViewById(R.id.rvRanking);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        data = new ArrayList<>();
        adapter = new RankingAdapter(data);
        rv.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 1. Mặc định load "Top Tuần" (Tab đầu tiên) khi vừa mở màn hình
        loadRankingData("WEEK");

        // 2. Bắt sự kiện chuyển Tab
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutRanking);

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0: // Bấm vào Top Tuần
                            loadRankingData("WEEK");
                            break;
                        case 1: // Bấm vào Top Tháng
                            loadRankingData("MONTH");
                            break;
                        case 2: // Bấm vào Top All
                            loadRankingData("ALL");
                            break;
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        return view;
    }

    private void loadRankingData(String timeFrame) {
        // Mặc định sắp xếp theo tổng lượt xem (Top All)
        String orderByField = "viewsCount";

        // Nếu là Tuần hoặc Tháng thì đổi trường sắp xếp tương ứng
        if (timeFrame.equals("WEEK")) {
            orderByField = "viewsWeek";
        } else if (timeFrame.equals("MONTH")) {
            orderByField = "viewsMonth";
        }

        // Truy vấn Firebase
        db.collection("stories")
                .orderBy(orderByField, Query.Direction.DESCENDING)
                .limit(10) // Chỉ lấy top 10
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        data.clear();
                        int currentRank = 1;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            StoryRanking story = document.toObject(StoryRanking.class);
                            story.setStoryId(document.getId());
                            story.setRank(currentRank++);

                            // Set lượt xem hiển thị chính xác theo Tab đang chọn
                            if (timeFrame.equals("WEEK")) {
                                story.setCurrentDisplayViews(story.getViewsWeek());
                            } else if (timeFrame.equals("MONTH")) {
                                story.setCurrentDisplayViews(story.getViewsMonth());
                            } else {
                                story.setCurrentDisplayViews(story.getViewsCount());
                            }

                            data.add(story);
                        }

                        adapter.updateData(data); // Đổ dữ liệu mới vào Adapter
                    } else {
                        Log.e("RankingFragment", "Lỗi lấy dữ liệu: ", task.getException());
                    }
                });
    }
}