package dz.recoia.authservice.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Nettoyage de la liste des centres d'interet saisis par l'etudiant. */
public final class Interests {

    public static final int MAX_COUNT = 8;
    public static final int MAX_LENGTH = 60;

    private Interests() { }

    /** Retire les vides et les doublons (sans tenir compte de la casse), limite le nombre et la longueur. */
    public static List<String> clean(List<String> raw) {
        List<String> out = new ArrayList<>();
        if (raw == null) return out;
        Set<String> seen = new HashSet<>();
        for (String s : raw) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isEmpty()) continue;
            if (t.length() > MAX_LENGTH) t = t.substring(0, MAX_LENGTH);
            if (seen.add(t.toLowerCase(Locale.ROOT))) out.add(t);
            if (out.size() == MAX_COUNT) break;
        }
        return out;
    }
}
