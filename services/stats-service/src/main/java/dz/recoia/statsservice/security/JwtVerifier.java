package dz.recoia.statsservice.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verification des jetons JWT (HS256) emis par auth-service, avec le MEME secret (JWT_SECRET).
 * Ne sait que verifier : l'emission reste dans auth-service.
 * La signature est controlee AVANT toute lecture du contenu.
 */
public final class JwtVerifier {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final Pattern SUB = Pattern.compile("\"sub\":\"(\\d+)\"");
    private static final Pattern ROLE = Pattern.compile("\"role\":\"([A-Za-z_]+)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\":(\\d+)");
    private static final Pattern ALG = Pattern.compile("\"alg\":\"HS256\"");

    private final byte[] key;
    private final Clock clock;

    public JwtVerifier(String secret) {
        this(secret, Clock.systemUTC());
    }

    JwtVerifier(String secret, Clock clock) {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("Le secret JWT doit faire au moins 32 caracteres");
        this.key = bytes;
        this.clock = clock;
    }

    public TokenClaims verify(String token) {
        if (token == null) throw new InvalidTokenException("Jeton manquant");
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) throw new InvalidTokenException("Jeton mal forme");

        String header;
        String payload;
        try {
            byte[] given = DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(hmac(parts[0] + "." + parts[1]), given)) {
                throw new InvalidTokenException("Signature invalide");
            }
            header = new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8);
            payload = new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Jeton mal forme");
        }
        if (!ALG.matcher(header).find()) throw new InvalidTokenException("Algorithme refuse");

        Matcher sub = SUB.matcher(payload);
        Matcher role = ROLE.matcher(payload);
        Matcher exp = EXP.matcher(payload);
        if (!sub.find() || !role.find() || !exp.find()) throw new InvalidTokenException("Contenu du jeton invalide");
        try {
            long expires = Long.parseLong(exp.group(1));
            if (expires <= clock.instant().getEpochSecond()) throw new InvalidTokenException("Jeton expire");
            return new TokenClaims(Long.parseLong(sub.group(1)), role.group(1), Instant.ofEpochSecond(expires));
        } catch (NumberFormatException e) {
            throw new InvalidTokenException("Contenu du jeton invalide");
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
