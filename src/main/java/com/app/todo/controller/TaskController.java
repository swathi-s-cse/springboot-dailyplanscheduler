package com.app.todo.controller;


import com.app.todo.models.Task;
import com.app.todo.services.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
//@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public String getTasks(Model model){
        List<Task> tasks = taskService.getAllTasks();

        taskService.saveTodayProgress();

        int todayScore = taskService.getTodayCompletionPercentage();
        List<Integer> last7Days = taskService.getLast7DaysCompletion();

        model.addAttribute("tasks", tasks);
        model.addAttribute("todayScore", todayScore);
        model.addAttribute("last7Days", last7Days);
        return "tasks";
    }



    @PostMapping
    public String createTasks(@RequestParam String title){
        taskService.createTask(title);

        return "redirect:/";
    }

    @GetMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return "redirect:/";
    }

    @GetMapping("/{id}/toggle")
    public String toggleTask(@PathVariable Long id){
        taskService.toggleTask(id);
        return "redirect:/";
    }


    @GetMapping("/today-score")
    @ResponseBody
    public String todayScore() {
        return taskService.getTodayCompletionPercentage() + "%";
    }

    @GetMapping("/weekly")
    public String weeklyProgress(Model model) {
        List<Integer> last7Days = taskService.getLast7DaysCompletion();
        model.addAttribute("last7Days", last7Days);
        List<String> last7DaysLabels = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            String day = today.minusDays(i).getDayOfWeek().toString(); // MONDAY, TUESDAY
            last7DaysLabels.add(day.substring(0,1).toUpperCase() + day.substring(1,3).toLowerCase()); // Mon, Tue
        }
        model.addAttribute("last7DaysLabels", last7DaysLabels); // pass to Thymeleaf
        return "weekly";
    }




}
