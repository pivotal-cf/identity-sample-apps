package io.pivotal.identityService.samples.authcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws InterruptedException {
//		Thread.sleep(4000);
		SpringApplication.run(Application.class, args);
	}

}
