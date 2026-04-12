package com.example.nhom5projectmobile;
public class Story {
    private String id;
    private String title;
    private String author;
    private String coverImage;
    private long views;
    private String chapter;

    // Thêm biến mới để lưu thời gian (Ví dụ: "3 ngày trước")
    private String timeAgo;

    private String pdfUrl;

    public Story() {}

    // Constructor đã được cập nhật để nhận đủ 7 tham số
    public Story(String id, String title, String author, String coverImage, long views, String chapter, String timeAgo, String pdfUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverImage = coverImage;
        this.views = views;
        this.chapter = chapter;
        this.timeAgo = timeAgo;
        this.pdfUrl = pdfUrl;
    }

    // Các hàm Getter để lấy dữ liệu ra
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverImage() { return coverImage; }
    public long getViews() { return views; }
    public String getChapter() { return chapter; }
    public String getTimeAgo() { return timeAgo; }

    // Các hàm Setter (nếu sau này cần dùng để sửa dữ liệu)
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public void setViews(long views) { this.views = views; }
    public void setChapter(String chapter) { this.chapter = chapter; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
}