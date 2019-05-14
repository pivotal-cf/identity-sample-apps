package io.pivotal.identityService.samples.clientcredentials.app;

public class Todo {
    private String id;
    private String task;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                "id='" + id + '\'' +
                ", task='" + task + '\'' +
                '}';
    }
}
