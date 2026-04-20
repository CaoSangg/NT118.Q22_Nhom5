package com.example.nhom5projectmobile;

public class StoryRanking {
    private String name;
    private String views;
    private int rank;
    private int imageResId; // Tạm thời dùng ảnh trong máy (drawable)

    public StoryRanking(String name, String views, int rank, int imageResId) {
        this.name = name;
        this.views = views;
        this.rank = rank;
        this.imageResId = imageResId;
    }

    // Getter cho các thuộc tính
    public String getName() { return name; }
    public String getViews() { return views; }
    public int getRank() { return rank; }
    public int getImageResId() { return imageResId; }
}
