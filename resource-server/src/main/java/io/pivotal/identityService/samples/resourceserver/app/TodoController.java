package io.pivotal.identityService.samples.resourceserver.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/todos")
public class TodoController {
    private final TodoRepository todos;

    public TodoController(TodoRepository todos) {
        this.todos = todos;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_todo.read')")
    public List<Todo> list() {
        return todos.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCOPE_todo.write')")
    public Todo create(@RequestBody Todo todo) {
        return todos.create(todo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_todo.write')")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        try {
            todos.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
