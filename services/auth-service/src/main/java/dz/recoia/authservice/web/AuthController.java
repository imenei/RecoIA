package dz.recoia.authservice.web;

import dz.recoia.authservice.security.TokenClaims;
import dz.recoia.authservice.service.AuthService;
import dz.recoia.authservice.web.AuthDtos.AuthResponse;
import dz.recoia.authservice.web.AuthDtos.LoginRequest;
import dz.recoia.authservice.web.AuthDtos.RegisterRequest;
import dz.recoia.authservice.web.AuthDtos.UpdateProfileRequest;
import dz.recoia.authservice.web.AuthDtos.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API publique d'authentification (acces via la gateway : /api/auth/**). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;
    private final TokenReader tokens;

    public AuthController(AuthService service, TokenReader tokens) {
        this.service = service;
        this.tokens = tokens;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    /** Les jetons sont sans etat : la deconnexion consiste, pour le client, a oublier son jeton. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        TokenClaims claims = tokens.require(authorization);
        return service.me(claims.userId());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@RequestHeader(name = "Authorization", required = false) String authorization,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        TokenClaims claims = tokens.require(authorization);
        return service.updateProfile(claims.userId(), request);
    }

    /** Liste de tous les comptes : administrateur uniquement. */
    @GetMapping("/users")
    public List<UserResponse> users(@RequestHeader(name = "Authorization", required = false) String authorization) {
        tokens.requireAdmin(authorization);
        return service.all();
    }
}
