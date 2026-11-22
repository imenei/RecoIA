package dz.recoia.recoservice.engine;

import java.util.List;

/** Profil d'un etudiant : centres d'interet + historique d'activite. */
public record StudentProfile(long studentId, List<String> interests, List<Interaction> history) {

    public StudentProfile {
        interests = interests == null ? List.of() : List.copyOf(interests);
        history = history == null ? List.of() : List.copyOf(history);
    }
}
