package org.cloudfoundry.identity.samples.authcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@org.springframework.stereotype.Controller
public class Controller {
    @Autowired
    private TodoService todoService;
    @Value("${resourceServerUrl}")
    private String resourceServerUrl;

    @RequestMapping(value = "/todo", method = GET)
    public String list(Model model) {
        if (resourceServerUrl.equals("https://resource-server.domain")) {
            model.addAttribute("header", "Warning: You need to configure the Resource Server sample application");
            model.addAttribute("displayWarning", true);
            return "configure_warning";
        }
        model.addAttribute("todoList", todoService.getAll());
        model.addAttribute("todo", new Todo());
        return "todo";
    }

    @RequestMapping(value = "/new_todo", method = POST)
    public String create(@ModelAttribute Todo body) {
        todoService.create(body);
        return "redirect:/todo";
    }

    @RequestMapping(value = "/todo/{id}/delete", method = POST)
    public String delete(@PathVariable String id) {
        todoService.delete(id);
        return "redirect:/todo";
    }
}
