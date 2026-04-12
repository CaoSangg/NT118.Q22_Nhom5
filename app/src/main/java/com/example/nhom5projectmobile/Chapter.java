package com.example.nhom5projectmobile;

public class Chapter {
    private String title;
    private String content; // Lưu link PDF chương
    private long orderIndex; // Dùng để sắp xếp thứ tự
    private long chapterNumber;

    public Chapter() {} // Constructor trống cho Firebase

    public Chapter(String title, String content, long orderIndex, long chapterNumber) {
        this.title = title;
        this.content = content;
        this.orderIndex = orderIndex;
        this.chapterNumber = chapterNumber;
    }
    public long getOrderIndex() { return orderIndex; }
    public long getChapterNumber() { return chapterNumber; }

    public String getTitle() { return title; }
    public String getContent() { return content; }
}