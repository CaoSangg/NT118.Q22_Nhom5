package com.example.nhom5projectmobile;

public class Story {

    private String storyId;

    private String id;

    private String title;

    private String author;

    private String coverImage;

    private long views;

    private String chapter;

    private String timeAgo;

    private String pdfUrl;

    public Story() {
    }

    public Story(
            String storyId,
            String title,
            String author,
            String coverImage,
            long views,
            String chapter,
            String timeAgo,
            String pdfUrl
    ) {

        this.storyId = storyId;

        this.id = storyId;

        this.title = title;

        this.author = author;

        this.coverImage = coverImage;

        this.views = views;

        this.chapter = chapter;

        this.timeAgo = timeAgo;

        this.pdfUrl = pdfUrl;
    }

    // =========================
    // GETTERS
    // =========================

    public String getStoryId() {

        return storyId;
    }

    public String getId() {

        return id;
    }

    public String getTitle() {

        return title;
    }

    public String getAuthor() {

        return author;
    }

    public String getCoverImage() {

        return coverImage;
    }

    public long getViews() {

        return views;
    }

    public String getChapter() {

        return chapter;
    }

    public String getTimeAgo() {

        return timeAgo;
    }

    public String getPdfUrl() {

        return pdfUrl;
    }

    // =========================
    // SETTERS
    // =========================

    public void setStoryId(String storyId) {

        this.storyId = storyId;

        this.id = storyId;
    }

    public void setId(String id) {

        this.id = id;

        this.storyId = id;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public void setAuthor(String author) {

        this.author = author;
    }

    public void setCoverImage(String coverImage) {

        this.coverImage = coverImage;
    }

    public void setViews(long views) {

        this.views = views;
    }

    public void setChapter(String chapter) {

        this.chapter = chapter;
    }

    public void setTimeAgo(String timeAgo) {

        this.timeAgo = timeAgo;
    }

    public void setPdfUrl(String pdfUrl) {

        this.pdfUrl = pdfUrl;
    }
}