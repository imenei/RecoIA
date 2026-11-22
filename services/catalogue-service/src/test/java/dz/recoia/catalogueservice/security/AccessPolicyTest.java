package dz.recoia.catalogueservice.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessPolicyTest {

    private static final String SECRET = "un-secret-de-test-assez-long-pour-hs256-0123456789";
    private final JwtCodec codec = new JwtCodec(SECRET, 120);
    private final AccessPolicy policy = new AccessPolicy(codec);

    private String bearer(String role) {
        return "Bearer " + codec.issue(7, "x@recoia.dz", role);
    }

    @Test
    void readingIsFreeForEveryone() {
        assertTrue(policy.check("GET", null).allowed());
        assertTrue(policy.check("HEAD", null).allowed());
        assertTrue(policy.check("OPTIONS", null).allowed());   // pre-requete CORS
    }

    @Test
    void writingWithoutTokenIs401() {
        for (String method : new String[] {"POST", "PUT", "PATCH", "DELETE"}) {
            AccessPolicy.Decision d = policy.check(method, null);
            assertTrue(!d.allowed(), method);
            assertEquals(401, d.status());
        }
    }

    @Test
    void studentCannotWrite() {
        AccessPolicy.Decision d = policy.check("DELETE", bearer("STUDENT"));
        assertTrue(!d.allowed());
        assertEquals(403, d.status());
    }

    @Test
    void adminCanWrite() {
        for (String method : new String[] {"POST", "PUT", "DELETE"}) {
            assertTrue(policy.check(method, bearer("ADMIN")).allowed(), method);
        }
    }

    @Test
    void garbageTokenIs401() {
        assertEquals(401, policy.check("POST", "Bearer abc.def.ghi").status());
        assertEquals(401, policy.check("POST", "Basic dXNlcjpwYXNz").status());
    }

    @Test
    void expiredAdminTokenIs401() {
        Clock past = Clock.fixed(Instant.now().minus(Duration.ofHours(3)), ZoneOffset.UTC);
        String old = "Bearer " + new JwtCodec(SECRET, 120, past).issue(1, "a@recoia.dz", "ADMIN");
        assertEquals(401, policy.check("PUT", old).status());
    }

    @Test
    void tokenSignedWithAnotherSecretIs401() {
        String foreign = "Bearer " + new JwtCodec("un-autre-secret-de-test-assez-long-0123456789ab", 120)
                .issue(1, "a@recoia.dz", "ADMIN");
        assertEquals(401, policy.check("POST", foreign).status());
    }
}
