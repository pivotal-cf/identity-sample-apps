package org.cloudfoundry.identity.samples.authcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
public class TodoService {
    private RestTemplate restTemplate;

    @Autowired
    public TodoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Todo> getAll() {
        ResponseEntity<List<Todo>> response = restTemplate.exchange("https://resource-server-sample.id-service.cf-app.com/todo",
            GET, null, new ParameterizedTypeReference<List<Todo>>() {});
        return response.getBody();
    }

    public Todo create(Todo todo) {
        HttpEntity<Todo> entity = new HttpEntity<>(todo);
        ResponseEntity<Todo> response = restTemplate.exchange("https://resource-server-sample.id-service.cf-app.com/todo",
            POST, entity, Todo.class);
        return response.getBody();
    }
}

class Todo {
    private String id;
    private String todo;
    private Date created;
    private Date updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
