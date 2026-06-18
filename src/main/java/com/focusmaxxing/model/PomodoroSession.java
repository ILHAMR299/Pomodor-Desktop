package com.focusmaxxing.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class PomodoroSession {
    private Integer id;
    private Integer userId;
    private Integer taskId;
    private SessionType sessionType;
    private int durationMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private SessionStatus status;

    public PomodoroSession() {
    }

    public PomodoroSession(Integer id, Integer userId, Integer taskId, SessionType sessionType, int durationMinutes, LocalDateTime startedAt, LocalDateTime completedAt, SessionStatus status) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.sessionType = sessionType;
        this.durationMinutes = durationMinutes;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status != null ? status : SessionStatus.COMPLETED;
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

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomodoroSession that = (PomodoroSession) o;
        return durationMinutes == that.durationMinutes &&
               Objects.equals(id, that.id) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(taskId, that.taskId) &&
               sessionType == that.sessionType &&
               status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, taskId, sessionType, durationMinutes, status);
    }

    @Override
    public String toString() {
        return "PomodoroSession{" +
                "id=" + id +
                ", userId=" + userId +
                ", taskId=" + taskId +
                ", sessionType=" + sessionType +
                ", durationMinutes=" + durationMinutes +
                ", status=" + status +
                '}';
    }
}
