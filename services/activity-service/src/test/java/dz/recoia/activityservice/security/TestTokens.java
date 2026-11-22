package dz.recoia.activityservice.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Emet des jetons exactement comme le JwtCodec de auth-service (meme format, meme signature). */
final class TestTokens {
    private static final Base64.Encoder ENC = Base64.getUrlEncoder().withoutPadding();

    static String issue(String secret, long userId, String email, String role, long iat, long exp) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payload = "{\"sub\":\"" + userId + "\",\"email\":\"" + email + "\",\"role\":\"" + role
                + "\",\"iat\":" + iat + ",\"exp\":" + exp + "}";
        String unsigned = enc(header) + "." + enc(payload);
        return unsigned + "." + ENC.encodeToString(hmac(secret, unsigned));
    }

    private static String enc(String s) { return ENC.encodeToString(s.getBytes(StandardCharsets.UTF_8)); }

    private static byte[] hmac(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
