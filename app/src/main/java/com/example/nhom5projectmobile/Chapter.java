package com.example.nhom5projectmobile;

import java.util.List;

public class Chapter {
    private String chapterId; // BẮT BUỘC PHẢI CÓ để lấy ID truyền sang màn hình đọc
    private String title;
    private List<String> pages; // Mảng chứa các link ảnh
    private String content; // Giữ lại dự phòng cho các truyện cũ dùng file PDF
    private long orderIndex;
    private long chapterNumber;

    public Chapter() {} // Constructor trống cho Firebase tự động ép kiểu

    // Các hàm Getter để lấy dữ liệu
    public String getChapterId() { return chapterId; }
    public String getTitle() { return title; }
    public List<String> getPages() { return pages; }
    public String getContent() { return content; }
    public long getOrderIndex() { return orderIndex; }
    public long getChapterNumber() { return chapterNumber; }
}