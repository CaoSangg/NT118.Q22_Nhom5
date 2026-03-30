package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvHotStories, rvNewStories;
    private StoryAdapter hotAdapter, newAdapter;
    private List<Story> hotStoryList, newStoryList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ View
        rvHotStories = view.findViewById(R.id.rvHotStories);
        rvNewStories = view.findViewById(R.id.rvNewStories);

        // Grid 3 cột (LayoutManager thực ra đã set trong XML nhưng set lại ở Java cho chắc chắn)
        rvHotStories.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvNewStories.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // 2. Khởi tạo danh sách & Adapter
        hotStoryList = new ArrayList<>();
        newStoryList = new ArrayList<>();

        // Khởi tạo danh sách & Adapter (Cập nhật mới)
        hotStoryList = new ArrayList<>();
        newStoryList = new ArrayList<>();

        // Truyền thêm lambda function (story -> ...) để xử lý click
        hotAdapter = new StoryAdapter(hotStoryList, story -> {
            openStoryDetail(story);
        });

        newAdapter = new StoryAdapter(newStoryList, story -> {
            openStoryDetail(story);
        });

        rvHotStories.setAdapter(hotAdapter);
        rvNewStories.setAdapter(newAdapter);

        // 3. Load dữ liệu
        loadHotStories();
        loadNewStories();

        return view;
    }

    private void loadHotStories() {
        // Sắp xếp theo monthlyViews giảm dần (Truyện nhiều lượt xem nhất)
        db.collection("stories")
                .orderBy("dailyViews", Query.Direction.DESCENDING)
                .limit(6) // Lấy 6 truyện để hiển thị thành 2 hàng cho đẹp
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hotStoryList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        hotStoryList.add(mapDocumentToStory(doc));
                    }
                    hotAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }

    private void loadNewStories() {
        // Sắp xếp theo một trường thời gian để tìm truyện mới cập nhật nhất
        // LƯU Ý: Trong Database Firestore, bạn cần thêm trường "updatedAt" (kiểu timestamp/number) cho từng truyện
        db.collection("stories")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(6)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    newStoryList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        newStoryList.add(mapDocumentToStory(doc));
                    }
                    newAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }
    private void openStoryDetail(Story story) {
        // Kiểm tra an toàn để tránh crash nếu Activity chưa sẵn sàng
        if (getActivity() == null) return;

        // Chuyển sang màn hình StoryDetailActivity
        android.content.Intent intent = new android.content.Intent(getActivity(), StoryDetailActivity.class);

        // Truyền ID và Tiêu đề truyện để màn hình sau dùng query Firestore
        intent.putExtra("STORY_ID", story.getId());
        intent.putExtra("STORY_TITLE", story.getTitle());

        startActivity(intent);
    }

    // Hàm chuyển đổi từ Document Snapshot sang Object Story
    private Story mapDocumentToStory(DocumentSnapshot doc) {
        String title = doc.getString("title");
        String author = doc.getString("author");
        String coverImage = doc.getString("coverImage");

        // 1. Lấy lượt xem động (Ưu tiên viewCount, nếu không có thì lấy dailyViews)
        long views = 0;
        if (doc.contains("viewCount")) {
            views = doc.getLong("viewCount");
        } else if (doc.contains("dailyViews")) {
            views = doc.getLong("dailyViews");
        }

        // 2. Lấy tên Chapter động
        String chapter = doc.contains("lastestChapterTitle") ? doc.getString("lastestChapterTitle") : "Đang cập nhật";

        // 3. Lấy thời gian và tính toán "X ngày trước"
        String timeAgo = "Mới đây";
        if (doc.contains("updatedAt")) {
            com.google.firebase.Timestamp timestamp = doc.getTimestamp("updatedAt");
            if (timestamp != null) {
                timeAgo = getTimeAgo(timestamp.toDate().getTime());
            }
        }

        // BẠN CẦN CẬP NHẬT LẠI MODEL STORY ĐỂ NHẬN THÊM BIẾN timeAgo NHƯ BÊN DƯỚI
        // (ID, Tên truyện, Tác giả, Ảnh bìa, Lượt xem, Tên Chap, Thời gian)
        return new Story(doc.getId(), title, author, coverImage, views, chapter, timeAgo);
    }

    // Hàm thuật toán biến đổi Timestamp thành "X ngày trước"
    private String getTimeAgo(long timeInMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " ngày trước";
        if (hours > 0) return hours + " giờ trước";
        if (minutes > 0) return minutes + " phút trước";
        return "Vừa xong";
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Lỗi tải truyện: " + message, Toast.LENGTH_SHORT).show();
        }
    }
}