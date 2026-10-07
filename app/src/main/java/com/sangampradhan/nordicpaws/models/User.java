package com.sangampradhan.nordicpaws.models;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String address;
    private String mobileNumber;
    private long createdAt;
    private String localProfileImagePath;

    // Default no-arg constructor required for Firebase Firestore deserialization
    public User() {
    }

    public User(String uid, String fullName, String email, String address, String mobileNumber, long createdAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.mobileNumber = mobileNumber;
        this.createdAt = createdAt;
    }

    public User(String uid, String fullName, String email, String address, String mobileNumber, long createdAt, String localProfileImagePath) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.mobileNumber = mobileNumber;
        this.createdAt = createdAt;
        this.localProfileImagePath = localProfileImagePath;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getLocalProfileImagePath() {
        return localProfileImagePath;
    }

    public void setLocalProfileImagePath(String localProfileImagePath) {
        this.localProfileImagePath = localProfileImagePath;
    }
}
