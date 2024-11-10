package com.example.todo_api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo_api.models.TodoItem;
import com.example.todo_api.services.TodoItemService;

@RestController // Replacing @Controller with @RestController for API-based responses
@RequestMapping("/api") // Base URL for your API
public class HomeController {
	@Autowired
	private TodoItemService todoItemService;

	@GetMapping("/todos")
	public ResponseEntity<List<TodoItem>> getAllTodos() {
		List<TodoItem> todoItems = (List<TodoItem>) todoItemService.getAll();
		return new ResponseEntity<>(todoItems, HttpStatus.OK);
	}

}