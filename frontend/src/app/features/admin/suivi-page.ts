import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { CourseStat, Dashboard, StatsService } from '../../core/stats.service';

interface Ranking { title: string; hint: string; rows: CourseStat[]; tone: 'cyan' | 'green' | 'red'; }

@Component({
  selector: 'app-suivi-page',
  template: `
    <div class="head">
      <div>
        <h1>Suivi de l’activité</h1>
        <p>Ce que font les étudiants : consultations, recommandations et retours.</p>
      </div>
      <button class="btn" type="button" (click)="load()" [disabled]="loading()">
        {{ loading() ? 'Chargement…' : 'Actualiser' }}
      </button>
    </div>

    @if (error()) { <p class="err" role="alert">{{ error() }}</p> }

    @if (data(); as d) {
      <div class="cards">
        <div class="card glass"><b>{{ d.totals.views }}</b><span>Consultations</span></div>
        <div class="card glass"><b>{{ d.totals.recommended }}</b><span>Recommandations faites</span></div>
        <div class="card glass"><b>{{ d.totals.accepted }}</b><span>Acceptées</span></div>
        <div class="card glass"><b>{{ d.totals.rejected }}</b><span>Rejetées</span></div>
        <div class="card glass"><b>{{ acceptance() }}</b><span>Taux d’acceptation</span></div>
      </div>

      <div class="rankings">
        @for (r of rankings(); track r.title) {
          <section class="glass box">
            <h2>{{ r.title }}</h2>
            <p class="hint">{{ r.hint }}</p>
            @if (r.rows.length === 0) {
              <p class="none">Pas encore de données.</p>
            } @else {
              <ol>
                @for (row of r.rows; track row.courseId; let i = $index) {
                  <li>
                    <span class="rank" [class]="r.tone">{{ i + 1 }}</span>
                    <span class="name" [attr.title]="row.title ?? ('Formation n°' + row.courseId)">{{ row.title ?? ('Formation n°' + row.courseId) }}</span>
                    <span class="num">{{ row.count }}</span>
                    <div class="bar"><i [class]="r.tone" [style.width.%]="percent(row, r.rows)"></i></div>
                  </li>
                }
              </ol>
            }
          </section>
        }
      </div>
    }
  `,
  styles: `
    :host { display: block; margin-top: 22px; }
    h1 { font-size: 36px; }
    h2 { font-size: 18px; margin: 0; }
    .head { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap; margin-bottom: 18px; }
    .head p, .hint { margin: 8px 0 0; color: var(--muted); }
    .hint { font-size: 13px; margin: 4px 0 16px; }
    .btn { height: 46px; padding: 0 22px; border: 0; border-radius: 999px; background: var(--primary); color: var(--ink);
      font: 600 14px 'Inter', sans-serif; cursor: pointer; }
    .btn[disabled] { opacity: .6; cursor: default; }
    .err { padding: 12px 18px; border-radius: 14px; background: #FEECEC; color: #B42318; font-weight: 500; }
    .glass { background: rgba(255,255,255,.7); border-radius: 22px; }
    .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 14px; margin-bottom: 18px; }
    .card { padding: 18px 20px; display: grid; gap: 4px; }
    .card b { font: 700 32px 'Outfit', sans-serif; color: var(--ink); font-variant-numeric: tabular-nums; }
    .card span { color: var(--muted); font-size: 14px; }
    .rankings { display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 16px; }
    .box { padding: 20px 22px; min-width: 0; }

    /* une ligne = rang | titre | nombre, puis la barre sous le titre : tout est aligné en colonnes */
    ol { list-style: none; margin: 0; padding: 0; display: grid; gap: 14px; }
    li { display: grid; grid-template-columns: 26px minmax(0, 1fr) 3.2ch; column-gap: 12px; row-gap: 6px; align-items: center; }
    .rank { width: 26px; height: 26px; border-radius: 50%; display: grid; place-items: center;
      font: 700 12px 'Inter', sans-serif; font-variant-numeric: tabular-nums; }
    .rank.cyan { background: #D8F6F8; color: #0B7F8C; }
    .rank.green { background: #DDF5E7; color: #1C7C4A; }
    .rank.red { background: #FCE4E4; color: #B42318; }
    .name { min-width: 0; font-size: 14px; font-weight: 600; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .num { text-align: right; font-size: 14px; font-weight: 700; font-variant-numeric: tabular-nums; }
    .bar { grid-column: 2 / -1; height: 8px; border-radius: 999px; background: #E7F6F8; overflow: hidden; }
    .bar i { display: block; height: 100%; border-radius: 999px; }
    .bar i.cyan { background: #19CBD8; }
    .bar i.green { background: #2FB36B; }
    .bar i.red { background: #D93B3B; }
    .none { color: var(--muted); }
  `,
})
export class SuiviPage {
  private stats = inject(StatsService);

  data = signal<Dashboard | null>(null);
  loading = signal(false);
  error = signal('');

  acceptance = computed(() => {
    const t = this.data()?.totals;
    if (!t || t.accepted + t.rejected === 0) return '—';
    return Math.round((100 * t.accepted) / (t.accepted + t.rejected)) + ' %';
  });

  rankings = computed<Ranking[]>(() => {
    const d = this.data();
    if (!d) return [];
    return [
      { title: 'Les plus consultées', hint: 'Fiches ouvertes par les étudiants', rows: d.mostViewed, tone: 'cyan' },
      { title: 'Les plus recommandées', hint: 'Proposées à le plus d’étudiants', rows: d.mostRecommended, tone: 'cyan' },
      { title: 'Recommandations acceptées', hint: 'Retour « me convient »', rows: d.mostAccepted, tone: 'green' },
      { title: 'Recommandations rejetées', hint: 'Retour « ne me convient pas »', rows: d.mostRejected, tone: 'red' },
    ];
  });

  constructor() { this.load(); }

  percent(row: CourseStat, rows: CourseStat[]): number {
    const max = Math.max(...rows.map(r => r.count), 1);
    return Math.max(4, Math.round((100 * row.count) / max));
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.stats.dashboard(10).subscribe({
      next: d => { this.data.set(d); this.loading.set(false); },
      error: (e: unknown) => {
        this.loading.set(false);
        const status = (e as HttpErrorResponse)?.status;
        this.error.set(status === 0 ? 'Serveur injoignable. Vérifiez que stats-service et la gateway sont lancés.'
          : status === 401 || status === 403 ? 'Accès réservé à l’administrateur : reconnectez-vous.'
          : 'Impossible de charger les statistiques. Vérifiez que activity-service et stats-service tournent.');
      },
    });
  }
}