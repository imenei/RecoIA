package dz.recoia.recoservice.engine;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Decoupe un texte en mots-cles : minuscules, sans accents, sans mots vides. */
final class TextAnalyzer {

    private static final Set<String> STOP_WORDS = Set.of(
            // francais
            "les", "des", "une", "pour", "avec", "dans", "sur", "par", "aux", "ces", "est", "son", "ses", "que",
            "qui", "vous", "nous", "vos", "votre", "leur", "plus", "pas", "mais", "comme", "tout", "tous", "cette",
            "cours", "formation", "apprendre", "decouvrez", "permet", "ainsi", "afin", "entre", "sans", "elle",
            // anglais
            "and", "the", "for", "with", "you", "your", "from", "this", "that", "are", "will", "have", "has",
            "course", "learn", "how", "what", "can", "all", "their", "about", "into", "using", "use");

    private TextAnalyzer() { }

    static List<String> tokens(String text) {
        if (text == null || text.isBlank()) return List.of();
        String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        List<String> out = new ArrayList<>();
        for (String raw : normalized.split("[^a-z0-9]+")) {
            if (raw.length() < 3 || STOP_WORDS.contains(raw)) continue;
            out.add(stem(raw));
        }
        return out;
    }

    /** Racinisation tres legere : retire le pluriel. */
    private static String stem(String t) {
        if (t.length() > 4 && (t.endsWith("s") || t.endsWith("x"))) return t.substring(0, t.length() - 1);
        return t;
    }
}
