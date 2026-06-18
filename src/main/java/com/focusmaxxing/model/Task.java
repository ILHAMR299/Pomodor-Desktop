package com.focusmaxxing.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private Integer id;
    private Integer userId;
    private String title;
    private String description;
    private Priority priority;
    private boolean isCompleted;
    private LocalDateTime createdAt;

    public Task() {
    }

    public Task(Integer id, Integer userId, String title, String description, Priority priority, boolean isCompleted, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return isCompleted == task.isCompleted &&
               Objects.equals(id, task.id) &&
               Objects.equals(userId, task.userId) &&
               Objects.equals(title, task.title) &&
               priority == task.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, priority, isCompleted);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
