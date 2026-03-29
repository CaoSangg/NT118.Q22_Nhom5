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

    private RecyclerView recyclerView;
    private StoryAdapter adapter;
    private List<Story> storyList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.rvHomeStories);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        storyList = new ArrayList<>();
        adapter = new StoryAdapter(storyList);
        recyclerView.setAdapter(adapter);

        loadStoriesFromFirestore();

        return view;
    }

    private void loadStoriesFromFirestore() {
        // Sắp xếp theo monthlyViews (Top tháng) như trong database_guide.md
        db.collection("stories")
                .orderBy("monthlyViews", Query.Direction.DESCENDING)
                .limit(15)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    storyList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        // Map trực tiếp từ Document sang Object Story
                        String title = doc.getString("title");
                        String author = doc.getString("author");
                        String coverImage = doc.getString("coverImage");
                        long views = doc.getLong("viewCount") != null ? doc.getLong("viewCount") : 0;

                        // Lấy chương mới nhất (giả sử bạn lưu field latestChapterName ở root để đỡ query sub-collection)
                        String chapter = doc.contains("latestChapter") ? "Chương " + doc.get("latestChapter") : "Đang cập nhật";

                        Story story = new Story(doc.getId(), title, author, coverImage, views, chapter);
                        storyList.add(story);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải truyện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}