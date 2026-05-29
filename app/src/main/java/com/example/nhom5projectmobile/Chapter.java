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

    // CÁC HÀM SETTER (BỔ SUNG ĐỂ SỬ DỤNG CHO OFFLINE VÀ DATABASE)
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }
    public void setTitle(String title) { this.title = title; }
    public void setPages(List<String> pages) { this.pages = pages; }
    public void setContent(String content) { this.content = content; }
    public void setOrderIndex(long orderIndex) { this.orderIndex = orderIndex; }
    public void setChapterNumber(long chapterNumber) { this.chapterNumber = chapterNumber; }
}