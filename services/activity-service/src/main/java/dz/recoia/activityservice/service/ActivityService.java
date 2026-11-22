package dz.recoia.activityservice.service;

import dz.recoia.activityservice.domain.Interaction;
import dz.recoia.activityservice.domain.InteractionType;
import dz.recoia.activityservice.repo.InteractionRepository;
import dz.recoia.activityservice.repo.InteractionRepository.CourseCount;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

    public record CourseStat(long courseId, long count) { }

    public record Totals(long views, long recommended, long accepted, long rejected) { }

    /** Tout ce que le tableau de bord de l'administrateur affiche. */
    public record Summary(Totals totals, List<CourseStat> mostViewed, List<CourseStat> mostRecommended,
                          List<CourseStat> mostAccepted, List<CourseStat> mostRejected) { }

    /** Etat d'un etudiant : ses favoris, ses notes et ses retours (1 = convient, -1 = ne convient pas). */
    public record StudentState(List<Long> favorites, Map<Long, Integer> ratings, Map<Long, Integer> feedback) { }

    private final InteractionRepository repo;

    public ActivityService(InteractionRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void recordView(long studentId, long courseId) {
        checkCourse(courseId);
        repo.save(new Interaction(studentId, courseId, InteractionType.VIEW, null));
    }

    @Transactional
    public void rate(long studentId, long courseId, Integer value) {
        checkCourse(courseId);
        if (value == null || value < 1 || value > 5) {
            throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
        }
        upsert(studentId, courseId, InteractionType.RATING, value);
    }

    @Transactional
    public void setFavorite(long studentId, long courseId, boolean favorite) {
        checkCourse(courseId);
        upsert(studentId, courseId, InteractionType.FAVORITE, favorite ? 1 : 0);
    }

    @Transactional
    public void setFeedback(long studentId, long courseId, Boolean accepted) {
        checkCourse(courseId);
        if (accepted == null) throw new IllegalArgumentException("Le champ accepted est obligatoire");
        upsert(studentId, courseId, InteractionType.FEEDBACK, accepted ? 1 : -1);
    }

    /** Appele par le service de recommandation quand il propose des formations a un etudiant. */
    @Transactional
    public void recordRecommended(long studentId, List<Long> courseIds) {
        if (courseIds == null) throw new IllegalArgumentException("La liste courseIds est obligatoire");
        for (Long id : new LinkedHashSet<>(courseIds)) {
            if (id == null) continue;
            checkCourse(id);
            repo.save(new Interaction(studentId, id, InteractionType.RECOMMENDED, null));
        }
    }

    @Transactional(readOnly = true)
    public StudentState state(long studentId) {
        List<Long> favorites = new ArrayList<>();
        Map<Long, Integer> ratings = new LinkedHashMap<>();
        Map<Long, Integer> feedback = new LinkedHashMap<>();
        for (Interaction i : repo.findByStudentId(studentId)) {
            if (i.getScore() == null) continue;
            switch (i.getType()) {
                case FAVORITE -> { if (i.getScore() == 1) favorites.add(i.getCourseId()); }
                case RATING -> ratings.put(i.getCourseId(), i.getScore());
                case FEEDBACK -> feedback.put(i.getCourseId(), i.getScore());
                default -> { }
            }
        }
        return new StudentState(favorites, ratings, feedback);
    }

    @Transactional(readOnly = true)
    public List<Interaction> interactionsOf(long studentId) {
        return repo.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public Summary summary(int limit) {
        int n = Math.max(1, Math.min(limit, 50));
        var page = PageRequest.of(0, n);
        Totals totals = new Totals(
                repo.countByType(InteractionType.VIEW),
                repo.countByType(InteractionType.RECOMMENDED),
                repo.countByTypeAndScore(InteractionType.FEEDBACK, 1),
                repo.countByTypeAndScore(InteractionType.FEEDBACK, -1));
        return new Summary(totals,
                stats(repo.top(InteractionType.VIEW, page)),
                stats(repo.top(InteractionType.RECOMMENDED, page)),
                stats(repo.topByScore(InteractionType.FEEDBACK, 1, page)),
                stats(repo.topByScore(InteractionType.FEEDBACK, -1, page)));
    }

    // ------------------------------------------------------------------ utilitaires

    private void upsert(long studentId, long courseId, InteractionType type, int score) {
        Interaction row = repo.findFirstByStudentIdAndCourseIdAndType(studentId, courseId, type)
                .orElseGet(() -> new Interaction(studentId, courseId, type, score));
        row.setScore(score);
        repo.save(row);
    }

    private static void checkCourse(long courseId) {
        if (courseId <= 0) throw new IllegalArgumentException("Identifiant de formation invalide");
    }

    private static List<CourseStat> stats(List<CourseCount> rows) {
        return rows.stream().map(r -> new CourseStat(r.getCourseId(), r.getTotal())).toList();
    }
}
