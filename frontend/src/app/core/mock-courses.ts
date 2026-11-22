/** Catalogue fictif. A remplacer par GET /api/courses (catalogue-service). */
export interface Formation {
  id: number;
  title: string;
  university: string;
  level: string;
  badge: string;
  rating: number;
  reviews: string;
  hours: number;
  image: string;
}

export const NIVEAUX = ['Licence', 'Master', 'Doctorat', 'Formation courte', 'Certifications'];

const A = 'Université d’Alger 1';
const S = 'Université des Sciences';

const RAW: [string, string, string, string, number, string, number][] = [
  ['Développement Web avec React', A, 'Licence', 'Recommandée', 4.8, '2.1k', 40],
  ['Intelligence Artificielle et Machine Learning', S, 'Master', 'Populaire', 4.7, '1.8k', 35],
  ['Data Science avec Python', A, 'Master', 'Nouveau', 4.6, '1.5k', 30],
  ['Cybersécurité : Fondamentaux', S, 'Licence', '+ de 10k étudiants', 4.9, '3.2k', 45],
  ['Anglais Professionnel', A, 'Formation courte', 'Très apprécié', 4.7, '1.2k', 50],
  ['Analyse de Données avec Python', S, 'Licence', 'En tendance', 4.8, '1.6k', 40],
  ['Modélisation 3D avec Blender', A, 'Certifications', 'Nouveau', 4.6, '1.1k', 35],
  ['Gestion de Projets Agiles', S, 'Master', 'Incontournable', 4.7, '1.4k', 30],
  ['Design UI/UX', A, 'Certifications', 'Nouveau', 4.6, '980', 28],
  ['Bases de données SQL', S, 'Licence', 'Populaire', 4.5, '1.7k', 32],
  ['Marketing Digital', A, 'Formation courte', 'En tendance', 4.4, '2.0k', 18],
  ['Méthodes de recherche en IA', S, 'Doctorat', 'Nouveau', 4.8, '410', 60],
];

export const FORMATIONS: Formation[] = RAW.map(
  ([title, university, level, badge, rating, reviews, hours], i) => ({
    id: i + 1, title, university, level, badge, rating, reviews, hours,
    image: `https://picsum.photos/seed/rec${i + 1}/400/250`,
  }),
);