package dz.recoia.recoservice.engine;

/** Ponderation du score final : contenu, collaboratif, retours utilisateur. */
public record Weights(double content, double collaborative, double feedback) {

    public static Weights defaults() { return new Weights(0.5, 0.3, 0.2); }
}
