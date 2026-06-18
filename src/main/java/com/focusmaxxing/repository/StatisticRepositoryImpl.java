package com.focusmaxxing.repository;

import com.focusmaxxing.model.Statistic;
import com.focusmaxxing.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatisticRepositoryImpl implements StatisticRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(FORMATTER);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        // SQLite DATE is stored as YYYY-MM-DD text
        return LocalDate.parse(dateStr, FORMATTER);
    }

    private Statistic mapRowToStatistic(ResultSet rs) throws SQLException {
        Statistic statistic = new Statistic();
        statistic.setId(rs.getInt("id"));
        statistic.setUserId(rs.getInt("user_id"));
        statistic.setStatDate(parseDate(rs.getString("stat_date")));
        statistic.setTotalFocusMinutes(rs.getInt("total_focus_minutes"));
        statistic.setTasksCompleted(rs.getInt("tasks_completed"));
        return statistic;
    }

    @Override
    public Statistic save(Statistic statistic) {
        if (statistic.getId() == null) {
            String sql = "INSERT INTO statistics (user_id, stat_date, total_focus_minutes, tasks_completed) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setInt(1, statistic.getUserId());
                pstmt.setString(2, formatDate(statistic.getStatDate()));
                pstmt.setInt(3, statistic.getTotalFocusMinutes());
                pstmt.setInt(4, statistic.getTasksCompleted());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        statistic.setId(generatedKeys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting statistic", e);
            }
        } else {
            String sql = "UPDATE statistics SET user_id = ?, stat_date = ?, total_focus_minutes = ?, tasks_completed = ? WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, statistic.getUserId());
                pstmt.setString(2, formatDate(statistic.getStatDate()));
                pstmt.setInt(3, statistic.getTotalFocusMinutes());
                pstmt.setInt(4, statistic.getTasksCompleted());
                pstmt.setInt(5, statistic.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("Error updating statistic", e);
            }
        }
        return statistic;
    }

    @Override
    public Optional<Statistic> findByUserIdAndDate(int userId, LocalDate statDate) {
        String sql = "SELECT * FROM statistics WHERE user_id = ? AND stat_date = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, formatDate(statDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStatistic(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding statistic by user id and date", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Statistic> findByUserId(int userId) {
        List<Statistic> stats = new ArrayList<>();
        String sql = "SELECT * FROM statistics WHERE user_id = ? ORDER BY stat_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(mapRowToStatistic(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding statistics by user id", e);
        }
        return stats;
    }
}
