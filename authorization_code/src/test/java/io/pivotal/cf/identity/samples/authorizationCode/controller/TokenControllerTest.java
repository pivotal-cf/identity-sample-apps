package io.pivotal.cf.identity.samples.authorizationCode.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TokenControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Test
    public void show() {
        given()
                .webAppContextSetup(context)
            .when()
                .get("/")
            .then()
                .statusCode(200)
                .body(equalTo("Hello world!"));
    }
}