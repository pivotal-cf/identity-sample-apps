package com.vmware.tanzu.appsso.samples.env;

import java.util.HashMap;
import java.util.Map;

import io.pivotal.cfenv.core.CfEnv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvController {

	private final String port;
	private final String memoryLimit;
	private final String cfInstanceIndex;
	private final String cfInstanceAddress;
	private final String vcapServices;
	private final String clientId;
	private final String clientSecret;
	private final String issuerUri;

	@Autowired
	private CfEnv cfEnv;

	public EnvController(
			// Injects the PORT environment variable, defaults to NOT SET
			@Value("${port:NOT SET}") String port,
			// Injects the MEMORY_LIMIT environment variable, defaults to NOT SET
			@Value("${memory.limit:NOT SET}") String memoryLimit,
			// Injects the CF_INSTANCE_INDEX environment variable, defaults to NOT SET
			@Value("${cf.instance.index:NOT SET}") String cfInstanceIndex,
			// Injects the CF_INSTANCE_ADDR environment variable, defaults to NOT SET
			@Value("${cf.instance.addr:NOT SET}") String cfInstanceAddress,
			@Value("${vcap.services:NOT SET}") String vcapServices,
			@Value("${app.client.id:NOT SET}") String clientId,
			@Value("${app.client.secret:NOT SET}") String clientSecret,
			@Value("${app.client.issuer_uri:NOT SET}") String issuerUri
	) {
		this.port = port;
		this.memoryLimit = memoryLimit;
		this.cfInstanceIndex = cfInstanceIndex;
		this.cfInstanceAddress = cfInstanceAddress;
		this.vcapServices = vcapServices;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.issuerUri = issuerUri;
	}

	// Maps GET requests to /
	@GetMapping("/")
	public Map<String, String> getEnv() {
		Map<String, String> result = new HashMap<>();
		result.put("PORT", port);
		result.put("MEMORY_LIMIT", memoryLimit);
		result.put("CF_INSTANCE_INDEX", cfInstanceIndex);
		result.put("CF_INSTANCE_ADDR", cfInstanceAddress);
		result.put("client_id", clientId);
		result.put("client_secret", clientSecret);
		result.put("issuer_uri", issuerUri);
		return result;
	}

	@GetMapping("/vcap")
	public Map<String, String> getVcapEnv() {
		Map<String, String> result = new HashMap<>();
		result.put("VCAP_SERVICES", vcapServices);
		return result;
	}


	@GetMapping("/env")
	public Map<String, Object> getSystemEnv() {

		Map<String, Object> result = new HashMap<>();
		System.getenv().forEach(
				(k, v) -> {
					result.put(k, v);
				}
		);
		return result;
	}
}
