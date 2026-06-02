package com.example.nhom5projectmobile;

import com.google.firebase.Timestamp;

public class Comment {
    private String userId;
    private String userName;
    private String content;
    private Timestamp timestamp;

    // Constructor rỗng bắt buộc phải có để Firebase Firestore có thể chuyển đổi dữ liệu tự động
    public Comment() {
    }

    public Comment(String userId, String userName, String content, Timestamp timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}