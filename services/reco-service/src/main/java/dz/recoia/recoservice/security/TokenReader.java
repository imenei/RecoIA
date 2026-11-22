package dz.recoia.recoservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Lit l'en-tete Authorization : l'identite de l'etudiant vient TOUJOURS du jeton, jamais de l'URL. */
@Component
public class TokenReader {

    private final JwtVerifier verifier;

    public TokenReader(@Value("${app.jwt.secret:${JWT_SECRET:change-me-change-me-change-me-change-me-32b}}") String secret) {
        this.verifier = new JwtVerifier(secret);
    }

    public TokenClaims require(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Connexion requise");
        }
        try {
            return verifier.verify(authorization.substring(7).trim());
        } catch (InvalidTokenException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalide ou expirée");
        }
    }
}
