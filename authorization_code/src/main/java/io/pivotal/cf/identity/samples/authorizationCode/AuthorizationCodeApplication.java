package io.pivotal.cf.identity.samples.authorizationCode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AuthorizationCodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationCodeApplication.class, args);
	}

	@RequestMapping(value = "/")
	public String index() {
		return "Hello world!";
	}
}
