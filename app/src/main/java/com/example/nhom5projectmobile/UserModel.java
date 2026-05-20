package com.example.nhom5projectmobile;

public class UserModel {

    private String userId;
    private String username;
    private String email;
    private String role;

    private Boolean canManageStories;

    public UserModel() {
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Boolean getCanManageStories() {
        return canManageStories;
    }
}