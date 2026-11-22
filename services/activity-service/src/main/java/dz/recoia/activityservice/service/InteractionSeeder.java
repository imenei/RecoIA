package dz.recoia.activityservice.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dz.recoia.activityservice.domain.Interaction;
import dz.recoia.activityservice.domain.InteractionType;
import dz.recoia.activityservice.repo.InteractionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Au demarrage, si aucune note n'existe : genere des interactions de test pour les etudiants.
 * Chaque etudiant interagit surtout avec les formations proches de ses centres d'interet.
 * Necessite auth-service et catalogue-service demarres ; sinon le seed est ignore (relancer le service).
 */
@Component
public class InteractionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InteractionSeeder.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CourseDto(Long id, String title, String category, String skills, String description) { }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record StudentDto(Long id, List<String> interests) { }

    private final InteractionRepository repo;
    private final boolean enabled;
    private final String authUrl;
    private final String catalogueUrl;

    public InteractionSeeder(InteractionRepository repo,
                             @Value("${app.seed-interactions:true}") boolean enabled,
                             @Value("${app.seed.auth-url:http://localhost:8081}") String authUrl,
                             @Value("${app.seed.catalogue-url:http://localhost:8082}") String catalogueUrl) {
        this.repo = repo;
        this.enabled = enabled;
        this.authUrl = authUrl;
        this.catalogueUrl = catalogueUrl;
    }

    @Override
    public void run(String... args) {
        if (!enabled || repo.countByType(InteractionType.VIEW) > 50) return;
        try {
            seed();
        } catch (Exception e) {
            log.warn("[activity] seed des interactions ignoré (auth/catalogue indisponibles ?) : {}", e.getMessage());
        }
    }

    private void seed() {
        RestClient http = RestClient.create();
        List<StudentDto> students = http.get().uri(authUrl + "/internal/users/students").retrieve()
                .body(new ParameterizedTypeReference<List<StudentDto>>() { });
        List<CourseDto> courses = http.get().uri(catalogueUrl + "/api/courses/all").retrieve()
                .body(new ParameterizedTypeReference<List<CourseDto>>() { });
        if (students == null || courses == null || students.isEmpty() || courses.isEmpty()) {
            log.warn("[activity] seed ignoré : aucun étudiant ou aucune formation");
            return;
        }

        List<Set<String>> courseTokens = new ArrayList<>();
        for (CourseDto c : courses) {
            courseTokens.add(tokens(nz(c.title()) + " " + nz(c.category()) + " " + nz(c.skills()) + " " + nz(c.description())));
        }

        int rows = 0;
        for (StudentDto s : students) {
            if (s.id() == null) continue;
            Random rnd = new Random(42 + s.id());
            Set<String> interest = tokens(String.join(" ", s.interests() == null ? List.of() : s.interests()));

            List<Integer> order = new ArrayList<>();
            List<Double> score = new ArrayList<>();
            for (int i = 0; i < courses.size(); i++) {
                Set<String> inter = new HashSet<>(interest);
                inter.retainAll(courseTokens.get(i));
                score.add(inter.size() + rnd.nextDouble() * 0.5);   // petit bruit pour varier
                order.add(i);
            }
            order.sort((a, b) -> Double.compare(score.get(b), score.get(a)));

            Set<Integer> used = new HashSet<>();
            int liked = 6 + rnd.nextInt(5);
            for (int k = 0; k < liked && k < order.size(); k++) {
                int i = order.get(k);
                used.add(i);
                rows += like(s.id(), courses.get(i).id(), rnd);
            }
            int noise = 2 + rnd.nextInt(3);
            for (int k = 0; k < noise; k++) {
                int i = rnd.nextInt(courses.size());
                if (!used.add(i)) continue;
                rows += dislike(s.id(), courses.get(i).id(), rnd);
            }
        }
        log.info("[activity] {} interactions de test créées pour {} étudiants", rows, students.size());
    }

    /** Formation proche des interets : vues, souvent une bonne note, parfois favori et "me convient". */
    private int like(long student, long course, Random rnd) {
        int n = 0;
        for (int v = 1 + rnd.nextInt(3); v > 0; v--) { repo.save(new Interaction(student, course, InteractionType.VIEW, null)); n++; }
        if (rnd.nextInt(100) < 80) { repo.save(new Interaction(student, course, InteractionType.RATING, 4 + rnd.nextInt(2))); n++; }
        if (rnd.nextInt(100) < 35) { repo.save(new Interaction(student, course, InteractionType.FAVORITE, 1)); n++; }
        if (rnd.nextInt(100) < 40) { repo.save(new Interaction(student, course, InteractionType.FEEDBACK, 1)); n++; }
        return n;
    }

    /** Formation hors profil : consultee, parfois mal notee ou rejetee. */
    private int dislike(long student, long course, Random rnd) {
        int n = 1;
        repo.save(new Interaction(student, course, InteractionType.VIEW, null));
        if (rnd.nextInt(100) < 50) { repo.save(new Interaction(student, course, InteractionType.RATING, 1 + rnd.nextInt(3))); n++; }
        if (rnd.nextInt(100) < 40) { repo.save(new Interaction(student, course, InteractionType.FEEDBACK, -1)); n++; }
        return n;
    }

    private static Set<String> tokens(String text) {
        Set<String> out = new HashSet<>();
        String norm = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        for (String t : norm.split("[^a-z0-9]+")) {
            if (t.length() < 3) continue;
            out.add(t.length() > 4 && t.endsWith("s") ? t.substring(0, t.length() - 1) : t);
        }
        return out;
    }

    private static String nz(String s) { return s == null ? "" : s; }
}