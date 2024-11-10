package com.example.todo_api.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo_api.models.TodoItem;
import com.example.todo_api.services.TodoItemService;

import jakarta.validation.Valid;

@RestController // Use @RestController to return JSON responses
@RequestMapping("/api") // API base path
public class TodoFormController {

	@Autowired
	private TodoItemService todoItemService;

	// POST /api/todo - Create a new TodoItem
	@PostMapping("/todo")
	public ResponseEntity<TodoItem> createTodoItem(@RequestBody @Valid TodoItem todoItem) {
		TodoItem createdItem = todoItemService.save(todoItem);
		return new ResponseEntity<>(createdItem, HttpStatus.CREATED); // 201 Created
	}

	// GET /api/todo/{id} - Get a specific TodoItem by ID
	@GetMapping("/todo/{id}")
	public ResponseEntity<TodoItem> getTodoItemById(@PathVariable("id") Long id) {
		Optional<TodoItem> todoItem = todoItemService.getById(id);
		if (todoItem.isPresent()) {
			return new ResponseEntity<>(todoItem.get(), HttpStatus.OK); // 200 OK with the todo item
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
	}

	// DELETE /api/todo/{id} - Delete a TodoItem by ID
	@DeleteMapping("/todo/{id}")
	public ResponseEntity<Void> deleteTodoItem(@PathVariable("id") Long id) {
		Optional<TodoItem> todoItem = todoItemService.getById(id);
		if (todoItem.isPresent()) {
			todoItemService.delete(todoItem.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content (success)
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found (if todo not found)
	}

	// PUT /api/todo/{id} - Update an existing TodoItem
	@PutMapping("/todo/{id}")
	public ResponseEntity<TodoItem> updateTodoItem(@PathVariable("id") Long id, @RequestBody @Valid TodoItem todoItem) {
		Optional<TodoItem> existingItem = todoItemService.getById(id);
		if (existingItem.isPresent()) {
			TodoItem updatedItem = existingItem.get();
			updatedItem.setDescription(todoItem.getDescription());
			updatedItem.setIsComplete(todoItem.getIsComplete());
			TodoItem savedItem = todoItemService.save(updatedItem);
			return new ResponseEntity<>(savedItem, HttpStatus.OK); // 200 OK with updated item
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found (if todo not found)
	}
}
