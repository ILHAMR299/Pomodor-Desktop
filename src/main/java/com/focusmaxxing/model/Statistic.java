package com.focusmaxxing.model;

import java.time.LocalDate;
import java.util.Objects;

public class Statistic {
    private Integer id;
    private Integer userId;
    private LocalDate statDate;
    private int totalFocusMinutes;
    private int tasksCompleted;

    public Statistic() {
    }

    public Statistic(Integer id, Integer userId, LocalDate statDate, int totalFocusMinutes, int tasksCompleted) {
        this.id = id;
        this.userId = userId;
        this.statDate = statDate;
        this.totalFocusMinutes = totalFocusMinutes;
        this.tasksCompleted = tasksCompleted;
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

    public LocalDate getStatDate() {
        return statDate;
    }

    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }

    public int getTotalFocusMinutes() {
        return totalFocusMinutes;
    }

    public void setTotalFocusMinutes(int totalFocusMinutes) {
        this.totalFocusMinutes = totalFocusMinutes;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statistic statistic = (Statistic) o;
        return totalFocusMinutes == statistic.totalFocusMinutes &&
               tasksCompleted == statistic.tasksCompleted &&
               Objects.equals(id, statistic.id) &&
               Objects.equals(userId, statistic.userId) &&
               Objects.equals(statDate, statistic.statDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, statDate, totalFocusMinutes, tasksCompleted);
    }

    @Override
    public String toString() {
        return "Statistic{" +
                "id=" + id +
                ", userId=" + userId +
                ", statDate=" + statDate +
                ", totalFocusMinutes=" + totalFocusMinutes +
                ", tasksCompleted=" + tasksCompleted +
                '}';
    }
}
