package com.focusmaxxing.service;

import com.focusmaxxing.model.PomodoroSession;
import com.focusmaxxing.model.SessionStatus;
import com.focusmaxxing.model.SessionType;
import com.focusmaxxing.model.Statistic;
import com.focusmaxxing.model.User;
import com.focusmaxxing.repository.PomodoroSessionRepository;
import com.focusmaxxing.repository.PomodoroSessionRepositoryImpl;
import com.focusmaxxing.repository.StatisticRepository;
import com.focusmaxxing.repository.StatisticRepositoryImpl;
import com.focusmaxxing.util.SessionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class PomodoroService {
    private final PomodoroSessionRepository sessionRepository;
    private final StatisticRepository statisticRepository;

    public PomodoroService() {
        this.sessionRepository = new PomodoroSessionRepositoryImpl();
        this.statisticRepository = new StatisticRepositoryImpl();
    }

    private User getLoggedInUser() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user logged in");
        }
        return user;
    }

    public PomodoroSession saveSession(Integer taskId, SessionType type, int durationMinutes, LocalDateTime startedAt, SessionStatus status) {
        User user = getLoggedInUser();
        
        PomodoroSession session = new PomodoroSession();
        session.setUserId(user.getId());
        session.setTaskId(taskId);
        session.setSessionType(type);
        session.setDurationMinutes(durationMinutes);
        session.setStartedAt(startedAt);
        session.setCompletedAt(LocalDateTime.now());
        session.setStatus(status);
        
        session = sessionRepository.save(session);
        
        // Update statistics for all Focus sessions, including interrupted ones (partial minutes)
        if (type == SessionType.FOCUS && durationMinutes > 0) {
            updateFocusStatistics(user.getId(), durationMinutes, startedAt.toLocalDate());
        }
        
        return session;
    }

    private void updateFocusStatistics(int userId, int durationMinutes, LocalDate date) {
        Optional<Statistic> statOpt = statisticRepository.findByUserIdAndDate(userId, date);
        Statistic stat;
        if (statOpt.isPresent()) {
            stat = statOpt.get();
            stat.setTotalFocusMinutes(stat.getTotalFocusMinutes() + durationMinutes);
        } else {
            stat = new Statistic();
            stat.setUserId(userId);
            stat.setStatDate(date);
            stat.setTotalFocusMinutes(durationMinutes);
            stat.setTasksCompleted(0);
        }
        statisticRepository.save(stat);
    }
    
    public void incrementTaskCompleted() {
        User user = getLoggedInUser();
        LocalDate today = LocalDate.now();
        Optional<Statistic> statOpt = statisticRepository.findByUserIdAndDate(user.getId(), today);
        Statistic stat;
        if (statOpt.isPresent()) {
            stat = statOpt.get();
            stat.setTasksCompleted(stat.getTasksCompleted() + 1);
        } else {
            stat = new Statistic();
            stat.setUserId(user.getId());
            stat.setStatDate(today);
            stat.setTotalFocusMinutes(0);
            stat.setTasksCompleted(1);
        }
        statisticRepository.save(stat);
    }
}
