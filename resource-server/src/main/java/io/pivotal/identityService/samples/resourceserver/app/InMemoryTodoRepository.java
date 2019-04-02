package io.pivotal.identityService.samples.resourceserver.app;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryTodoRepository implements TodoRepository {
    private LinkedHashMap<UUID, Todo> db;

    public InMemoryTodoRepository() {
        this.db = new LinkedHashMap<>();

        // Seed data
        this.create(new Todo("seed-task-1"));
        this.create(new Todo("seed-task-2"));
        this.create(new Todo("seed-task-3"));
    }

    @Override
    public Todo create(Todo todo) {
        db.put(todo.getId(), todo);
        return todo;
    }

    @Override
    public List<Todo> findAll() {
        return new ArrayList<>(db.values());
    }

    @Override
    public void deleteById(UUID id) {
        Todo todo = db.remove(id);
        if (todo == null) throw new IllegalStateException(String.format("Todo with id=%s not found", id));
    }
}
