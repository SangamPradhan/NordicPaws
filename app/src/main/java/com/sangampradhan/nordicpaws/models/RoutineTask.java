package com.sangampradhan.nordicpaws.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoutineTask {
    private String id;
    private String petId;
    private String title;
    private String time;
    private String category;
    private boolean isCompleted;
    private String lastCompletedDate; // Date formatted as "yyyy-MM-dd"

    // Schedule frequency fields: "DAILY", "WEEKLY", "SPECIFIC_DAYS", "ONCE"
    private String scheduleType;
    // Specific days if scheduleType is "SPECIFIC_DAYS" (e.g., ["MON", "WED", "FRI"])
    private List<String> daysOfWeek;

    // Default no-arg constructor required for Firestore
    public RoutineTask() {
    }

    public RoutineTask(String id, String petId, String title, String time, String category, boolean isCompleted) {
        this.id = id;
        this.petId = petId;
        this.title = title;
        this.time = time;
        this.category = category;
        this.isCompleted = isCompleted;
        this.scheduleType = "DAILY";
        if (isCompleted) {
            this.lastCompletedDate = getTodayDateString();
        }
    }

    public RoutineTask(String id, String petId, String title, String time, String category, 
                       boolean isCompleted, String scheduleType, List<String> daysOfWeek) {
        this.id = id;
        this.petId = petId;
        this.title = title;
        this.time = time;
        this.category = category;
        this.isCompleted = isCompleted;
        this.scheduleType = scheduleType;
        this.daysOfWeek = daysOfWeek;
        if (isCompleted) {
            this.lastCompletedDate = getTodayDateString();
        }
    }

    public static String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCompleted() {
        if (lastCompletedDate != null && !lastCompletedDate.isEmpty()) {
            return lastCompletedDate.equals(getTodayDateString());
        }
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed) {
            this.lastCompletedDate = getTodayDateString();
        } else {
            this.lastCompletedDate = "";
        }
    }

    public String getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(String lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    public List<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }
}
