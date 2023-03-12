package com.vmware.tanzu.appsso.samples.env;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.cfenv.core.CfEnv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class EnvControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CfEnv cfEnv;


	@Test
	void hasIssuerUriInVcapServices() throws Exception {
		mockMvc.perform(get("/", 42L)
						.contentType("application/json"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.issuer_uri", equalTo("https://some.custom.issuer.uri/")));
	}


}