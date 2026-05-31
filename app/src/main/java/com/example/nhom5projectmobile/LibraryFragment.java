package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView rv;
    private StoryAdapter adapter;
    private List<Story> storyList;
    private FirebaseFirestore db;
    private GridLayoutManager gridLayoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false); // Ánh xạ layout fragment_library mới

        rv = view.findViewById(R.id.rvHotGrid);

        // Mặc định tab đầu tiên là Theo dõi nên để 3 cột
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rv.setLayoutManager(gridLayoutManager);

        db = FirebaseFirestore.getInstance();
        storyList = new ArrayList<>();

        adapter = new StoryAdapter(storyList, story -> {
            openStoryDetail(story);
        });
        rv.setAdapter(adapter);

        // 1. Mặc định tải Tab "Theo dõi" khi vừa mở lên
        loadFollowedStories();

        // 2. Bắt sự kiện chuyển Tab
        TabLayout tabLayout = view.findViewById(R.id.tabLayoutSubHot);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switch (tab.getPosition()) {
                        case 0: // Bấm vào "Theo dõi"
                            gridLayoutManager.setSpanCount(3); // Trả về 3 cột
                            loadFollowedStories();
                            break;
                        case 1: // Bấm vào "Vừa đọc"
                            gridLayoutManager.setSpanCount(3);
                            loadHistoryStories();
                            break;
                        case 2: // Bấm vào "Đã tải"
                            gridLayoutManager.setSpanCount(3);
                            loadDownloadedStories(); // Gọi hàm tải dữ liệu từ SQLite
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

    // =========================================================
    // HÀM XỬ LÝ TAB "THEO DÕI"
    // =========================================================
    private void loadFollowedStories() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        db.collection("stories")
                .whereArrayContains("followers", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    storyList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        storyList.add(mapDocumentToStory(doc));
                    }
                    adapter.notifyDataSetChanged();

                    if (storyList.isEmpty() && getContext() != null) {
                        Toast.makeText(getContext(), "Bạn chưa theo dõi truyện nào", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi tải dữ liệu: " + e.getMessage()));
    }

    // =========================================================
    // HÀM XỬ LÝ TAB "VỪA ĐỌC" (LỊCH SỬ)
    // =========================================================
    private void loadHistoryStories() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showToast("Vui lòng đăng nhập để xem lịch sử!");
            return;
        }

        String currentUserId = currentUser.getUid();

        db.collection("users").document(currentUserId)
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        storyList.clear();
                        adapter.notifyDataSetChanged();
                        showToast("Bạn chưa đọc truyện nào");
                        return;
                    }

                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String storyId = doc.getId();
                        tasks.add(db.collection("stories").document(storyId).get());
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(list -> {
                        storyList.clear();
                        for (Object obj : list) {
                            DocumentSnapshot storyDoc = (DocumentSnapshot) obj;
                            if (storyDoc.exists()) {
                                storyList.add(mapDocumentToStory(storyDoc));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });
                })
                .addOnFailureListener(e -> showToast("Lỗi tải lịch sử: " + e.getMessage()));
    }

    // =========================================================
    // CÁC HÀM PHỤ TRỢ MẶC ĐỊNH
    // =========================================================
    private void openStoryDetail(Story story) {
        if (getActivity() == null) return;
        android.content.Intent intent = new android.content.Intent(getActivity(), StoryDetailActivity.class);
        intent.putExtra("STORY_ID", story.getId());
        intent.putExtra("STORY_TITLE", story.getTitle());
        TabLayout tabLayout = getView().findViewById(R.id.tabLayoutSubHot);
        if (tabLayout != null && tabLayout.getSelectedTabPosition() == 2) {
            intent.putExtra("IS_OFFLINE", true);
            intent.putExtra("STORY_COVER", story.getCoverImage());
        }
        startActivity(intent);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private Story mapDocumentToStory(DocumentSnapshot doc) {
        String title = doc.getString("title");
        String author = doc.getString("author");
        String coverImage = doc.getString("coverImage");

        long views = 0;
        if (doc.contains("viewsCount")) {
            views = doc.getLong("viewsCount");
        } else if (doc.contains("viewCount")) {
            views = doc.getLong("viewCount");
        } else if (doc.contains("dailyViews")) {
            views = doc.getLong("dailyViews");
        }
        String status = doc.contains("status") ? doc.getString("status") : "Đang cập nhật";
        long chaptersCount = doc.contains("chaptersCount") ? doc.getLong("chaptersCount") : 0;
        String chapter = chaptersCount > 0 ? status + "\nChương " + chaptersCount : status;

        String timeAgo = "Mới đây";
        if (doc.contains("updatedAt")) {
            com.google.firebase.Timestamp timestamp = doc.getTimestamp("updatedAt");
            if (timestamp != null) {
                timeAgo = getTimeAgo(timestamp.toDate().getTime());
            }
        }

        return new Story(doc.getId(), title, author, coverImage, views, chapter, timeAgo, null);
    }

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

    private void loadDownloadedStories() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        storyList.clear();
        storyList.addAll(dbHelper.getAllOfflineStories()); // Lấy từ SQLite ra
        adapter.notifyDataSetChanged();

        if (storyList.isEmpty() && getContext() != null) {
            Toast.makeText(getContext(), "Không có truyện nào được tải offline", Toast.LENGTH_SHORT).show();
        }
    }
}
