package dz.recoia.authservice.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Creation et verification de jetons JWT signes en HS256 (HMAC-SHA256), sans bibliotheque externe.
 *
 * Format : base64url(en-tete) . base64url(contenu) . base64url(signature)
 * Contenu : sub (id utilisateur), email, role, iat, exp.
 *
 * Le meme secret doit etre configure dans l'api-gateway pour qu'il puisse verifier les jetons.
 */
public final class JwtCodec {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    private final byte[] key;
    private final long ttlSeconds;
    private final Clock clock;

    public JwtCodec(String secret, long ttlMinutes) {
        this(secret, ttlMinutes, Clock.systemUTC());
    }

    JwtCodec(String secret, long ttlMinutes, Clock clock) {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("Le secret JWT doit faire au moins 32 caracteres");
        this.key = bytes;
        this.ttlSeconds = ttlMinutes * 60;
        this.clock = clock;
    }

    public String issue(long userId, String email, String role) {
        long now = clock.instant().getEpochSecond();
        String payload = "{\"sub\":" + quote(String.valueOf(userId)) + ",\"email\":" + quote(email)
                + ",\"role\":" + quote(role) + ",\"iat\":" + now + ",\"exp\":" + (now + ttlSeconds) + "}";
        String unsigned = encode(HEADER) + "." + encode(payload);
        return unsigned + "." + ENCODER.encodeToString(hmac(unsigned));
    }

    public TokenClaims verify(String token) {
        if (token == null) throw new InvalidTokenException("Jeton manquant");
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) throw new InvalidTokenException("Jeton mal forme");

        byte[] given;
        Map<String, String> header;
        Map<String, String> claims;
        try {
            given = DECODER.decode(parts[2]);
            header = FlatJson.parse(decode(parts[0]));
            // verifier la signature AVANT de lire le contenu
            byte[] expected = hmac(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected, given)) throw new InvalidTokenException("Signature invalide");
            claims = FlatJson.parse(decode(parts[1]));
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Jeton mal forme");
        }
        if (!"HS256".equals(header.get("alg"))) throw new InvalidTokenException("Algorithme refuse");

        try {
            long exp = Long.parseLong(claims.get("exp"));
            if (exp <= clock.instant().getEpochSecond()) throw new InvalidTokenException("Jeton expire");
            return new TokenClaims(Long.parseLong(claims.get("sub")), claims.get("email"), claims.get("role"),
                    Instant.ofEpochSecond(exp));
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("Contenu du jeton invalide");
        }
    }

    // ------------------------------------------------------------------ utilitaires

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String encode(String s) {
        return ENCODER.encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String s) {
        return new String(DECODER.decode(s), StandardCharsets.UTF_8);
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"').toString();
    }

    /** Lecteur JSON minimal pour un objet plat : valeurs texte ou nombres. */
    static final class FlatJson {
        private final String s;
        private int i;

        private FlatJson(String s) { this.s = s; }

        static Map<String, String> parse(String json) {
            return new FlatJson(json).object();
        }

        private Map<String, String> object() {
            Map<String, String> out = new HashMap<>();
            skip();
            expect('{');
            skip();
            if (peek() == '}') { i++; return out; }
            while (true) {
                skip();
                String key = string();
                skip();
                expect(':');
                skip();
                out.put(key, peek() == '"' ? string() : bare());
                skip();
                if (peek() == ',') { i++; continue; }
                expect('}');
                return out;
            }
        }

        private String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (i >= s.length()) throw new IllegalArgumentException("chaine non terminee");
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                if (i >= s.length()) throw new IllegalArgumentException("echappement invalide");
                char e = s.charAt(i++);
                switch (e) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 > s.length()) throw new IllegalArgumentException("echappement invalide");
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                    }
                    default -> sb.append(e);   // \" \\ \/
                }
            }
        }

        private String bare() {
            int start = i;
            while (i < s.length() && s.charAt(i) != ',' && s.charAt(i) != '}') i++;
            return s.substring(start, i).trim();
        }

        private char peek() { return i < s.length() ? s.charAt(i) : '\0'; }
        private void skip() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        private void expect(char c) {
            if (peek() != c) throw new IllegalArgumentException("attendu : " + c);
            i++;
        }
    }
}
