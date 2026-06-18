package com.focusmaxxing.repository;

import com.focusmaxxing.model.PomodoroSession;
import java.util.List;

public interface PomodoroSessionRepository {
    PomodoroSession save(PomodoroSession session);
    List<PomodoroSession> findByUserId(int userId);
}
