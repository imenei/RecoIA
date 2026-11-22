package dz.recoia.statsservice.security;

/** Jeton absent, mal forme, falsifie ou expire. */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
