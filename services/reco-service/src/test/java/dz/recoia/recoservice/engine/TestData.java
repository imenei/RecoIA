package dz.recoia.recoservice.engine;

import java.util.List;

/** Petit catalogue de 16 formations pour les tests et la demonstration. */
final class TestData {

    private TestData() { }

    static List<Course> catalogue() {
        return List.of(
            new Course(1, "Machine Learning avec Python", "Apprenez les algorithmes de machine learning, la régression et la classification avec Python et scikit-learn", "Data Science", "Intermédiaire", "python machine learning scikit-learn statistiques"),
            new Course(2, "Deep Learning avec PyTorch", "Réseaux de neurones profonds, vision par ordinateur et entraînement de modèles avec PyTorch", "Data Science", "Avancé", "deep learning pytorch réseaux de neurones python"),
            new Course(3, "Analyse de données avec Pandas", "Nettoyer, transformer et visualiser des données avec Python et Pandas", "Data Science", "Débutant", "python pandas visualisation données"),
            new Course(4, "Statistiques pour la Data Science", "Probabilités, tests d'hypothèses et statistiques inférentielles pour analyser des données", "Data Science", "Débutant", "statistiques probabilités données"),
            new Course(5, "Développement Web avec React", "Créer des interfaces web modernes avec React, composants et hooks", "Informatique", "Avancé", "javascript react web frontend"),
            new Course(6, "Architecture Microservices avec Spring", "Concevoir des microservices avec Spring Boot, API REST et gRPC", "Informatique", "Avancé", "java spring microservices api"),
            new Course(7, "Introduction à la Cybersécurité", "Sécurité des réseaux, cryptographie, menaces et bonnes pratiques", "Informatique", "Débutant", "sécurité réseaux cryptographie"),
            new Course(8, "Bases de données SQL", "Modélisation relationnelle, requêtes SQL et PostgreSQL", "Informatique", "Intermédiaire", "sql postgresql bases de données"),
            new Course(9, "Marketing Digital", "Stratégie de contenu, réseaux sociaux, référencement et publicité en ligne", "Business", "Débutant", "marketing seo réseaux sociaux"),
            new Course(10, "Gestion de projet agile", "Scrum, Kanban, planification et pilotage d'équipe", "Business", "Intermédiaire", "scrum kanban gestion projet"),
            new Course(11, "Anglais professionnel", "Communication orale et écrite en entreprise, réunions et présentations", "Langues", "Intermédiaire", "anglais communication entreprise"),
            new Course(12, "Espagnol débutant", "Vocabulaire, grammaire de base et conversations du quotidien", "Langues", "Débutant", "espagnol grammaire conversation"),
            new Course(13, "Nutrition et bien-être", "Alimentation équilibrée, hygiène de vie et prévention", "Santé", "Débutant", "nutrition alimentation prévention"),
            new Course(14, "Premiers secours", "Gestes de secours, réanimation et urgences du quotidien", "Santé", "Débutant", "secours réanimation urgences"),
            new Course(15, "Design UI/UX", "Conception d'interfaces, prototypage et recherche utilisateur", "Arts et design", "Intermédiaire", "figma ux interface"),
            new Course(16, "Photographie numérique", "Composition, lumière et retouche photo", "Arts et design", "Débutant", "photographie lumière retouche"));
    }
}
