package com.focusmaxxing.service;

import com.focusmaxxing.model.Priority;
import com.focusmaxxing.model.Task;
import com.focusmaxxing.model.User;
import com.focusmaxxing.repository.TaskRepository;
import com.focusmaxxing.repository.TaskRepositoryImpl;
import com.focusmaxxing.util.SessionManager;

import java.util.List;
import java.util.Optional;

public class TaskService {
    private final TaskRepository taskRepository;
    private final PomodoroService pomodoroService;

    public TaskService() {
        this.taskRepository = new TaskRepositoryImpl();
        this.pomodoroService = new PomodoroService();
    }

    private User getLoggedInUser() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user logged in");
        }
        return user;
    }

    public Task addTask(String title, String description, Priority priority) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Judul tugas tidak boleh kosong");
        }
        User user = getLoggedInUser();
        
        Task task = new Task();
        task.setUserId(user.getId());
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority != null ? priority : Priority.MEDIUM);
        task.setCompleted(false);
        
        return taskRepository.save(task);
    }

    public Task updateTask(int taskId, String title, String description, Priority priority) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            if (!task.getUserId().equals(getLoggedInUser().getId())) {
                throw new SecurityException("Tidak bisa mengubah tugas milik pengguna lain");
            }
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(priority);
            return taskRepository.save(task);
        }
        throw new IllegalArgumentException("Tugas tidak ditemukan");
    }

    public void completeTask(int taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            if (!task.getUserId().equals(getLoggedInUser().getId())) {
                throw new SecurityException("Tidak bisa menyelesaikan tugas milik pengguna lain");
            }
            if (!task.isCompleted()) {
                task.setCompleted(true);
                taskRepository.save(task);
                // Update the user's daily statistics
                pomodoroService.incrementTaskCompleted();
            }
        } else {
            throw new IllegalArgumentException("Tugas tidak ditemukan");
        }
    }

    public List<Task> getMyTasks() {
        return taskRepository.findByUserId(getLoggedInUser().getId());
    }

    public void deleteTask(int taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            if (!taskOpt.get().getUserId().equals(getLoggedInUser().getId())) {
                throw new SecurityException("Tidak bisa menghapus tugas milik pengguna lain");
            }
            taskRepository.delete(taskId);
        }
    }
}
