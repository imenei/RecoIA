package dz.recoia.activityservice.web;

import dz.recoia.activityservice.security.TokenReader;
import dz.recoia.activityservice.service.ActivityService;
import dz.recoia.activityservice.service.ActivityService.StudentState;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Activite de l'etudiant connecte (acces via la gateway : /api/activity/**).
 * L'identifiant de l'etudiant est lu dans le jeton, pas dans l'URL.
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    public record RatingRequest(Integer value) { }

    public record FeedbackRequest(Boolean accepted) { }

    private final ActivityService service;
    private final TokenReader tokens;

    public ActivityController(ActivityService service, TokenReader tokens) {
        this.service = service;
        this.tokens = tokens;
    }

    /** Une consultation de la fiche d'une formation. */
    @PostMapping("/views/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void view(@RequestHeader(name = "Authorization", required = false) String authorization,
                     @PathVariable("courseId") long courseId) {
        service.recordView(tokens.require(authorization).userId(), courseId);
    }

    /** Note de 1 a 5 : {"value": 4}. Une nouvelle note remplace l'ancienne. */
    @PutMapping("/ratings/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rate(@RequestHeader(name = "Authorization", required = false) String authorization,
                     @PathVariable("courseId") long courseId,
                     @RequestBody RatingRequest body) {
        service.rate(tokens.require(authorization).userId(), courseId, body.value());
    }

    @PutMapping("/favorites/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavorite(@RequestHeader(name = "Authorization", required = false) String authorization,
                            @PathVariable("courseId") long courseId) {
        service.setFavorite(tokens.require(authorization).userId(), courseId, true);
    }

    @DeleteMapping("/favorites/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@RequestHeader(name = "Authorization", required = false) String authorization,
                               @PathVariable("courseId") long courseId) {
        service.setFavorite(tokens.require(authorization).userId(), courseId, false);
    }

    /** Retour sur une recommandation : {"accepted": true} (me convient) ou {"accepted": false}. */
    @PutMapping("/feedback/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void feedback(@RequestHeader(name = "Authorization", required = false) String authorization,
                         @PathVariable("courseId") long courseId,
                         @RequestBody FeedbackRequest body) {
        service.setFeedback(tokens.require(authorization).userId(), courseId, body.accepted());
    }

    /** Favoris, notes et retours de l'etudiant connecte. */
    @GetMapping("/me")
    public StudentState me(@RequestHeader(name = "Authorization", required = false) String authorization) {
        return service.state(tokens.require(authorization).userId());
    }
}
