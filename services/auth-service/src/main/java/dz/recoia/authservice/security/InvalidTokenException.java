package dz.recoia.authservice.security;

/** Jeton absent, mal forme, falsifie ou expire. */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
