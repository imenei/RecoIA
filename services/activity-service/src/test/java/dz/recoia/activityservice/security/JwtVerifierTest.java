package dz.recoia.activityservice.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class JwtVerifierTest {

    private static final String SECRET = "change-me-change-me-change-me-change-me-32b";
    private static final long NOW = 1_800_000_000L;
    private final Clock clock = Clock.fixed(Instant.ofEpochSecond(NOW), ZoneOffset.UTC);
    private final JwtVerifier verifier = new JwtVerifier(SECRET, clock);

    private String token(String secret, String role, long exp) {
        return TestTokens.issue(secret, 42, "etudiant01@recoia.dz", role, NOW - 60, exp);
    }

    @Test
    void jetonValide_donneIdentifiantEtRole() {
        TokenClaims c = verifier.verify(token(SECRET, "STUDENT", NOW + 3600));
        assertEquals(42, c.userId());
        assertEquals("STUDENT", c.role());
    }

    @Test
    void jetonAdmin_estLuAvecSonRole() {
        assertEquals("ADMIN", verifier.verify(token(SECRET, "ADMIN", NOW + 3600)).role());
    }

    @Test
    void jetonExpire_estRefuse() {
        assertThrows(InvalidTokenException.class, () -> verifier.verify(token(SECRET, "STUDENT", NOW - 1)));
    }

    @Test
    void autreSecret_estRefuse() {
        String other = "another-secret-another-secret-another-secret";
        assertThrows(InvalidTokenException.class, () -> verifier.verify(token(other, "STUDENT", NOW + 3600)));
    }

    @Test
    void contenuFalsifie_estRefuse() {
        String[] p = token(SECRET, "STUDENT", NOW + 3600).split("\\.");
        String forged = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"1\",\"email\":\"x@y.z\",\"role\":\"ADMIN\",\"iat\":1,\"exp\":9999999999}".getBytes());
        assertThrows(InvalidTokenException.class, () -> verifier.verify(p[0] + "." + forged + "." + p[2]));
    }

    @Test
    void jetonAbsentOuMalForme_estRefuse() {
        assertThrows(InvalidTokenException.class, () -> verifier.verify(null));
        assertThrows(InvalidTokenException.class, () -> verifier.verify("abc"));
        assertThrows(InvalidTokenException.class, () -> verifier.verify("a.b.c"));
    }

    @Test
    void secretTropCourt_estRefuse() {
        assertThrows(IllegalArgumentException.class, () -> new JwtVerifier("court"));
    }
}
