package dz.recoia.recoservice.engine;

/** Une formation du catalogue (donnees recues du catalogue-service). */
public record Course(long id, String title, String description, String category, String level, String skills) {
}
