package dz.recoia.recoservice.engine;

import java.util.List;

/** Demonstration en ligne de commande : affiche les recommandations d'une etudiante. */
public final class RecoDemo {

    public static void main(String[] args) {
        Recommender recommender = new Recommender(TestData.catalogue());

        StudentProfile imene = new StudentProfile(1, List.of("data science", "python"),
                List.of(Interaction.favorite(1), Interaction.view(5), Interaction.rating(3, 5)));
        List<StudentProfile> others = List.of(
                new StudentProfile(2, List.of(), List.of(Interaction.favorite(1), Interaction.rating(3, 5), Interaction.favorite(2))),
                new StudentProfile(3, List.of(), List.of(Interaction.favorite(1), Interaction.like(2), Interaction.like(8))),
                new StudentProfile(4, List.of(), List.of(Interaction.favorite(11), Interaction.favorite(10))));

        System.out.println("Recommandations pour Imene (intérêts : data science, python)\n");
        for (Recommendation r : recommender.recommend(imene, others, 5)) {
            String title = TestData.catalogue().stream().filter(c -> c.id() == r.courseId()).findFirst().orElseThrow().title();
            System.out.printf("%3.0f%%  %-40s %s%n", r.score() * 100, title, r.explanation());
        }
    }
}
