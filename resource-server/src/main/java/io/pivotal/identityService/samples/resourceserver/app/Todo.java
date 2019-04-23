package io.pivotal.identityService.samples.resourceserver.app;

import java.util.UUID;

public class Todo {
    private UUID id;
    private String task;

    public Todo() {
        this.id = UUID.randomUUID();
    }

    public Todo(String task) {
        this.id = UUID.randomUUID();
        this.task = task;
    }

    public UUID getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", task='" + task + '\'' +
                '}';
    }
}
