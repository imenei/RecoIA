package dz.recoia.recoservice.service;

import dz.recoia.recoservice.client.RemoteServices;
import dz.recoia.recoservice.client.RemoteServices.CourseDto;
import dz.recoia.recoservice.client.RemoteServices.InteractionDto;
import dz.recoia.recoservice.engine.Course;
import dz.recoia.recoservice.engine.EmbeddingIndex;
import dz.recoia.recoservice.engine.Interaction;
import dz.recoia.recoservice.engine.Recommendation;
import dz.recoia.recoservice.engine.Recommender;
import dz.recoia.recoservice.engine.StudentProfile;
import dz.recoia.recoservice.engine.TextEmbedder;
import dz.recoia.recoservice.engine.Weights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RecoService {

    private static final Logger log = LoggerFactory.getLogger(RecoService.class);

    public record Item(long courseId, String title, String imageUrl, String category,
                       double score, String explanation,
                       String level, Integer hours, Double rating, Integer reviews) { }

    private record Cached(int signature, Recommender recommender, boolean semantic, long builtAt) { }

    private final RemoteServices remote;
    private final TextEmbedder embedder;
    private final Weights weights;
    private volatile Cached cache;

    public RecoService(RemoteServices remote, TextEmbedder embedder,
                       @Value("${app.reco.weights.content:0.5}") double content,
                       @Value("${app.reco.weights.collaborative:0.3}") double collaborative,
                       @Value("${app.reco.weights.feedback:0.2}") double feedback) {
        this.remote = remote;
        this.embedder = embedder;
        this.weights = new Weights(content, collaborative, feedback);
    }

    public List<Item> recommend(long studentId, int limit, boolean record) {
        List<CourseDto> courses = remote.courses();
        if (courses.isEmpty()) return List.of();

        var students = remote.students();
        if (students.stream().noneMatch(s -> s.id() != null && s.id() == studentId)) {
            throw new NoSuchElementException("Étudiant introuvable : " + studentId);
        }

        List<StudentProfile> profiles = new ArrayList<>();
        for (var s : students) {
            profiles.add(new StudentProfile(s.id(), s.interests(), history(s.id())));
        }
        StudentProfile target = profiles.stream().filter(p -> p.studentId() == studentId).findFirst().orElseThrow();

        List<Course> engineCourses = courses.stream()
                .map(c -> new Course(c.id(), nz(c.title()), nz(c.description()), nz(c.category()),
                        nz(c.level()), nz(c.skills())))
                .toList();
        Map<Long, CourseDto> byId = new HashMap<>();
        for (CourseDto c : courses) byId.put(c.id(), c);

        Recommender recommender = recommender(engineCourses);
        List<Item> items = new ArrayList<>();
        for (Recommendation r : recommender.recommend(target, profiles, limit)) {
            CourseDto c = byId.get(r.courseId());
            items.add(new Item(r.courseId(), c.title(), c.imageUrl(), c.category(), r.score(), r.explanation(),
                    c.level(), c.hours(), c.rating(), c.reviews()));
        }

        if (record && !items.isEmpty()) {
            try {
                remote.recordRecommended(studentId, items.stream().map(Item::courseId).toList());
            } catch (RuntimeException e) {
                log.warn("Impossible de signaler les recommandations à activity-service : {}", e.getMessage());
            }
        }
        return items;
    }

    /**
     * Moteur sémantique (Sentence-BERT) mis en cache tant que le catalogue ne change pas.
     * Si ai-service est indisponible : repli sur TF-IDF, et on réessaie le modèle après 60 s.
     */
    private Recommender recommender(List<Course> courses) {
        int signature = courses.hashCode();
        Cached c = cache;
        long now = System.currentTimeMillis();
        if (valid(c, signature, now)) return c.recommender();
        synchronized (this) {
            c = cache;
            if (valid(c, signature, now)) return c.recommender();
            Recommender r;
            boolean semantic;
            try {
                r = new Recommender(courses, weights, new EmbeddingIndex(courses, embedder));
                semantic = true;
                log.info("Moteur sémantique construit ({} formations)", courses.size());
            } catch (RuntimeException e) {
                log.warn("ai-service indisponible, repli sur TF-IDF : {}", e.getMessage());
                r = new Recommender(courses, weights);
                semantic = false;
            }
            cache = new Cached(signature, r, semantic, now);
            return r;
        }
    }

    public String engine() {
        Cached c = cache;
        return c == null ? "non-initialise" : c.semantic() ? "sentence-bert" : "tf-idf (repli)";
    }

    private static boolean valid(Cached c, int signature, long now) {
        return c != null && c.signature() == signature && (c.semantic() || now - c.builtAt() < 60_000);
    }

    private List<Interaction> history(long studentId) {
        List<Interaction> out = new ArrayList<>();
        try {
            for (InteractionDto d : remote.interactions(studentId)) map(d).ifPresent(out::add);
        } catch (RuntimeException e) {
            log.warn("Historique indisponible pour l'étudiant {} : {}", studentId, e.getMessage());
        }
        return out;
    }

    /** activity-service -> moteur : VIEW, RATING (1-5), FAVORITE (1), FEEDBACK (1 = LIKE, -1 = DISLIKE). */
    private static Optional<Interaction> map(InteractionDto d) {
        Integer s = d.score();
        long id = d.courseId();
        return switch (d.type()) {
            case "VIEW" -> Optional.of(Interaction.view(id));
            case "RATING" -> s == null ? Optional.empty() : Optional.of(Interaction.rating(id, s));
            case "FAVORITE" -> (s != null && s == 1) ? Optional.of(Interaction.favorite(id)) : Optional.empty();
            case "FEEDBACK" -> s == null ? Optional.empty()
                    : Optional.of(s > 0 ? Interaction.like(id) : Interaction.dislike(id));
            default -> Optional.empty();   // RECOMMENDED : ignoré
        };
    }

    public int studentCount() {
        return remote.students().size();
    }

    private static String nz(String s) { return s == null ? "" : s; }
}