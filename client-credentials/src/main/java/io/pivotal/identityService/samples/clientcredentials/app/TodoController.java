package io.pivotal.identityService.samples.clientcredentials.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;

@Controller
public class TodoController {

    @Value("${RESOURCE_URL}")
    private String resourceServerUrl;

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/todos")
    public String todo(Model model) {
        if (resourceServerUrl.equals("https://resource-server-sample.<your-domain>.com")) {
            model.addAttribute("header", "Warning: You need to configure the Resource Server sample application");
            model.addAttribute("displayWarning", true);
            return "configure_warning";
        }

        try {
            model.addAttribute("todoList", todoService.getAll());
        } catch (WebClientResponseException error) {
            model.addAttribute("error", error);
            model.addAttribute("todoList", new ArrayList<Todo>());
        }

        model.addAttribute("todo", new TodoRequest());
        return "todos";
    }

    @PostMapping("/todos")
    public String create(@ModelAttribute TodoRequest body) {
        todoService.create(body);
        return "redirect:/todos";
    }

    @DeleteMapping("/todos/{id}")
    public String delete(@PathVariable String id) {
        todoService.delete(id);
        return "redirect:/todos";
    }
}
