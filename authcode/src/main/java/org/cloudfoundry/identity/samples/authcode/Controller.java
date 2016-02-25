package org.cloudfoundry.identity.samples.authcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@org.springframework.stereotype.Controller
public class Controller {
    @Autowired
    private TodoService todoService;

    @RequestMapping(value = "/todo", method = GET)
    public String list(Model model) {
        model.addAttribute("todoList", todoService.getAll());
        model.addAttribute("todo", new Todo());
        return "todo";
    }

    @RequestMapping(value = "/new_todo", method = POST)
    public String create(@ModelAttribute Todo body) {
        todoService.create(body);
        return "redirect:/todo";
    }
}
