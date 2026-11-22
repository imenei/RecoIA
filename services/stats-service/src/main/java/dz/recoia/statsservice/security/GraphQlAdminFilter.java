package dz.recoia.statsservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Les statistiques sont reservees a l'administrateur : controle du jeton avant toute requete GraphQL. */
@Component
public class GraphQlAdminFilter extends OncePerRequestFilter {

    private final JwtVerifier verifier;

    public GraphQlAdminFilter(@Value("${app.jwt.secret:${JWT_SECRET:change-me-change-me-change-me-change-me-32b}}") String secret) {
        this.verifier = new JwtVerifier(secret);
    }

    /** Seules les requetes POST sur /graphql sont controlees (la page GraphiQL reste accessible). */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/graphql".equals(request.getRequestURI()) || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            reject(response, 401, "Connexion requise");
            return;
        }
        TokenClaims claims;
        try {
            claims = verifier.verify(header.substring(7).trim());
        } catch (InvalidTokenException e) {
            reject(response, 401, "Session invalide ou expirée");
            return;
        }
        if (!"ADMIN".equals(claims.role())) {
            reject(response, 403, "Réservé aux administrateurs");
            return;
        }
        chain.doFilter(request, response);
    }

    private static void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
