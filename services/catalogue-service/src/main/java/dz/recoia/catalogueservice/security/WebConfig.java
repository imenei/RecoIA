package dz.recoia.catalogueservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Branche la verification sur /api/courses/** et /api/images/**.
 * Les endpoints /internal/** (appeles par les autres services) ne sont pas concernes.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminGuardInterceptor guard;

    public WebConfig(AdminGuardInterceptor guard) {
        this.guard = guard;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(guard).addPathPatterns("/api/courses/**", "/api/images/**");
    }
}
