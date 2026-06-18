package com.focusmaxxing.repository;

import com.focusmaxxing.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(int id);
    List<Task> findByUserId(int userId);
    List<Task> findAll();
    void delete(int id);
}
