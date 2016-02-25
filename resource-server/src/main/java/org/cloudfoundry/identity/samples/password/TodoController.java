package org.cloudfoundry.identity.samples.password;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/todo")
public class TodoController {

	private Map<String, Todo> todoDB = new HashMap<>();

	@RequestMapping(method = GET)
	@ResponseBody
	@PreAuthorize("#oauth2.hasScope('todo.read')")
	public Collection<Todo> list() {
		return todoDB.values();
	}

	@RequestMapping(method = POST)
	@ResponseBody
	@PreAuthorize("#oauth2.hasScope('todo.write')")
	public ResponseEntity<?> create(@RequestBody Todo body) {
		String id = UUID.randomUUID().toString();
		body.setId(id);
		body.setCreated(new Date());
		body.setUpdated(new Date());
		todoDB.put(id, body);
		return new ResponseEntity<>(body, CREATED);
	}

	@RequestMapping(value = "/{todoId}", method = PUT)
	@PreAuthorize("#oauth2.hasScope('todo.write')")
	public ResponseEntity<?> update(@PathVariable String todoId, @RequestBody Todo body) {
		Todo saved = todoDB.get(todoId);
		saved.setTodo(body.getTodo());
		body.setUpdated(new Date());
		todoDB.put(todoId, saved);
		return new ResponseEntity<>(OK);
	}

	@RequestMapping(value = "/{todoId}", method = DELETE)
	@PreAuthorize("#oauth2.hasScope('todo.write')")
	public ResponseEntity<?> delete(@PathVariable String todoId) {
		if (todoId == null || todoDB.get(todoId) == null) {
			throw new HttpClientErrorException(NOT_FOUND, "Entry with id(" + todoId + ") not found.");
		}
		todoDB.remove(todoId);
		return new ResponseEntity<>(OK);
	}

	@ExceptionHandler
	public ResponseEntity<?> handleError(HttpClientErrorException exception) {
		Map<String, String> response = Collections.singletonMap("message", exception.getMessage());
		return new ResponseEntity<>(response, exception.getStatusCode());
	}
}