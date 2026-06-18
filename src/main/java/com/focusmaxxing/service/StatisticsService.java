package com.focusmaxxing.service;

import com.focusmaxxing.model.Statistic;
import com.focusmaxxing.repository.StatisticRepository;
import com.focusmaxxing.repository.StatisticRepositoryImpl;
import com.focusmaxxing.util.SessionManager;

import java.util.List;

public class StatisticsService {
    private final StatisticRepository statisticRepository;

    public StatisticsService() {
        this.statisticRepository = new StatisticRepositoryImpl();
    }

    public List<Statistic> getMyStatistics() {
        if (SessionManager.getInstance().getCurrentUser() == null) {
            throw new IllegalStateException("User not logged in");
        }
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        return statisticRepository.findByUserId(userId);
    }
}
