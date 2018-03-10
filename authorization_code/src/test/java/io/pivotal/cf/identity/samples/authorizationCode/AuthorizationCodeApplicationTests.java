package io.pivotal.cf.identity.samples.authorizationCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthorizationCodeApplicationTests {

	@Autowired
	private WebApplicationContext context;

	@Test
	public void loadsHelloWorld() {
		given()
			.webAppContextSetup(context)
			.when()
				.get("/")
			.then()
				.statusCode(200)
				.body(equalTo("Hello world!"));
	}

}
