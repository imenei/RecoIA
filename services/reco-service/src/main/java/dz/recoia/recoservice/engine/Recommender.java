package dz.recoia.recoservice.engine;

import dz.recoia.recoservice.engine.Interaction.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Moteur de recommandation hybride.
 *
 * <pre>
 * score = poidsContenu       x similarite(profil, formation)          [TF-IDF + cosinus]
 *       + poidsCollaboratif  x moyenne ponderee des voisins           [filtrage collaboratif]
 *       + poidsRetours       x (proche d'un "me convient" - proche d'un "ne me convient pas")
 * </pre>
 *
 * Classe sans etat apres construction : elle ne depend ni de Spring ni d'une base de donnees.
 */
public final class Recommender {

    private static final int NEIGHBORS = 10;
    private static final double MIN_SIMILARITY = 0.05;
    private static final double ANCHOR_THRESHOLD = 0.15;

    private final List<Course> courses;
    private final Map<Long, Course> byId = new HashMap<>();
    private final ContentIndex index;
    private final Weights weights;

    public Recommender(List<Course> courses) {
        this(courses, Weights.defaults());
    }

    public Recommender(List<Course> courses, Weights weights) {
        this(courses, weights, new TfIdfIndex(courses));
    }

    public Recommender(List<Course> courses, Weights weights, ContentIndex index) {
        this.courses = List.copyOf(courses);
        this.weights = weights;
        for (Course c : this.courses) byId.put(c.id(), c);
        this.index = index;
    }

    /**
     * @param target profil de l'etudiant
     * @param others profils des autres etudiants (pour le filtrage collaboratif)
     * @param topN   nombre de formations a retourner (0 ou moins = toutes)
     */
    public List<Recommendation> recommend(StudentProfile target, Collection<StudentProfile> others, int topN) {
        List<StudentProfile> peers = others.stream().filter(p -> p.studentId() != target.studentId()).toList();
        Set<Long> excluded = excludedIds(target);

        Map<String, Double> profile = buildProfile(target);
        List<Map<String, Double>> liked = vectorsOf(target, Type.LIKE);
        List<Map<String, Double>> disliked = vectorsOf(target, Type.DISLIKE);
        List<Neighbor> neighbors = findNeighbors(implicit(target), peers);
        List<Anchor> anchors = anchors(target);

        List<Scored> scored = new ArrayList<>();
        for (Course c : courses) {
            if (excluded.contains(c.id())) continue;
            Map<String, Double> cv = index.courseVector(c.id());
            double content = TfIdfIndex.cosine(profile, cv);
            double adjust = maxSimilarity(cv, liked) - maxSimilarity(cv, disliked);
            Collab collab = collaborative(c.id(), neighbors);
            double raw = weights.content() * content + weights.collaborative() * collab.score
                    + weights.feedback() * adjust;
            scored.add(new Scored(c, Math.max(0, raw), collab, adjust));
        }

        double best = scored.stream().mapToDouble(Scored::raw).max().orElse(0);
        List<Recommendation> result = new ArrayList<>();

        if (best <= 0) {
            result = popularity(scored, peers);                       // demarrage a froid
        } else {
            scored.sort(Comparator.comparingDouble(Scored::raw).reversed()
                    .thenComparingLong(s -> s.course.id()));
            for (Scored s : scored) {
                result.add(new Recommendation(s.course.id(), s.raw / best, s.raw, explain(s, target, anchors)));
            }
        }
        return topN > 0 && result.size() > topN ? new ArrayList<>(result.subList(0, topN)) : result;
    }

    // ---------------------------------------------------------------- profil et contenu

    /** Formations deja connues de l'etudiant : on ne les lui recommande pas. */
    private Set<Long> excludedIds(StudentProfile s) {
        Set<Long> ids = new HashSet<>();
        for (Interaction i : s.history()) {
            if (i.type() != Type.VIEW) ids.add(i.courseId());
        }
        return ids;
    }

