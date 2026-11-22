package dz.recoia.catalogueservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Objets servant a verifier les jetons.
 *
 * Le secret est le meme que celui de auth-service : variable d'environnement JWT_SECRET,
 * sinon la valeur de developpement par defaut (identique des deux cotes).
 */
@Configuration
public class SecurityBeans {

    @Bean
    JwtCodec jwtCodec(@Value("${app.jwt.secret:${JWT_SECRET:change-me-change-me-change-me-change-me-32b}}") String secret) {
        return new JwtCodec(secret, 120);   // la duree ne sert qu'a l'emission : ici on ne fait que verifier
    }

    @Bean
    AccessPolicy accessPolicy(JwtCodec jwt) {
        return new AccessPolicy(jwt);
    }

    @Bean
    AdminGuardInterceptor adminGuardInterceptor(AccessPolicy policy) {
        return new AdminGuardInterceptor(policy);
    }
}
