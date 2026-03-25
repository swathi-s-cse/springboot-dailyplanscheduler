package com.app.todo.services;

import com.app.todo.models.Task;
import com.app.todo.models.DailyProgress;
import com.app.todo.repository.TaskRepository;
import com.app.todo.repository.DailyProgressRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final DailyProgressRepository dailyProgressRepository;

    public TaskService(TaskRepository taskRepository, DailyProgressRepository dailyProgressRepository) {
        this.taskRepository = taskRepository;
        this.dailyProgressRepository = dailyProgressRepository;
    }

    // ------------------- Task CRUD -------------------
    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }

    public void createTask(String title){
        Task task = new Task();
        task.setTitle(title);
        task.setCompleted(false);
        taskRepository.save(task);

        saveTodayProgress(); // ✅ update progress
    }

    public void deleteTask(Long id){
        taskRepository.deleteById(id);
        saveTodayProgress(); // ✅ update progress
    }

    public void toggleTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task id"));

        if (task.isCompletedToday()) {
            task.setCompleted(false);
            task.setLastCompletedDate(null);
        } else {
            task.setCompleted(true);
            task.setLastCompletedDate(LocalDate.now());
        }

        taskRepository.save(task);
        saveTodayProgress(); // ✅ update progress
    }

    // ------------------- Today Completion -------------------
    public int getTodayCompletionPercentage() {
        List<Task> tasks = taskRepository.findAll();
        if (tasks.isEmpty()) return 0;

        long completedCount = tasks.stream()
                .filter(Task::isCompletedToday)
                .count();

        return (int) ((completedCount * 100) / tasks.size());
    }

    // ------------------- Daily Progress Persistence -------------------
    public void saveTodayProgress() {
        int todayScore = getTodayCompletionPercentage();
        LocalDate today = LocalDate.now();

        // ✅ Find existing or create new
        DailyProgress progress = dailyProgressRepository.findByDate(today)
                .orElse(new DailyProgress());
        progress.setDate(today);
        progress.setCompletionPercentage(todayScore);

        dailyProgressRepository.save(progress);
    }

    // ------------------- Last 7 Days Completion -------------------
    public List<Integer> getLast7DaysCompletion() {
        List<Integer> scores = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int percentage = dailyProgressRepository.findByDate(date)
                    .map(DailyProgress::getCompletionPercentage)
                    .orElse(0); // default 0 if no record
            scores.add(percentage);
        }
        return scores;
    }


    // existing fields and methods ...

    // ------------------- Daily Reset -------------------
    @Scheduled(cron = "0 0 0 * * ?") // Every day at 00:00 (midnight)
    public void resetDailyTasks() {
        List<Task> tasks = taskRepository.findAll();
        for (Task task : tasks) {
            task.setCompleted(false);
            task.setLastCompletedDate(null);
        }
        taskRepository.saveAll(tasks);

        // Optional: Save today's progress as 0 for the new day
        saveTodayProgress();
        System.out.println("Daily tasks reset at midnight.");
    }
}
