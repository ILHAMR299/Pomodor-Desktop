package com.focusmaxxing.repository;

import com.focusmaxxing.model.Statistic;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatisticRepository {
    Statistic save(Statistic statistic);
    Optional<Statistic> findByUserIdAndDate(int userId, LocalDate statDate);
    List<Statistic> findByUserId(int userId);
}
