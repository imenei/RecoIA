package dz.recoia.authservice.service;

import dz.recoia.authservice.model.Role;
import dz.recoia.authservice.model.User;
import dz.recoia.authservice.model.UserRepository;
import dz.recoia.authservice.security.JwtCodec;
import dz.recoia.authservice.web.AuthDtos.AuthResponse;
import dz.recoia.authservice.web.AuthDtos.LoginRequest;
import dz.recoia.authservice.web.AuthDtos.RegisterRequest;
import dz.recoia.authservice.web.AuthDtos.UpdateProfileRequest;
import dz.recoia.authservice.web.AuthDtos.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> TRACKS =
            Set.of("Licence", "Master", "Doctorat", "Formation courte", "Certifications");
    private static final Set<String> LANGUAGES = Set.of("FR", "EN", "ANY");
    private static final Set<String> DURATIONS = Set.of("SHORT", "MEDIUM", "LONG", "ANY");

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtCodec jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtCodec jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalize(req.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }
        User u = new User();
        u.setEmail(email);
        u.setFullName(req.fullName().trim());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(Role.STUDENT);                       // l'inscription publique ne cree jamais d'admin
        u.setInterests(Interests.clean(req.interests()));
        u.setTrack(pick(req.track(), TRACKS, false));
        u.setPreferredLanguage(pick(req.language(), LANGUAGES, true));
        u.setPreferredDuration(pick(req.duration(), DURATIONS, true));
        return authResponse(users.save(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = users.findByEmailIgnoreCase(normalize(req.email())).orElse(null);
        if (u == null || !encoder.matches(req.password(), u.getPasswordHash())) {
            // meme message dans les deux cas : on ne revele pas si l'email existe
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        }
        return authResponse(u);
    }

    @Transactional(readOnly = true)
    public UserResponse me(long userId) {
        return UserResponse.from(find(userId));
    }

    @Transactional
    public UserResponse updateProfile(long userId, UpdateProfileRequest req) {
        User u = find(userId);
        if (req.fullName() != null && !req.fullName().isBlank()) u.setFullName(req.fullName().trim());
        if (req.interests() != null) u.setInterests(Interests.clean(req.interests()));
        if (req.track() != null) u.setTrack(pick(req.track(), TRACKS, false));
        if (req.language() != null) u.setPreferredLanguage(pick(req.language(), LANGUAGES, true));
        if (req.duration() != null) u.setPreferredDuration(pick(req.duration(), DURATIONS, true));
        return UserResponse.from(users.save(u));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> all() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> students() {
        return users.findByRole(Role.STUDENT).stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse byId(long id) {
        return UserResponse.from(find(id));
    }

    private User find(long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private AuthResponse authResponse(User u) {
        return new AuthResponse(jwt.issue(u.getId(), u.getEmail(), u.getRole().name()), UserResponse.from(u));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Renvoie la valeur si elle fait partie de la liste autorisee, sinon null (champ facultatif). */
    private static String pick(String value, Set<String> allowed, boolean upper) {
        if (value == null) return null;
        String t = value.trim();
        if (upper) t = t.toUpperCase(Locale.ROOT);
        return allowed.contains(t) ? t : null;
    }
}