    /** Vecteur de gouts : centres d'interet + formations favorites, aimees ou bien notees (+ vues, faiblement). */
    private Map<String, Double> buildProfile(StudentProfile s) {
        Map<String, Double> profile = new HashMap<>();
        for (String interest : s.interests()) TfIdfIndex.addScaled(profile, index.textVector(interest), 1.0);

        Set<Long> viewed = new HashSet<>();
        for (Interaction i : s.history()) {
            double w = switch (i.type()) {
                case FAVORITE, LIKE -> 1.0;
                case RATING -> i.value() >= 4 ? (i.value() - 3) / 2.0 : 0.0;
                case VIEW -> viewed.add(i.courseId()) ? 0.2 : 0.0;
                case DISLIKE -> 0.0;
            };
            if (w > 0) TfIdfIndex.addScaled(profile, index.courseVector(i.courseId()), w);
        }
        return profile;
    }

    private List<Map<String, Double>> vectorsOf(StudentProfile s, Type type) {
        List<Map<String, Double>> out = new ArrayList<>();
        for (Interaction i : s.history()) {
            if (i.type() == type) out.add(index.courseVector(i.courseId()));
        }
        return out;
    }

    private static double maxSimilarity(Map<String, Double> v, List<Map<String, Double>> others) {
        double max = 0;
        for (Map<String, Double> o : others) max = Math.max(max, TfIdfIndex.cosine(v, o));
        return max;
    }

    // ---------------------------------------------------------------- filtrage collaboratif

    private record Neighbor(Map<Long, Double> ratings, double similarity) { }

    private record Collab(double score, int supporters) { }

    /** Appreciation implicite d'un etudiant pour chaque formation, entre -1 et 1. */
    private static Map<Long, Double> implicit(StudentProfile s) {
        Map<Long, Double> m = new HashMap<>();
        Set<Long> viewed = new HashSet<>();
        for (Interaction i : s.history()) {
            double d = switch (i.type()) {
                case VIEW -> viewed.add(i.courseId()) ? 0.2 : 0.0;
                case FAVORITE -> 0.8;
                case LIKE -> 1.0;
                case DISLIKE -> -1.0;
                case RATING -> (i.value() - 3) / 2.0;
            };
            m.merge(i.courseId(), d, Double::sum);
        }
        m.replaceAll((k, v) -> Math.max(-1, Math.min(1, v)));
        return m;
    }

    private static List<Neighbor> findNeighbors(Map<Long, Double> mine, List<StudentProfile> peers) {
        if (mine.isEmpty()) return List.of();
        List<Neighbor> all = new ArrayList<>();
        for (StudentProfile p : peers) {
            Map<Long, Double> theirs = implicit(p);
            double sim = cosineById(mine, theirs);
            if (sim >= MIN_SIMILARITY) all.add(new Neighbor(theirs, sim));
        }
        all.sort(Comparator.comparingDouble(Neighbor::similarity).reversed());
        return all.size() > NEIGHBORS ? all.subList(0, NEIGHBORS) : all;
    }

    private static double cosineById(Map<Long, Double> a, Map<Long, Double> b) {
        double dot = 0;
        for (Map.Entry<Long, Double> e : a.entrySet()) {
            Double w = b.get(e.getKey());
            if (w != null) dot += e.getValue() * w;
        }
        double na = 0, nb = 0;
        for (double x : a.values()) na += x * x;
        for (double x : b.values()) nb += x * x;
        return na == 0 || nb == 0 ? 0 : dot / Math.sqrt(na * nb);
    }

    private static Collab collaborative(long courseId, List<Neighbor> neighbors) {
        double num = 0, den = 0;
        int supporters = 0;
        for (Neighbor n : neighbors) {
            Double r = n.ratings.get(courseId);
            if (r == null) continue;
            num += n.similarity * r;
            den += n.similarity;
            if (r >= 0.5) supporters++;
        }
        // le +0.2 attenue le score quand peu de voisins ont un avis
        return new Collab(den > 0 ? Math.max(0, num / (den + 0.2)) : 0, supporters);
    }

