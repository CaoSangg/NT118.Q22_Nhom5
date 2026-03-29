package com.example.nhom5projectmobile;

import java.util.List;

public class Story {
    private String storyId;
    private String title;
    private String author;
    private String coverImage;
    private String description;
    private String status;
    private long viewCount;
    private long monthlyViews; // Dùng để sắp xếp Top tháng
    private List<String> category;
    private String latestChapter; // Hiển thị ở danh sách ngoài

    // Constructor rỗng cho Firebase
    public Story() {}

    public Story(String storyId, String title, String author, String coverImage, long viewCount, String latestChapter) {
        this.storyId = storyId;
        this.title = title;
        this.author = author;
        this.coverImage = coverImage;
        this.viewCount = viewCount;
        this.latestChapter = latestChapter;
    }

    // Getters
    public String getStoryId() { return storyId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverImage() { return coverImage; }
    public long getViewCount() { return viewCount; }
    public String getLatestChapter() { return latestChapter; }
}