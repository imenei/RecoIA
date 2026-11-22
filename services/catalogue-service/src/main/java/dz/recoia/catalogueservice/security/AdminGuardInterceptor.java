package dz.recoia.catalogueservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/** Applique {@link AccessPolicy} avant chaque requete du catalogue. */
public class AdminGuardInterceptor implements HandlerInterceptor {

    private final AccessPolicy policy;

    public AdminGuardInterceptor(AccessPolicy policy) {
        this.policy = policy;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        AccessPolicy.Decision decision = policy.check(request.getMethod(), request.getHeader("Authorization"));
        if (decision.allowed()) return true;

        response.setStatus(decision.status());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + decision.status() + ",\"message\":\"" + decision.message() + "\"}");
        return false;
    }
}
