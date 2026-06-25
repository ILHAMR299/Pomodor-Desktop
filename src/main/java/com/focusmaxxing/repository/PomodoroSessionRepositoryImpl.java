package com.focusmaxxing.repository;

import com.focusmaxxing.model.PomodoroSession;
import com.focusmaxxing.model.SessionStatus;
import com.focusmaxxing.model.SessionType;
import com.focusmaxxing.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PomodoroSessionRepositoryImpl implements PomodoroSessionRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(FORMATTER);
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, FORMATTER);
    }

    private PomodoroSession mapRowToSession(ResultSet rs) throws SQLException {
        PomodoroSession session = new PomodoroSession();
        session.setId(rs.getInt("id"));
        session.setUserId(rs.getInt("user_id"));
        
        int taskId = rs.getInt("task_id");
        if (!rs.wasNull()) {
            session.setTaskId(taskId);
        }

        session.setSessionType(SessionType.valueOf(rs.getString("session_type")));
        session.setDurationMinutes(rs.getInt("duration_minutes"));
        session.setStartedAt(parseDateTime(rs.getString("started_at")));
        session.setCompletedAt(parseDateTime(rs.getString("completed_at")));
        session.setStatus(SessionStatus.valueOf(rs.getString("status")));
        return session;
    }

    @Override
    public PomodoroSession save(PomodoroSession session) {
        if (session.getId() == null) {
            String sql = "INSERT INTO pomodoro_sessions (user_id, task_id, session_type, " +
                    ", started_at, completed_at, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setInt(1, session.getUserId());
                if (session.getTaskId() != null) {
                    pstmt.setInt(2, session.getTaskId());
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                pstmt.setString(3, session.getSessionType().name());
                pstmt.setInt(4, session.getDurationMinutes());
                pstmt.setString(5, formatDateTime(session.getStartedAt()));
                pstmt.setString(6, formatDateTime(session.getCompletedAt()));
                pstmt.setString(7, session.getStatus().name());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        session.setId(generatedKeys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting pomodoro session", e);
            }
        } else {
            String sql = "UPDATE pomodoro_sessions SET user_id = ?, task_id = ?, session_type = ?, duration_minutes = ?, started_at = ?, completed_at = ?, status = ? WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, session.getUserId());
                if (session.getTaskId() != null) {
                    pstmt.setInt(2, session.getTaskId());
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                pstmt.setString(3, session.getSessionType().name());
                pstmt.setInt(4, session.getDurationMinutes());
                pstmt.setString(5, formatDateTime(session.getStartedAt()));
                pstmt.setString(6, formatDateTime(session.getCompletedAt()));
                pstmt.setString(7, session.getStatus().name());
                pstmt.setInt(8, session.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("Error updating pomodoro session", e);
            }
        }
        return session;
    }

    @Override
    public List<PomodoroSession> findByUserId(int userId) {
        List<PomodoroSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM pomodoro_sessions WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapRowToSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding pomodoro sessions by user id", e);
        }
        return sessions;
    }
}
