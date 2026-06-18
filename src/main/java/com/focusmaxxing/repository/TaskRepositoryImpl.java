package com.focusmaxxing.repository;

import com.focusmaxxing.model.Priority;
import com.focusmaxxing.model.Task;
import com.focusmaxxing.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class    TaskRepositoryImpl implements TaskRepository {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, FORMATTER);
    }

    private Task mapRowToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setUserId(rs.getInt("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPriority(Priority.valueOf(rs.getString("priority")));
        task.setCompleted(rs.getBoolean("is_completed"));
        task.setCreatedAt(parseDateTime(rs.getString("created_at")));
        return task;
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            String sql = "INSERT INTO tasks (user_id, title, description, priority, is_completed) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setInt(1, task.getUserId());
                pstmt.setString(2, task.getTitle());
                pstmt.setString(3, task.getDescription());
                pstmt.setString(4, task.getPriority().name());
                pstmt.setBoolean(5, task.isCompleted());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new DataAccessException("Error inserting task", e);
            }
        } else {
            String sql = "UPDATE tasks SET user_id = ?, title = ?, description = ?, priority = ?, is_completed = ? WHERE id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, task.getUserId());
                pstmt.setString(2, task.getTitle());
                pstmt.setString(3, task.getDescription());
                pstmt.setString(4, task.getPriority().name());
                pstmt.setBoolean(5, task.isCompleted());
                pstmt.setInt(6, task.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new DataAccessException("Error updating task", e);
            }
        }
        return task;
    }

    @Override
    public Optional<Task> findById(int id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding task by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Task> findByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding tasks by user id", e);
        }
        return tasks;
    }

    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all tasks", e);
        }
        return tasks;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting task", e);
        }
    }
}
