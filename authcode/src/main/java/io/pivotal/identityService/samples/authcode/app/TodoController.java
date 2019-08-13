package io.pivotal.identityService.samples.authcode.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Controller
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/todos")
    public String todo(HttpServletRequest request, Model model) {
        try {
            model.addAttribute("todoList", todoService.getAll(getAuthorizationHeader(request)));
        } catch (WebClientResponseException error) {
            model.addAttribute("error", error);
            model.addAttribute("todoList", new ArrayList<Todo>());
        }

        model.addAttribute("todo", new TodoRequest());
        return "todos";
    }

    @PostMapping("/todos")
    public String create(HttpServletRequest request, @ModelAttribute TodoRequest body) {
        todoService.create(getAuthorizationHeader(request), body);
        return "redirect:/todos";
    }

    @DeleteMapping("/todos/{id}")
    public String delete(HttpServletRequest request, @PathVariable String id) {
        todoService.delete(getAuthorizationHeader(request), id);
        return "redirect:/todos";
    }

    private String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}
