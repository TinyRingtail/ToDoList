package com.example.todo.controllers;

import com.example.todo.models.TodoItem;
import com.example.todo.models.User;
import com.example.todo.repositories.TodoItemRepository;
import com.example.todo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ToDoController {

    private final TodoItemRepository todoItemRepository;
    private final UserRepository userRepository;

    public ToDoController(TodoItemRepository todoItemRepository, UserRepository userRepository) {
        this.todoItemRepository = todoItemRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "sort", required = false) String sort, Model model) {
        User user = getCurrentUser();
        List<TodoItem> allTasks;

        switch (sort) {
            case "oldest" -> allTasks = todoItemRepository.findByUserOrderByCreatedAtAsc(user);
            case "newest" -> allTasks = todoItemRepository.findByUserOrderByCreatedAtDesc(user);
            case "priority" -> allTasks = todoItemRepository.findByUserOrderByPriorityDesc(user);
            case null, default -> {
                sort = "newest";
                allTasks = todoItemRepository.findByUserOrderByCreatedAtDesc(user);
            }
        }

        model.addAttribute("allTasks", allTasks);
        model.addAttribute("NewTask", new TodoItem());
        model.addAttribute("sort", sort);

        return "index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute TodoItem todoItem) {
        User currentUser = getCurrentUser();
        todoItem.setUser(currentUser);  // Ось цей рядок важливий!
        todoItemRepository.save(todoItem);
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        todoItemRepository.deleteById(id);
        return "redirect:/";
    }

    @PostMapping("/deleteAll")
    @Transactional
    public String deleteAllItems() {
        User currentUser = getCurrentUser();  // Метод для отримання поточного користувача
        todoItemRepository.deleteAllByUser(currentUser);
        return "redirect:/";
    }

    @PostMapping("/toggle/{id}")
    public String toggleCompleted(@PathVariable("id") Long id) {
        TodoItem item = todoItemRepository.findById(id).orElse(null);
        User currentUser = getCurrentUser();
        if (item != null && item.getUser() != null &&
                item.getUser().getUsername().equals(currentUser.getUsername())) {
            item.setCompleted(!item.isCompleted());
            todoItemRepository.save(item);
        }
        return "redirect:/";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, @RequestParam("title") String newTitle) {
        TodoItem item = todoItemRepository.findById(id).orElse(null);
        if (item != null && item.getUser().getUsername().equals(getCurrentUser().getUsername())) {
            item.setTitle(newTitle);
            todoItemRepository.save(item);
        }
        return "redirect:/";
    }
}
