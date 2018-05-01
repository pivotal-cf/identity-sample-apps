package io.pivotal.cf.identity.samples.resourceServer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AcmeController {

    @RequestMapping("/acme/abc")
    public String abc() {
        return "Verified that token contains acme.abc";
    }

    @RequestMapping("/acme/xyz")
    public String xyz() {
        return "Verified that token contains acme.xyz";
    }
}
