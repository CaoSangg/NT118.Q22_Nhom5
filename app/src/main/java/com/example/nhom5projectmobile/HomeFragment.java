package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
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

    // Các thành phần phục vụ phân trang mục Truyện Mới
    private NestedScrollView homeScrollView;
    private LinearLayout layoutHotStoriesSection;
    private LinearLayout layoutPageNumbers;
    private Button btnPrevPage, btnNextPage;
    private List<Story> allNewStories = new ArrayList<>();
    private int currentPage = 1;
    private final int PAGE_SIZE = 9; // Hiển thị 9 truyện mỗi trang (3x3 grid)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ View
        homeScrollView = view.findViewById(R.id.homeScrollView);
        layoutHotStoriesSection = view.findViewById(R.id.layoutHotStoriesSection);
        rvNewStories = view.findViewById(R.id.rvNewStories); // Danh sách truyện mới ở dưới (có phân trang)
        rvHotStories = view.findViewById(R.id.rvListStories); // Danh sách truyện hot ở trên (6 truyện tiêu biểu)
        layoutPageNumbers = view.findViewById(R.id.layoutPageNumbers);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);

        // Đặt layout grid 3 cột
        rvHotStories.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rvNewStories.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // 2. Khởi tạo danh sách
        hotStoryList = new ArrayList<>();
        newStoryList = new ArrayList<>();

        // Khởi tạo Adapter
        hotAdapter = new StoryAdapter(hotStoryList, story -> {
            openStoryDetail(story);
        });

        newAdapter = new StoryAdapter(newStoryList, story -> {
            openStoryDetail(story);
        });
        newAdapter.setTagText("NEW");

        rvHotStories.setAdapter(hotAdapter);
        rvNewStories.setAdapter(newAdapter);

        // Thiết lập sự kiện click cho các nút phân trang chính (< và >)
        if (btnPrevPage != null) {
            btnPrevPage.setOnClickListener(v -> {
                if (currentPage > 1) {
                    currentPage--;
                    displayNewStoriesPage(currentPage);
                }
            });
        }

        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> {
                int totalPages = (int) Math.ceil((double) allNewStories.size() / PAGE_SIZE);
                if (currentPage < totalPages) {
                    currentPage++;
                    displayNewStoriesPage(currentPage);
                }
            });
        }

        // 3. Load dữ liệu
        loadHotStories();
        loadNewStories();

        return view;
    }

    private void loadHotStories() {
        // Chỉ lấy 6 truyện hot nhất (theo lượt xem ngày) hiển thị ở đầu trang
        db.collection("stories")
                .orderBy("dailyViews", Query.Direction.DESCENDING)
                .limit(6)
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
        // Tải toàn bộ truyện mới để phân trang cục bộ
        db.collection("stories")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(120) // Giới hạn tối đa 120 truyện mới để hiển thị phân trang gọn nhẹ
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allNewStories.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        allNewStories.add(mapDocumentToStory(doc));
                    }
                    currentPage = 1;
                    displayNewStoriesPage(currentPage);
                })
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }

    private void displayNewStoriesPage(int page) {
        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, allNewStories.size());

        newStoryList.clear();
        if (startIndex < allNewStories.size()) {
            newStoryList.addAll(allNewStories.subList(startIndex, endIndex));
        }
        newAdapter.notifyDataSetChanged();

        // Ẩn phần truyện hot khi không ở trang 1
        if (layoutHotStoriesSection != null) {
            if (page == 1) {
                layoutHotStoriesSection.setVisibility(View.VISIBLE);
            } else {
                layoutHotStoriesSection.setVisibility(View.GONE);
            }
        }

        updatePaginationUI();

        // Tự động cuộn màn hình lên đầu danh sách Truyện Mới Cập Nhật
        if (homeScrollView != null && getView() != null) {
            View targetView = getView().findViewById(R.id.layoutNewStoriesTitle);
            if (targetView != null) {
                homeScrollView.post(() -> homeScrollView.smoothScrollTo(0, targetView.getTop()));
            }
        }
    }

    private void updatePaginationUI() {
        int totalPages = (int) Math.ceil((double) allNewStories.size() / PAGE_SIZE);

        View layoutPagination = getView() != null ? getView().findViewById(R.id.layoutPagination) : null;
        if (layoutPagination != null) {
            if (totalPages <= 1) {
                layoutPagination.setVisibility(View.GONE);
                return;
            } else {
                layoutPagination.setVisibility(View.VISIBLE);
            }
        }

        if (btnPrevPage != null) {
            btnPrevPage.setEnabled(currentPage > 1);
            btnPrevPage.setAlpha(currentPage > 1 ? 1.0f : 0.4f);
        }
        if (btnNextPage != null) {
            btnNextPage.setEnabled(currentPage < totalPages);
            btnNextPage.setAlpha(currentPage < totalPages ? 1.0f : 0.4f);
        }

        if (layoutPageNumbers != null) {
            layoutPageNumbers.removeAllViews();

            for (int i = 1; i <= totalPages; i++) {
                final int pageIndex = i;
                Button btn = new Button(getContext());

                // Cấu hình kích thước nút (40dp x 40dp)
                float scale = getResources().getDisplayMetrics().density;
                int sizeInPx = (int) (38 * scale + 0.5f);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
                int marginInPx = (int) (4 * scale + 0.5f);
                params.setMargins(marginInPx, 0, marginInPx, 0);
                btn.setLayoutParams(params);

                btn.setPadding(0, 0, 0, 0);
                btn.setText(String.valueOf(i));
                btn.setTextSize(12);

                // Highlight trang hiện tại
                if (i == currentPage) {
                    btn.setBackgroundColor(android.graphics.Color.parseColor("#E91E63")); // Màu hồng đặc trưng
                    btn.setTextColor(android.graphics.Color.WHITE);
                } else {
                    btn.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0")); // Màu xám nhạt
                    btn.setTextColor(android.graphics.Color.BLACK);
                }

                btn.setOnClickListener(v -> {
                    currentPage = pageIndex;
                    displayNewStoriesPage(currentPage);
                });

                layoutPageNumbers.addView(btn);
            }
        }
    }

    private void openStoryDetail(Story story) {
        if (getActivity() == null) return;
        android.content.Intent intent = new android.content.Intent(getActivity(), StoryDetailActivity.class);
        intent.putExtra("STORY_ID", story.getId());
        intent.putExtra("STORY_TITLE", story.getTitle());
        startActivity(intent);
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

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Lỗi tải truyện: " + message, Toast.LENGTH_SHORT).show();
        }
    }
}