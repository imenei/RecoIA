package dz.recoia.recoservice.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Vecteurs TF-IDF du catalogue + similarite cosinus. */
final class TfIdfIndex implements ContentIndex {

    private final Map<String, Double> idf = new HashMap<>();
    private final Map<Long, Map<String, Double>> vectors = new HashMap<>();
    private final Map<Long, Set<String>> terms = new HashMap<>();

    TfIdfIndex(List<Course> courses) {
        Map<Long, Map<String, Integer>> counts = new HashMap<>();
        Map<String, Integer> df = new HashMap<>();

        for (Course c : courses) {
            // le titre pese double, la categorie et les competences completent la description
            String doc = c.title() + " " + c.title() + " " + c.category() + " " + c.skills() + " " + c.description();
            Map<String, Integer> tf = new HashMap<>();
            for (String t : TextAnalyzer.tokens(doc)) tf.merge(t, 1, Integer::sum);
            counts.put(c.id(), tf);
            terms.put(c.id(), new HashSet<>(tf.keySet()));
            for (String t : tf.keySet()) df.merge(t, 1, Integer::sum);
        }

        int n = courses.size();
        for (Map.Entry<String, Integer> e : df.entrySet()) {
            idf.put(e.getKey(), Math.log((1.0 + n) / (1.0 + e.getValue())) + 1.0);
        }
        for (Course c : courses) {
            vectors.put(c.id(), weigh(counts.get(c.id())));
        }
    }

    @Override public Map<String, Double> courseVector(long courseId) {
        return vectors.getOrDefault(courseId, Map.of());
    }

    @Override public Set<String> courseTerms(long courseId) {
        return terms.getOrDefault(courseId, Set.of());
    }

    /** Vecteur d'un texte libre (ex. un centre d'interet), dans le meme espace que les formations. */
    @Override public Map<String, Double> textVector(String text) {
        Map<String, Integer> tf = new HashMap<>();
        for (String t : TextAnalyzer.tokens(text)) {
            if (idf.containsKey(t)) tf.merge(t, 1, Integer::sum);   // mots inconnus du catalogue ignores
        }
        return weigh(tf);
    }

    /** TF sous-lineaire x IDF, normalise en norme 1. */
    private Map<String, Double> weigh(Map<String, Integer> tf) {
        Map<String, Double> v = new HashMap<>();
        double norm = 0;
        for (Map.Entry<String, Integer> e : tf.entrySet()) {
            double w = (1.0 + Math.log(e.getValue())) * idf.getOrDefault(e.getKey(), 1.0);
            v.put(e.getKey(), w);
            norm += w * w;
        }
        if (norm == 0) return Map.of();
        norm = Math.sqrt(norm);
        for (Map.Entry<String, Double> e : v.entrySet()) e.setValue(e.getValue() / norm);
        return v;
    }

    static double cosine(Map<String, Double> a, Map<String, Double> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Map<String, Double> small = a.size() <= b.size() ? a : b;
        Map<String, Double> large = small == a ? b : a;
        double dot = 0;
        for (Map.Entry<String, Double> e : small.entrySet()) {
            Double w = large.get(e.getKey());
            if (w != null) dot += e.getValue() * w;
        }
        return dot / (norm(a) * norm(b));
    }

    private static double norm(Map<String, Double> v) {
        double s = 0;
        for (double x : v.values()) s += x * x;
        return Math.sqrt(s);
    }

    static void addScaled(Map<String, Double> target, Map<String, Double> v, double weight) {
        for (Map.Entry<String, Double> e : v.entrySet()) target.merge(e.getKey(), e.getValue() * weight, Double::sum);
    }
}
