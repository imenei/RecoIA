package dz.recoia.recoservice.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Vecteurs = embeddings d'un modele pre-entraine (le sens, pas seulement les mots). */
public final class EmbeddingIndex implements ContentIndex {

    private final TfIdfIndex terms;   // sert uniquement aux explications
    private final TextEmbedder embedder;
    private final Map<Long, Map<String, Double>> vectors = new HashMap<>();
    private final Map<String, Map<String, Double>> cache = new ConcurrentHashMap<>();

    public EmbeddingIndex(List<Course> courses, TextEmbedder embedder) {
        this.terms = new TfIdfIndex(courses);
        this.embedder = embedder;
        List<double[]> vecs = embedder.embed(courses.stream().map(EmbeddingIndex::document).toList());
        for (int i = 0; i < courses.size(); i++) vectors.put(courses.get(i).id(), toMap(vecs.get(i)));
    }

    private static String document(Course c) {
        return c.title() + ". " + c.title() + ". " + c.category() + ". " + c.skills() + ". " + c.description();
    }

    private static Map<String, Double> toMap(double[] v) {
        Map<String, Double> m = new HashMap<>();
        for (int i = 0; i < v.length; i++) m.put(Integer.toString(i), v[i]);
        return m;
    }

    @Override public Map<String, Double> courseVector(long id) { return vectors.getOrDefault(id, Map.of()); }

    @Override public Map<String, Double> textVector(String text) {
        if (text == null || text.isBlank()) return Map.of();
        return cache.computeIfAbsent(text, t -> {
            try { return toMap(embedder.embed(List.of(t)).get(0)); }
            catch (RuntimeException e) { return Map.of(); }
        });
    }

    @Override public Set<String> courseTerms(long id) { return terms.courseTerms(id); }
}