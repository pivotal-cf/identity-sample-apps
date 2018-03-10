package io.pivotal.cf.identity.samples.authorizationCode.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {
    @RequestMapping(value = "/")
    public String show() {
        return "Hello world!";
    }
}
