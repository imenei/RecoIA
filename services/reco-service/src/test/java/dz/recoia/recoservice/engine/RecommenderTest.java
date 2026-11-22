package dz.recoia.recoservice.engine;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommenderTest {

    private final Recommender recommender = new Recommender(TestData.catalogue());

    private static StudentProfile student(long id, List<String> interests, Interaction... history) {
        return new StudentProfile(id, interests, List.of(history));
    }

    private static Optional<Recommendation> find(List<Recommendation> list, long courseId) {
        return list.stream().filter(r -> r.courseId() == courseId).findFirst();
    }

    @Test
    void interestsAndFavoritesDriveContentScore() {
        StudentProfile imene = student(1, List.of("data science", "python"), Interaction.favorite(1));
        List<Recommendation> top = recommender.recommend(imene, List.of(), 3);

        assertEquals(3, top.size());
        assertTrue(List.of(2L, 3L, 4L).contains(top.get(0).courseId()),
                "la premiere recommandation doit venir de la Data Science : " + top.get(0));
        assertTrue(top.stream().noneMatch(r -> r.courseId() == 1), "un favori ne doit pas etre recommande");
    }

    @Test
    void alreadyRatedOrRejectedCoursesAreExcluded() {
        StudentProfile s = student(1, List.of("python"),
                Interaction.rating(3, 5), Interaction.dislike(2), Interaction.like(4));
        List<Recommendation> all = recommender.recommend(s, List.of(), 0);

        assertTrue(find(all, 3).isEmpty() && find(all, 2).isEmpty() && find(all, 4).isEmpty());
    }

    @Test
    void scoresAreBoundedAndSorted() {
        StudentProfile s = student(1, List.of("web", "design"), Interaction.favorite(5));
        List<Recommendation> all = recommender.recommend(s, List.of(), 0);

        double previous = Double.MAX_VALUE;
        for (Recommendation r : all) {
            assertTrue(r.score() >= 0 && r.score() <= 1, "score hors de [0, 1] : " + r);
            assertTrue(r.rawScore() <= previous, "liste non triee");
            previous = r.rawScore();
        }
        assertEquals(1.0, all.get(0).score());
    }

    @Test
    void likeRaisesAndDislikeLowersSimilarCourses() {
        StudentProfile base = student(1, List.of("python"));
        double before = find(recommender.recommend(base, List.of(), 0), 2).orElseThrow().rawScore();

        StudentProfile liked = student(1, List.of("python"), Interaction.like(1));
        double afterLike = find(recommender.recommend(liked, List.of(), 0), 2).orElseThrow().rawScore();

        StudentProfile disliked = student(1, List.of("python"), Interaction.dislike(1));
        double afterDislike = find(recommender.recommend(disliked, List.of(), 0), 2).orElseThrow().rawScore();

        assertTrue(afterLike > before, "un 'me convient' doit remonter les formations proches");
        assertTrue(afterDislike < before, "un 'ne me convient pas' doit faire baisser les formations proches");
    }

    @Test
    void similarStudentsInfluenceRecommendations() {
        StudentProfile me = student(1, List.of(), Interaction.favorite(5), Interaction.rating(8, 5));
        // deux etudiants aux gouts proches des miens, qui apprecient aussi la gestion de projet (10)
        List<StudentProfile> peers = new ArrayList<>();
        peers.add(student(2, List.of(), Interaction.favorite(5), Interaction.rating(8, 5), Interaction.favorite(10)));
        peers.add(student(3, List.of(), Interaction.favorite(5), Interaction.like(10)));

        Recommendation withPeers = find(recommender.recommend(me, peers, 0), 10).orElseThrow();
        Recommendation without = find(recommender.recommend(me, List.of(), 0), 10).orElseThrow();

        assertTrue(withPeers.rawScore() > without.rawScore(), "les voisins doivent faire monter la formation 10");
        assertTrue(withPeers.explanation().contains("étudiant"), "explication : " + withPeers.explanation());
    }

    @Test
    void explanationMentionsMatchedInterest() {
        StudentProfile s = student(1, List.of("design"));
        Recommendation top = recommender.recommend(s, List.of(), 1).get(0);

        assertEquals(15L, top.courseId());
        assertTrue(top.explanation().contains("Correspond à ton intérêt : design"), top.explanation());
    }

    @Test
    void explanationMentionsFavoriteForSimilarCourse() {
        StudentProfile s = student(1, List.of(), Interaction.favorite(1));
        Recommendation top = recommender.recommend(s, List.of(), 1).get(0);

        assertTrue(top.explanation().startsWith("Similaire à tes favoris : Machine Learning avec Python"),
                top.explanation());
    }

    @Test
    void coldStartFallsBackToPopularity() {
        StudentProfile newcomer = student(99, List.of());
        List<StudentProfile> peers = List.of(
                student(2, List.of(), Interaction.favorite(14)),
                student(3, List.of(), Interaction.like(14)),
                student(4, List.of(), Interaction.rating(14, 5), Interaction.favorite(11)));

        List<Recommendation> recos = recommender.recommend(newcomer, peers, 2);

        assertEquals(14L, recos.get(0).courseId());
        assertTrue(recos.get(0).explanation().startsWith("Populaire"), recos.get(0).explanation());
    }

    @Test
    void emptyEverywhereStillReturnsCatalogue() {
        List<Recommendation> recos = recommender.recommend(student(1, List.of()), List.of(), 5);

        assertEquals(5, recos.size());
        assertFalse(recos.get(0).explanation().isBlank());
    }

    @Test
    void textAnalyzerIgnoresAccentsCaseAndPlurals() {
        assertEquals(TextAnalyzer.tokens("reseau securite"), TextAnalyzer.tokens("Réseaux SÉCURITÉ"));
    }
}
