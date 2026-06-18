package com.focusmaxxing.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:focusmaxxing.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String usersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'USER',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String tasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                priority TEXT NOT NULL DEFAULT 'MEDIUM',
                is_completed BOOLEAN NOT NULL DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """;

        String sessionsTable = """
            CREATE TABLE IF NOT EXISTS pomodoro_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                task_id INTEGER,
                session_type TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                started_at DATETIME NOT NULL,
                completed_at DATETIME,
                status TEXT NOT NULL DEFAULT 'COMPLETED',
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL
            );
        """;

        String statsTable = """
            CREATE TABLE IF NOT EXISTS statistics (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                stat_date DATE NOT NULL,
                total_focus_minutes INTEGER DEFAULT 0,
                tasks_completed INTEGER DEFAULT 0,
                UNIQUE(user_id, stat_date),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """;

        String indexes = """
            CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id);
            CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON pomodoro_sessions(user_id);
            CREATE INDEX IF NOT EXISTS idx_stats_user_date ON statistics(user_id, stat_date);
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Enable foreign keys for SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");
            
            stmt.execute(usersTable);
            stmt.execute(tasksTable);
            stmt.execute(sessionsTable);
            stmt.execute(statsTable);
            
            // Execute indexes separately
            for(String indexSql : indexes.split(";")) {
                if(!indexSql.trim().isEmpty()) {
                    stmt.execute(indexSql.trim() + ";");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database schema", e);
        }
    }
}
