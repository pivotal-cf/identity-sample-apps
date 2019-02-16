package io.pivotal.cf.identity.samples.resourceServer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class ResourceServerController {

    List<String> inMemoryDataStore = new ArrayList<>();

    @GetMapping("/")
    public String get() {
        return inMemoryDataStore.toString();
    }

    @PostMapping("/")
    public String post() {
        inMemoryDataStore.add(new Date().toString());
        return inMemoryDataStore.toString();
    }
}
