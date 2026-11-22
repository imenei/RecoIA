# Moteur de recommandation de RecoIA

Code : `services/reco-service/src/main/java/dz/recoia/recoservice/engine/`
Aucune API externe : tout est calcule localement. Le moteur est en Java pur (pas de Spring).

## Principe

Pour un etudiant, chaque formation recoit un score hybride :

```
score = 0.5 x contenu + 0.3 x collaboratif + 0.2 x retours
```

| Composante | Calcul |
|---|---|
| **Contenu** | Vecteurs TF-IDF des formations (titre x2, categorie, competences, description). Le profil de l'etudiant est la somme des vecteurs de ses centres d'interet, favoris, formations aimees, bien notees (>= 4) et vues (poids 0.2). Score = similarite cosinus profil / formation. |
| **Collaboratif** | Appreciation implicite par etudiant : favori 0.8, "me convient" 1, "ne me convient pas" -1, note (n-3)/2, vue 0.2. Les 10 voisins les plus proches (cosinus >= 0.05) donnent une moyenne ponderee pour chaque formation. |
| **Retours** | Proximite avec une formation "me convient" (bonus) moins proximite avec une formation "ne me convient pas" (malus). |

- **Exclues** : formations deja notees, mises en favori, aimees ou rejetees.
- **Score affiche** : compatibilite *relative* (1 = meilleure formation de la liste), `rawScore` donne la valeur brute.
- **Demarrage a froid** : sans profil ni voisins, on propose les formations les plus appreciees des autres ; sans aucune donnee, l'ordre du catalogue (score 0, badge masque dans l'interface).
- **Explications** : "Similaire a tes favoris : X", "Correspond a ton interet : Y", "Apprecie par N etudiants proches de toi".

## Utilisation

```java
Recommender reco = new Recommender(courses);                    // courses : catalogue-service
List<Recommendation> top = reco.recommend(student, others, 10); // others : activite des autres etudiants
```

## Tests

```bash
cd services/reco-service
mvn test                       # RecommenderTest (JUnit 5)
```

`RecoDemo` (dans `src/test`) affiche un exemple de recommandations : lancez sa methode `main` depuis l'IDE.
