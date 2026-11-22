package dz.recoia.authservice.web;

import dz.recoia.authservice.model.Role;
import dz.recoia.authservice.security.InvalidTokenException;
import dz.recoia.authservice.security.JwtCodec;
import dz.recoia.authservice.security.TokenClaims;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Lit l'en-tete "Authorization: Bearer <jeton>" et applique les regles d'acces. */
@Component
public class TokenReader {

    private final JwtCodec jwt;

    public TokenReader(JwtCodec jwt) {
        this.jwt = jwt;
    }

    public TokenClaims require(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Connexion requise");
        }
        try {
            return jwt.verify(authorization.substring(7).trim());
        } catch (InvalidTokenException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalide ou expirée");
        }
    }

    public TokenClaims requireAdmin(String authorization) {
        TokenClaims claims = require(authorization);
        if (!Role.ADMIN.name().equals(claims.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Réservé aux administrateurs");
        }
        return claims;
    }
}
