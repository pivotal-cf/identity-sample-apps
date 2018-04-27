package io.pivotal.cf.identity.samples.resourceServer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodosController {

    @RequestMapping("/todos/read")
    public String read() {
        return "Verified that token contains todo.read";
    }

    @RequestMapping("/todos/write")
    public String write() {
        return "Verified that token contains todo.read and todo.write";
    }
}
