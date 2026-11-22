package dz.recoia.authservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Genere des etudiants de test coherents : chacun appartient a un "profil" (data, web, business...)
 * et recoit 2 a 4 centres d'interet de ce profil, plus parfois un interet d'un autre profil.
 */
public final class SampleUsers {

    public record Spec(String fullName, String email, String cluster, List<String> interests) { }

    public static final List<String> CLUSTERS = List.of("data", "web", "business", "design", "langues", "sante");

    private static final List<List<String>> CLUSTER_INTERESTS = List.of(
            List.of("data science", "python", "machine learning", "statistiques", "intelligence artificielle"),
            List.of("développement web", "react", "javascript", "java", "microservices"),
            List.of("marketing digital", "gestion de projet", "entrepreneuriat", "finance", "management"),
            List.of("design", "ux", "photographie", "illustration", "montage vidéo"),
            List.of("anglais", "communication", "espagnol", "rédaction", "prise de parole"),
            List.of("nutrition", "premiers secours", "psychologie", "santé publique", "sport"));

    private static final List<String> FIRST_NAMES = List.of(
            "Amine", "Imane", "Yacine", "Sara", "Karim", "Lina", "Walid", "Nesrine", "Riad", "Meriem",
            "Anis", "Salma", "Hakim", "Ikram", "Sofiane", "Asma", "Djamel", "Rania", "Bilal", "Yasmine",
            "Mehdi", "Camélia", "Fares", "Nour", "Samir", "Lamia", "Zakaria", "Hiba", "Tarek", "Amira");

    private static final List<String> LAST_NAMES = List.of(
            "Benali", "Mansouri", "Haddad", "Bouzid", "Cherif", "Slimani", "Khelifi", "Messaoudi", "Brahimi",
            "Belkacem", "Taleb", "Guerfi", "Hamidi", "Rahmani", "Saadi");

    private SampleUsers() { }

    public static List<Spec> students(int count, long seed) {
        Random random = new Random(seed);
        List<Spec> out = new ArrayList<>();
        for (int n = 1; n <= count; n++) {
            int clusterIndex = (n - 1) % CLUSTERS.size();
            List<String> pool = new ArrayList<>(CLUSTER_INTERESTS.get(clusterIndex));
            java.util.Collections.shuffle(pool, random);
            List<String> interests = new ArrayList<>(pool.subList(0, 2 + random.nextInt(3)));   // 2 a 4

            if (random.nextInt(5) == 0) {   // 20 % : un interet hors profil (bruit realiste)
                List<String> other = CLUSTER_INTERESTS.get((clusterIndex + 1 + random.nextInt(CLUSTERS.size() - 1)) % CLUSTERS.size());
                String extra = other.get(random.nextInt(other.size()));
                if (!interests.contains(extra)) interests.add(extra);
            }

            String name = FIRST_NAMES.get((n - 1) % FIRST_NAMES.size()) + " "
                    + LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
            out.add(new Spec(name, String.format("etudiant%02d@recoia.dz", n), CLUSTERS.get(clusterIndex), interests));
        }
        return out;
    }
}
