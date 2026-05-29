package com.example.nhom5projectmobile;

public class StoryRanking {
    private String storyId;
    private String title;
    private int viewsCount;   // Sửa thành viewsCount cho khớp Firebase
    private int viewsMonth;   // Cần add thêm trên Firebase
    private int viewsWeek;    // Cần add thêm trên Firebase
    private String coverImage; // Sửa thành coverImage cho khớp Firebase

    // Thuộc tính phụ trợ cho UI (Không lưu trên Firebase)
    private int rank;
    private int currentDisplayViews;

    // Bắt buộc phải có constructor rỗng cho Firestore
    public StoryRanking() {}

    // Getters cho Firestore map dữ liệu
    public String getStoryId() { return storyId; }
    public String getTitle() { return title; }
    public int getViewsCount() { return viewsCount; }
    public int getViewsMonth() { return viewsMonth; }
    public int getViewsWeek() { return viewsWeek; }
    public String getCoverImage() { return coverImage; }

    // Setters cho Firestore map dữ liệu
    public void setStoryId(String storyId) { this.storyId = storyId; }
    public void setTitle(String title) { this.title = title; }
    public void setViewsCount(int viewsCount) { this.viewsCount = viewsCount; }
    public void setViewsMonth(int viewsMonth) { this.viewsMonth = viewsMonth; }
    public void setViewsWeek(int viewsWeek) { this.viewsWeek = viewsWeek; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    // Getter/Setter dùng cho UI
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public int getCurrentDisplayViews() { return currentDisplayViews; }
    public void setCurrentDisplayViews(int currentDisplayViews) { this.currentDisplayViews = currentDisplayViews; }
}
