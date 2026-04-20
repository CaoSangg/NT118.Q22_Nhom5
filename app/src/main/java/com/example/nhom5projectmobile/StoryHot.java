package com.example.nhom5projectmobile;

public class StoryHot {
    private String name;
    private String chapter;
    private int imageRes; // Tạm thời dùng ảnh trong drawable

    public StoryHot(String name, String chapter, int imageRes) {
        this.name = name;
        this.chapter = chapter;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public String getChapter() { return chapter; }
    public int getImageRes() { return imageRes; }
}

