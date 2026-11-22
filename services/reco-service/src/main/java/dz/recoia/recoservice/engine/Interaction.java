package dz.recoia.recoservice.engine;

/** Une action d'un etudiant sur une formation. */
public record Interaction(long courseId, Type type, double value) {

    public enum Type { VIEW, RATING, FAVORITE, LIKE, DISLIKE }

    public static Interaction view(long courseId) { return new Interaction(courseId, Type.VIEW, 1); }
    public static Interaction rating(long courseId, double stars) { return new Interaction(courseId, Type.RATING, stars); }
    public static Interaction favorite(long courseId) { return new Interaction(courseId, Type.FAVORITE, 1); }
    public static Interaction like(long courseId) { return new Interaction(courseId, Type.LIKE, 1); }
    public static Interaction dislike(long courseId) { return new Interaction(courseId, Type.DISLIKE, 1); }
}
