package dz.recoia.statsservice.security;

import java.time.Instant;

/** Contenu utile d'un jeton valide. */
public record TokenClaims(long userId, String role, Instant expiresAt) {
}
