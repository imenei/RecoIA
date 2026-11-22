package dz.recoia.activityservice.web;

import dz.recoia.activityservice.service.ActivityService;
import dz.recoia.activityservice.service.ActivityService.Summary;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Endpoints internes pour stats-service et reco-service.
 * La gateway ne route pas /internal/** : ils ne sont pas joignables depuis le navigateur.
 */
@RestController
@RequestMapping("/internal/activity")
public class InternalController {

    public record InteractionView(long courseId, String type, Integer score, Instant createdAt) { }

    public record RecommendedRequest(Long studentId, List<Long> courseIds) { }

    private final ActivityService service;

    public InternalController(ActivityService service) {
        this.service = service;
    }

    /** Classements et totaux pour le tableau de bord de l'administrateur. */
    @GetMapping("/summary")
    public Summary summary(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        return service.summary(limit);
    }

    /** Historique d'un etudiant, pour le calcul des recommandations. */
    @GetMapping("/students/{id}/interactions")
    public List<InteractionView> interactions(@PathVariable("id") long id) {
        return service.interactionsOf(id).stream()
                .map(i -> new InteractionView(i.getCourseId(), i.getType().name(), i.getScore(), i.getCreatedAt()))
                .toList();
    }

    /** Le service de recommandation signale les formations qu'il vient de proposer. */
    @PostMapping("/recommended")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recommended(@RequestBody RecommendedRequest body) {
        if (body.studentId() == null) throw new IllegalArgumentException("Le champ studentId est obligatoire");
        service.recordRecommended(body.studentId(), body.courseIds());
    }
}
