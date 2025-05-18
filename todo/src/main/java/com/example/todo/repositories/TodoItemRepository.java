package com.example.todo.repositories;

import java.util.List;

import com.example.todo.models.TodoItem;
import com.example.todo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    List<TodoItem> findByUserOrderByCreatedAtDesc(User user);
    List<TodoItem> findByUserOrderByCreatedAtAsc(User user);
    List<TodoItem> findByUserOrderByPriorityDesc(User user);
    void deleteAllByUser(User user);

}
