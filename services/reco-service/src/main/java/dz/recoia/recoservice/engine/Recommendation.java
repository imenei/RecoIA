package dz.recoia.recoservice.engine;

/**
 * Une formation recommandee.
 *
 * @param score    compatibilite relative entre 0 et 1 (1 = meilleure formation de la liste)
 * @param rawScore score brut avant normalisation
 */
public record Recommendation(long courseId, double score, double rawScore, String explanation) {
}
