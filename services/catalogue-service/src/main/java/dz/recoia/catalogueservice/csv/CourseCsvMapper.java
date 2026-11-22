package dz.recoia.catalogueservice.csv;

import dz.recoia.catalogueservice.domain.Course;

import java.util.*;

/**
 * Transforme des lignes CSV en formations. Accepte nos noms de colonnes
 * (title, university, level, ...) ET ceux du dataset Coursera
 * (Course Name, University, Difficulty Level, Course Rating, Course Description, Skills).
 */
public final class CourseCsvMapper {
    private CourseCsvMapper() {}

    private static final Map<String, List<String>> ALIASES = Map.of(
        "title", List.of("title", "titre", "course name", "course_name", "name"),
        "university", List.of("university", "universite", "organization", "institution"),
        "level", List.of("level", "niveau", "difficulty level", "difficulty"),
        "category", List.of("category", "categorie", "domaine"),
        "description", List.of("description", "course description"),
        "skills", List.of("skills", "competences"),
        "rating", List.of("rating", "note", "course rating"),
        "reviews", List.of("reviews", "avis"),
        "hours", List.of("hours", "duree", "heures")
    );

    public static List<Course> map(List<List<String>> rows) {
        if (rows.isEmpty()) return List.of();
        Map<String, Integer> idx = new HashMap<>();
        List<String> header = rows.get(0);
        for (Map.Entry<String, List<String>> e : ALIASES.entrySet()) {
            for (int i = 0; i < header.size(); i++) {
                if (e.getValue().contains(header.get(i).trim().toLowerCase())) { idx.put(e.getKey(), i); break; }
            }
        }
        if (!idx.containsKey("title")) throw new IllegalArgumentException("Colonne du titre introuvable (title / Course Name)");
        List<Course> out = new ArrayList<>();
        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            String title = get(row, idx, "title");
            if (title.isBlank()) continue;
            Course c = new Course();
            c.setTitle(cut(title, 300));
            c.setUniversity(cut(get(row, idx, "university"), 200));
            c.setLevel(normalizeLevel(get(row, idx, "level")));
            c.setCategory(cut(get(row, idx, "category"), 100));
            c.setDescription(cut(get(row, idx, "description"), 4000));
            c.setSkills(cut(get(row, idx, "skills"), 1000));
            c.setRating(clamp(parseDouble(get(row, idx, "rating")), 0, 5));
            c.setReviews((int) parseDouble(get(row, idx, "reviews")));
            c.setHours((int) parseDouble(get(row, idx, "hours")));
            out.add(c);
        }
        return out;
    }

    /** Niveaux de l'interface : Licence, Master, Doctorat, Formation courte, Certifications. */
    public static String normalizeLevel(String raw) {
        String s = raw == null ? "" : raw.trim().toLowerCase();
        return switch (s) {
            case "", "n/a" -> "Formation courte";
            case "beginner", "debutant", "débutant" -> "Licence";
            case "intermediate", "intermediaire", "intermédiaire" -> "Master";
            case "advanced", "avance", "avancé" -> "Doctorat";
            case "conditionally mixed", "mixed" -> "Certifications";
            default -> raw.trim();
        };
    }

    private static String get(List<String> row, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        return (i == null || i >= row.size()) ? "" : row.get(i).trim();
    }

    private static double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Double.parseDouble(s.trim().replace(',', '.')); }
        catch (NumberFormatException e) { return 0; }
    }

    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
    private static String cut(String s, int max) { return s.length() <= max ? s : s.substring(0, max); }
}
