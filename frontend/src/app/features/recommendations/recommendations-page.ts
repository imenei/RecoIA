import { Component, signal } from '@angular/core';
import { Recommendation, Verdict } from '../../core/models';
import { CourseCard } from '../../shared/course-card/course-card';

/** Page "Pour toi". Donnees fictives : remplacer par un appel GET /api/recommendations. */
@Component({
  selector: 'app-recommendations-page',
  imports: [CourseCard],
  template: `
    <h1>Recommandé pour toi</h1>
    <section class="grid">
      @for (r of recos(); track r.course.id) {
        <app-course-card [reco]="r" (feedback)="onFeedback(r, $event)" />
      }
    </section>
  `,
  styles: `
    h1 { margin-bottom: 20px; }
    .grid { display: grid; gap: 20px; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); }
  `,
})
export class RecommendationsPage {
  recos = signal<Recommendation[]>([
    { score: 0.98, rating: 5, explanation: 'Similaire à tes favoris : Python, SQL',
      course: { id: 1, title: 'Machine Learning avec Python', category: 'Data Science', level: 'Avancé',
        imageUrl: 'https://picsum.photos/seed/1/400/250' } },
    { score: 0.95, rating: 4, explanation: 'Apprécié par des étudiants proches de toi',
      course: { id: 2, title: 'Architecture microservices', category: 'Informatique', level: 'Intermédiaire',
        imageUrl: 'https://picsum.photos/seed/2/400/250' } },
    { score: 0.93, rating: 4, explanation: 'Basé sur tes centres d’intérêt : data',
      course: { id: 3, title: 'Analyse de données métier', category: 'Business', level: 'Débutant',
        imageUrl: 'https://picsum.photos/seed/3/400/250' } },
  ]);

  onFeedback(r: Recommendation, v: Verdict) {
    console.log('Retour', v, 'sur', r.course.title); // TODO: POST /api/activity/feedback
  }
}
