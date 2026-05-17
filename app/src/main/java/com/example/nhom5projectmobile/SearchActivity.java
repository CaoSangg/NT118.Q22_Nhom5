package com.example.nhom5projectmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView rvSearchResults;
    private TextView tvSearchTitle, tvNoResult;
    private ImageView btnBackSearch;
    private List<Story> searchList;
    private StoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        tvSearchTitle = findViewById(R.id.tvSearchTitle);
        tvNoResult = findViewById(R.id.tvNoResult);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        btnBackSearch = findViewById(R.id.btnBackSearch);

        // bắt nút back
        btnBackSearch.setOnClickListener(v -> finish());

        searchList = new ArrayList<>();

        adapter = new StoryAdapter(searchList, story -> {
            Intent intent = new Intent(SearchActivity.this, StoryDetailActivity.class);
            intent.putExtra("STORY_ID", story.getId());
            startActivity(intent);
        });

        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 3));
        rvSearchResults.setAdapter(adapter);

        String keyword = getIntent().getStringExtra("SEARCH_KEYWORD");
        if (keyword != null) {
            String finalKeyword = keyword.trim().toLowerCase();
            tvSearchTitle.setText("Kết quả cho: \"" + finalKeyword + "\"");
            performSearch(finalKeyword);
        }
    }

    private void performSearch(String keyword) {
        // Catalog là lưu TA nên này map để search Anh-Việt
        java.util.Map<String, String> categoryDict = new java.util.HashMap<>();
        categoryDict.put("action", "hành động");
        categoryDict.put("adventure", "phiêu lưu");
        categoryDict.put("fantasy", "viễn tưởng");
        categoryDict.put("science fiction", "khoa học viễn tưởng");
        categoryDict.put("comedy", "hài hước");
        categoryDict.put("drama", "chính kịch");
        categoryDict.put("mystery", "bí ẩn");
        categoryDict.put("supernatural", "siêu nhiên");
        categoryDict.put("historical", "lịch sử");
        categoryDict.put("psychological", "tâm lý");
        categoryDict.put("horror", "kinh dị");
        categoryDict.put("magic", "phép thuật");
        categoryDict.put("martial arts", "võ thuật");
        categoryDict.put("slice of life", "đời thường");
        categoryDict.put("family", "gia đình");
        categoryDict.put("shonen", "thiếu niên");
        categoryDict.put("dark fantasy", "viễn tưởng đen tối");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Tải danh sách truyện về và lọc tại máy (Client-side filtering)
        db.collection("stories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                        String titleLowercase = doc.getString("title_lowercase");
                        List<String> keywordsArray = (List<String>) doc.get("keywords");
                        List<String> categoriesArray = (List<String>) doc.get("category");

                        boolean isMatch = false;

                        // So với chữ trong Tên truyện
                        if (titleLowercase != null && titleLowercase.contains(keyword)) {
                            isMatch = true;
                        }

                        // So với Mảng Keywords
                        if (!isMatch && keywordsArray != null) {
                            for (String k : keywordsArray) {
                                if (k != null && k.toLowerCase().contains(keyword)) {
                                    isMatch = true;
                                    break;
                                }
                            }
                        }

                        // So với Mảng Thể loại (Anh-Việt)
                        if (!isMatch && categoriesArray != null) {
                            for (String c : categoriesArray) {
                                if (c != null) {
                                    // Bắt buộc phải dùng trim() thêm 1 lần nữa để dọn sạch dấu cách thừa trong Database
                                    String enWord = c.toLowerCase().trim();

                                    // Dò trong từ điển
                                    String vnWord = categoryDict.containsKey(enWord) ? categoryDict.get(enWord) : enWord;

                                    // Kiểm tra xem từ khóa người dùng gõ vào có khớp không
                                    if (enWord.contains(keyword) || vnWord.contains(keyword)) {
                                        isMatch = true;
                                        break;
                                    }
                                }
                            }
                        }

                        // Nếu khớp nào thì add vào danh sách kết quả
                        if (isMatch) {
                            String id = doc.getId();
                            String title = doc.getString("title");
                            String author = doc.getString("author");
                            String coverImage = doc.getString("coverImage");
                            long views = doc.contains("viewsCount") ? doc.getLong("viewsCount") : 0;
                            long chaptersCount = doc.contains("chaptersCount") ? doc.getLong("chaptersCount") : 0;

                            Story story = new Story(id, title, author, coverImage, views, "Số chương: " + chaptersCount, "Đang ra", "");
                            searchList.add(story);
                        }
                    }

                    if (searchList.isEmpty()) {
                        tvNoResult.setVisibility(View.VISIBLE);
                    } else {
                        tvNoResult.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}