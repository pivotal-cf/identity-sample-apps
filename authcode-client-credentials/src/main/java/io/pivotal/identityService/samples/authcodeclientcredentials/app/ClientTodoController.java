package io.pivotal.identityService.samples.authcodeclientcredentials.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;

@Controller
@RequestMapping(value = "/client")
public class ClientTodoController {

    @Value("${RESOURCE_URL}")
    private String resourceServerUrl;

    private final TodoService todoService;

    public ClientTodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/todos")
    public String clientGetTodos(Model model, @RegisteredOAuth2AuthorizedClient("ssoclientcredentials") OAuth2AuthorizedClient authorizedClient) {
        if (resourceServerUrl.equals("https://resource-server-sample.<your-domain>.com")) {
            model.addAttribute("header", "Warning: You need to configure the Resource Server sample application");
            model.addAttribute("displayWarning", true);
            return "configure_warning";
        }

        try {
            model.addAttribute("todoList", todoService.getAll(authorizedClient));
        } catch (WebClientResponseException error) {
            model.addAttribute("error", error);
            model.addAttribute("todoList", new ArrayList<Todo>());
        }

        model.addAttribute("todo", new TodoRequest());
        return "client-todos";
    }

    @PostMapping("/todos")
    public String clientCreateTodo(@ModelAttribute TodoRequest body, @RegisteredOAuth2AuthorizedClient("ssoclientcredentials") OAuth2AuthorizedClient authorizedClient) {
        todoService.create(body, authorizedClient);
        return "redirect:/client/todos";
    }

    @DeleteMapping("/todos/{id}")
    public String clientDeleteTodo(@PathVariable String id, @RegisteredOAuth2AuthorizedClient("ssoclientcredentials") OAuth2AuthorizedClient authorizedClient) {
        todoService.delete(id, authorizedClient);
        return "redirect:/client/todos";
    }
}
