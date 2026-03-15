package com.example.nhom5projectmobile;

public class Story {
    private String title;
    private String chapter;
    private String views;
    private int imageResource; // Tạm thời dùng ảnh trong máy

    public Story(String title, String chapter, String views, int imageResource) {
        this.title = title;
        this.chapter = chapter;
        this.views = views;
        this.imageResource = imageResource;
    }

    // Getters
    public String getTitle() { return title; }
    public String getChapter() { return chapter; }
    public String getViews() { return views; }
    public int getImageResource() { return imageResource; }
}
