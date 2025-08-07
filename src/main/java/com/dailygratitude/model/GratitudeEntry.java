package com.dailygratitude.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GratitudeEntry {
    private Long id;
    private String entryText;
    private LocalDate createdDate;
    private LocalDateTime createdDateTime;
    private Integer moodRating; // 1-5 scale (optional)
    private String tags; // comma-separated tags (optional)
    
    // Constructors
    public GratitudeEntry() {
        // Default constructor
    }
    
    public GratitudeEntry(String entryText) {
        this.entryText = entryText;
        this.createdDate = LocalDate.now();
        this.createdDateTime = LocalDateTime.now();
    }
    
    public GratitudeEntry(String entryText, Integer moodRating) {
        this.entryText = entryText;
        this.moodRating = moodRating;
        this.createdDate = LocalDate.now();
        this.createdDateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEntryText() {
        return entryText;
    }
    
    public void setEntryText(String entryText) {
        this.entryText = entryText;
    }
    
    public LocalDate getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }
    
    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
    
    public Integer getMoodRating() {
        return moodRating;
    }
    
    public void setMoodRating(Integer moodRating) {
        this.moodRating = moodRating;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    // Utility methods
    public String getFormattedDate() {
        return createdDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }
    
    public String getFormattedDateTime() {
        return createdDateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"));
    }
    
    public String getPreview(int maxLength) {
        if (entryText == null) return "";
        if (entryText.length() <= maxLength) return entryText;
        return entryText.substring(0, maxLength) + "...";
    }
    
    @Override
    public String toString() {
        return "GratitudeEntry{" +
                "id=" + id +
                ", date=" + createdDate +
                ", preview='" + getPreview(50) + '\'' +
                '}';
    }
}