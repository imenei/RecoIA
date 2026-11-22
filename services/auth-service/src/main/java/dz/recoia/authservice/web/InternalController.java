package dz.recoia.authservice.web;

import dz.recoia.authservice.service.AuthService;
import dz.recoia.authservice.web.AuthDtos.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints internes pour les autres microservices (activity-service, reco-service).
 * La gateway ne route pas /internal/** : ils ne sont pas joignables depuis le navigateur.
 */
@RestController
@RequestMapping("/internal/users")
public class InternalController {

    private final AuthService service;

    public InternalController(AuthService service) {
        this.service = service;
    }

    @GetMapping("/students")
    public List<UserResponse> students() {
        return service.students();
    }

    @GetMapping("/{id}")
    public UserResponse byId(@PathVariable("id") long id) {
        return service.byId(id);
    }
}
