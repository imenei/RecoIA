package dz.recoia.recoservice.web;

import dz.recoia.recoservice.security.TokenClaims;
import dz.recoia.recoservice.security.TokenReader;
import dz.recoia.recoservice.service.RecoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/reco")
public class RecoController {

    private final RecoService service;
    private final TokenReader tokens;

    public RecoController(RecoService service, TokenReader tokens) {
        this.service = service;
        this.tokens = tokens;
    }

    /** Recommandations de l'etudiant connecte : l'identite vient du jeton. */
    @GetMapping("/me")
    public List<RecoService.Item> me(@RequestHeader(value = "Authorization", required = false) String auth,
                                     @RequestParam(name = "limit", defaultValue = "10") int limit,
                                     @RequestParam(name = "record", defaultValue = "true") boolean record) {
        TokenClaims c = tokens.require(auth);
        return service.recommend(c.userId(), clamp(limit), record);
    }

    /** Un etudiant ne peut lire que ses propres recommandations ; l'administrateur peut tout lire. */
    @GetMapping("/students/{id}")
    public List<RecoService.Item> forStudent(@PathVariable("id") long id,
                                             @RequestHeader(value = "Authorization", required = false) String auth,
                                             @RequestParam(name = "limit", defaultValue = "10") int limit,
                                             @RequestParam(name = "record", defaultValue = "true") boolean record) {
        TokenClaims c = tokens.require(auth);
        if (c.userId() != id && !"ADMIN".equals(c.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        return service.recommend(id, clamp(limit), record);
    }

    /** Indique quel moteur est actif : sentence-bert ou tf-idf (repli). */
    @GetMapping("/engine")
    public Map<String, String> engine() {
        return Map.of("engine", service.engine());
    }

    private static int clamp(int limit) { return Math.max(1, Math.min(limit, 50)); }

    /** GET /api/reco/stats : nombre d'étudiants inscrits (aucune donnée personnelle). */
    @GetMapping("/stats")
    public Map<String, Integer> stats() {
        return Map.of("students", service.studentCount());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
}