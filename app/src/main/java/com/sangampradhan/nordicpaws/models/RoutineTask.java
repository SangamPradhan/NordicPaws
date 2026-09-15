package com.sangampradhan.nordicpaws.models;

public class RoutineTask {
    private String id;
    private String petId;
    private String title;
    private String time;
    private String category;
    private boolean isCompleted;

    public RoutineTask(String id, String petId, String title, String time, String category, boolean isCompleted) {
        this.id = id;
        this.petId = petId;
        this.title = title;
        this.time = time;
        this.category = category;
        this.isCompleted = isCompleted;
    }

    public String getId() { return id; }
    public String getPetId() { return petId; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getCategory() { return category; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
