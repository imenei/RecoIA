package dz.recoia.authservice.security;

import java.time.Instant;

/** Contenu d'un jeton valide. */
public record TokenClaims(long userId, String email, String role, Instant expiresAt) {
}
