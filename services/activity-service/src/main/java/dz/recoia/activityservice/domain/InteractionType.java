package dz.recoia.activityservice.domain;

/**
 * VIEW        : une consultation (une ligne par consultation).
 * RECOMMENDED : la formation a ete proposee a l'etudiant dans ses recommandations.
 * RATING      : note de l'etudiant (score 1 a 5), une ligne par etudiant et formation.
 * FAVORITE    : score 1 = en favori, 0 = retire des favoris.
 * FEEDBACK    : retour sur une recommandation, score 1 = me convient, -1 = ne me convient pas.
 */
public enum InteractionType { VIEW, RECOMMENDED, RATING, FAVORITE, FEEDBACK }
