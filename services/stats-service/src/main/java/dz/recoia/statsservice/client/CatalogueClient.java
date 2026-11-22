package dz.recoia.statsservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Titres des formations (REST vers catalogue-service) : activity-service ne connait que les identifiants. */
@Component
public class CatalogueClient {

    public record CatalogueCourse(Long id, String title) { }

    private final RestClient http;
    private final String url;

    public CatalogueClient(@Value("${app.services.catalogue:http://localhost:8082}") String url) {
        this.url = url;
        HttpClient jdk = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofSeconds(15));
        this.http = RestClient.builder().requestFactory(factory).build();
    }

    /** identifiant -> titre. Renvoie une table vide si le catalogue ne repond pas : les statistiques restent affichees. */
    public Map<Long, String> titles() {
        Map<Long, String> out = new HashMap<>();
        try {
            List<CatalogueCourse> courses = http.get().uri(url + "/api/courses/all").retrieve()
                    .body(new ParameterizedTypeReference<List<CatalogueCourse>>() { });
            if (courses != null) {
                for (CatalogueCourse c : courses) if (c.id() != null) out.put(c.id(), c.title());
            }
        } catch (RuntimeException e) {
            // titres absents : le tableau de bord affichera seulement les identifiants
        }
        return out;
    }
}
