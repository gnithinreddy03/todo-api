package com.example.todo_api.models;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TodoItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Boolean isComplete;
	private String description;
	private Instant createdAt;
	private Instant updatedAt;

	public TodoItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TodoItem(Long id, Boolean isComplete, String description, Instant createdAt, Instant updatedAt) {
		super();
		this.id = id;
		this.isComplete = isComplete;
		this.description = description;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsComplete() {
		return isComplete;
	}

	public void setIsComplete(Boolean isComplete) {
		this.isComplete = isComplete;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "TodoItem [id=" + id + ", isComplete=" + isComplete + ", description=" + description + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}

}
