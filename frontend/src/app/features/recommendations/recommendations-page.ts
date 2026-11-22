import { Component, OnInit, inject, signal } from '@angular/core';
import { Recommendation, Verdict } from '../../core/models';
import { AuthService } from '../../core/auth.service';
import { RecoService } from '../../core/reco.service';
import { ActivityService } from '../../core/activity.service';
import { CourseCard } from '../../shared/course-card/course-card';

/** Page "Mes recommandations" : données réelles de reco-service, avec les boutons de retour. */
@Component({
  selector: 'app-recommendations-page',
  imports: [CourseCard],
  template: `
    <h1>Mes recommandations</h1>
    @if (loading()) {
      <p class="info">Chargement de vos recommandations…</p>
    } @else if (error()) {
      <p class="info">Recommandations indisponibles pour le moment. Vérifiez que le serveur est lancé.</p>
    } @else if (recos().length === 0) {
      <p class="info">Aucune recommandation pour le moment.</p>
    }
    <section class="grid">
      @for (r of recos(); track r.course.id) {
        <app-course-card [reco]="r" [showFeedback]="true" (feedback)="onFeedback(r, $event)" />
      }
    </section>
  `,
  styles: `
    h1 { margin: 24px 0 20px; }
    .info { opacity: .7; margin-bottom: 16px; }
    .grid { display: grid; gap: 20px; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); }
  `,
})
export class RecommendationsPage implements OnInit {
  private auth = inject(AuthService);
  private reco = inject(RecoService);
  private activity = inject(ActivityService);

  recos = signal<Recommendation[]>([]);
  loading = signal(true);
  error = signal(false);

  ngOnInit(): void {
    const id = this.auth.user()?.id;
    if (id == null) {
      this.loading.set(false);
      return;
    }
    this.reco.forStudent(id, 12).subscribe({
      next: r => { this.recos.set(r); this.loading.set(false); },
      error: e => {
        console.error('Recommandations indisponibles', e);
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  onFeedback(r: Recommendation, v: Verdict) {
    this.activity.feedback(r.course.id, v === 'LIKE').subscribe({
      next: () => {},
      error: e => console.error('Retour non enregistré', e),
    });
  }

}