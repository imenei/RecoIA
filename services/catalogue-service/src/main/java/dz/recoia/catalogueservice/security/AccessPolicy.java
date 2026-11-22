package dz.recoia.catalogueservice.security;

import java.util.Set;

/**
 * Regle d'acces du catalogue :
 * - lecture (GET, HEAD, OPTIONS) : libre ;
 * - ajout, modification, suppression, import, envoi d'image (POST, PUT, PATCH, DELETE) : administrateur uniquement.
 *
 * Classe sans dependance a Spring : elle se teste directement.
 */
public final class AccessPolicy {

    public record Decision(boolean allowed, int status, String message) {
        static Decision allow() { return new Decision(true, 200, ""); }
        static Decision deny(int status, String message) { return new Decision(false, status, message); }
    }

    private static final Set<String> READ_ONLY = Set.of("GET", "HEAD", "OPTIONS");

    private final JwtCodec jwt;

    public AccessPolicy(JwtCodec jwt) {
        this.jwt = jwt;
    }

    public Decision check(String httpMethod, String authorizationHeader) {
        if (READ_ONLY.contains(httpMethod.toUpperCase())) return Decision.allow();

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Decision.deny(401, "Connexion requise");
        }
        TokenClaims claims;
        try {
            claims = jwt.verify(authorizationHeader.substring(7).trim());
        } catch (InvalidTokenException e) {
            return Decision.deny(401, "Session invalide ou expirée");
        }
        if (!"ADMIN".equals(claims.role())) {
            return Decision.deny(403, "Réservé aux administrateurs");
        }
        return Decision.allow();
    }
}