    // ---------------------------------------------------------------- demarrage a froid

    /** Sans profil ni voisins : on propose les formations les plus appreciees des autres etudiants. */
    private List<Recommendation> popularity(List<Scored> candidates, List<StudentProfile> peers) {
        Map<Long, Integer> votes = new HashMap<>();
        for (StudentProfile p : peers) {
            for (Interaction i : p.history()) {
                boolean positive = i.type() == Type.FAVORITE || i.type() == Type.LIKE
                        || (i.type() == Type.RATING && i.value() >= 4);
                if (positive) votes.merge(i.courseId(), 1, Integer::sum);
            }
        }
        int max = votes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Scored> sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator.<Scored>comparingInt(s -> votes.getOrDefault(s.course.id(), 0)).reversed()
                .thenComparingLong(s -> s.course.id()));

        List<Recommendation> out = new ArrayList<>();
        for (Scored s : sorted) {
            int v = votes.getOrDefault(s.course.id(), 0);
            out.add(new Recommendation(s.course.id(), max == 0 ? 0 : (double) v / max, v,
                    v > 0 ? "Populaire auprès des autres apprenants" : "Formation du catalogue"));
        }
        return out;
    }

    // ---------------------------------------------------------------- explications

    private record Anchor(long courseId, boolean favorite) { }

    private record Scored(Course course, double raw, Collab collab, double adjust) { }

    private List<Anchor> anchors(StudentProfile s) {
        List<Anchor> out = new ArrayList<>();
        for (Interaction i : s.history()) {
            if (i.type() == Type.FAVORITE) out.add(new Anchor(i.courseId(), true));
            else if (i.type() == Type.LIKE || (i.type() == Type.RATING && i.value() >= 4)) {
                out.add(new Anchor(i.courseId(), false));
            }
        }
        return out;
    }

    private String explain(Scored s, StudentProfile target, List<Anchor> anchors) {
        List<String> reasons = new ArrayList<>();
        Map<String, Double> cv = index.courseVector(s.course.id());

        Anchor best = null;
        double bestSim = 0;
        for (Anchor a : anchors) {
            double sim = TfIdfIndex.cosine(cv, index.courseVector(a.courseId));
            if (sim > bestSim) { bestSim = sim; best = a; }
        }
        if (best != null && bestSim >= ANCHOR_THRESHOLD && byId.containsKey(best.courseId)) {
            String title = byId.get(best.courseId).title();
            reasons.add(best.favorite ? "Similaire à tes favoris : " + title
                    : "Similaire à ce que tu as aimé : " + title);
        } else if (s.adjust > 0.2) {
            reasons.add("Proche des formations que tu as aimées");
        }

        String interest = bestInterest(target.interests(), s.course.id());
        if (interest != null) reasons.add("Correspond à ton intérêt : " + interest);

        if (s.collab.supporters > 0 && s.collab.score >= 0.1) {
            reasons.add(s.collab.supporters == 1 ? "Apprécié par un étudiant proche de toi"
                    : "Apprécié par " + s.collab.supporters + " étudiants proches de toi");
        }
        if (reasons.isEmpty()) return "Sélectionnée à partir de ton profil";
        return String.join(" · ", reasons.subList(0, Math.min(2, reasons.size())));
    }

    private String bestInterest(List<String> interests, long courseId) {
        Set<String> terms = index.courseTerms(courseId);
        String best = null;
        double bestRatio = 0;
        for (String interest : interests) {
            List<String> tokens = TextAnalyzer.tokens(interest);
            if (tokens.isEmpty()) continue;
            long matched = tokens.stream().filter(terms::contains).count();
            double ratio = (double) matched / tokens.size();
            if (ratio >= 0.5 && ratio > bestRatio) { bestRatio = ratio; best = interest; }
        }
        return best;
    }
}
