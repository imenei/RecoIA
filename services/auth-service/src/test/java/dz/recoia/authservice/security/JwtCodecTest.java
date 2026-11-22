package dz.recoia.authservice.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtCodecTest {

    private static final String SECRET = "un-secret-de-test-assez-long-pour-hs256-0123456789";
    private final JwtCodec codec = new JwtCodec(SECRET, 120);

    @Test
    void roundTripKeepsClaims() {
        String token = codec.issue(42, "imene@recoia.dz", "STUDENT");
        TokenClaims claims = codec.verify(token);

        assertEquals(42L, claims.userId());
        assertEquals("imene@recoia.dz", claims.email());
        assertEquals("STUDENT", claims.role());
        assertTrue(claims.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void specialCharactersInEmailSurvive() {
        String email = "a\"b\\c@recoia.dz";
        assertEquals(email, codec.verify(codec.issue(1, email, "ADMIN")).email());
    }

    @Test
    void tamperedPayloadIsRejected() {
        String token = codec.issue(1, "a@recoia.dz", "STUDENT");
        String[] p = token.split("\\.");
        String forged = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"1\",\"email\":\"a@recoia.dz\",\"role\":\"ADMIN\",\"iat\":1,\"exp\":9999999999}".getBytes());
        assertThrows(InvalidTokenException.class, () -> codec.verify(p[0] + "." + forged + "." + p[2]));
    }

    @Test
    void wrongSecretIsRejected() {
        String token = new JwtCodec("un-autre-secret-de-test-assez-long-0123456789ab", 120).issue(1, "a@b.dz", "STUDENT");
        assertThrows(InvalidTokenException.class, () -> codec.verify(token));
    }

    @Test
    void expiredTokenIsRejected() {
        Clock past = Clock.fixed(Instant.now().minus(Duration.ofHours(3)), ZoneOffset.UTC);
        String token = new JwtCodec(SECRET, 120, past).issue(1, "a@b.dz", "STUDENT");
        assertThrows(InvalidTokenException.class, () -> codec.verify(token));
    }

    @Test
    void garbageIsRejected() {
        assertThrows(InvalidTokenException.class, () -> codec.verify(null));
        assertThrows(InvalidTokenException.class, () -> codec.verify("abc"));
        assertThrows(InvalidTokenException.class, () -> codec.verify("a.b.c"));
    }

    @Test
    void algorithmNoneIsRejected() {
        String h = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes());
        String token = codec.issue(1, "a@b.dz", "ADMIN");
        String[] p = token.split("\\.");
        assertThrows(InvalidTokenException.class, () -> codec.verify(h + "." + p[1] + "."));
    }

    @Test
    void shortSecretIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new JwtCodec("trop-court", 60));
    }
}
