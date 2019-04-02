package io.pivotal.identityService.samples.resourceserver.app;

import java.util.List;
import java.util.UUID;

public interface TodoRepository {
    Todo create(Todo todo);
    List<Todo> findAll();
    void deleteById(UUID id);
}
