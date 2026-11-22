package dz.recoia.recoservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** Appels vers catalogue-service, auth-service et activity-service. */
@Component
public class RemoteServices {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CourseDto(Long id, String title, String description, String category, String level,
                            String skills, String imageUrl,
                            Integer hours, Double rating, Integer reviews) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StudentDto(Long id, String fullName, List<String> interests) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InteractionDto(long courseId, String type, Integer score) { }

    public record RecommendedBody(Long studentId, List<Long> courseIds) { }

    private final RestClient http;

    public RemoteServices() {
        this.http = RestClient.create();
    }

    public List<CourseDto> courses() {
        List<CourseDto> r = http.get().uri("http://localhost:8082/api/courses/all").retrieve()
                .body(new ParameterizedTypeReference<List<CourseDto>>() { });
        return r == null ? List.of() : r;
    }

    public List<StudentDto> students() {
        List<StudentDto> r = http.get().uri("http://localhost:8081/internal/users/students").retrieve()
                .body(new ParameterizedTypeReference<List<StudentDto>>() { });
        return r == null ? List.of() : r;
    }

    public List<InteractionDto> interactions(long studentId) {
        List<InteractionDto> r = http.get()
                .uri("http://localhost:8083/internal/activity/students/{id}/interactions", studentId).retrieve()
                .body(new ParameterizedTypeReference<List<InteractionDto>>() { });
        return r == null ? List.of() : r;
    }

    public void recordRecommended(long studentId, List<Long> courseIds) {
        http.post().uri("http://localhost:8083/internal/activity/recommended")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RecommendedBody(studentId, courseIds))
                .retrieve().toBodilessEntity();
    }
}