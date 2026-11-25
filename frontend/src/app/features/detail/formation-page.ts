git add -A
git status --short
git commit -m "feat: fiche formation, notation, redirection admin, centrage profil"
git pushimport { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ActivityService } from '../../core/activity.service';
import { ActivityStore } from '../../core/activity.store';
import { Icon } from '../../shared/icon/icon';

const CATALOGUE = 'http://localhost:8082';

interface ApiCourse {
  id: number;
  title: string;
  university: string | null;
  level: string | null;
  category: string | null;
  description: string | null;
  skills: string | null;
  rating: number;
  reviews: number;
  hours: number;
  imageUrl: string | null;
}

/** Fiche d'une formation : description, compétences, favori et note de l'étudiant. */
@Component({
  selector: 'app-formation-page',
  imports: [Icon, RouterLink],
  template: `
    <a class="back" routerLink="/explorer"><app-icon name="back" [size]="18" /> Retour aux formations</a>

    @if (course(); as c) {
      <article class="detail">
        <div class="hero" [style.background-image]="image()">
          @if (c.category) { <span class="badge">{{ c.category }}</span> }
        </div>
        <div class="content">
          <h1>{{ c.title }}</h1>
          <div class="meta">
            <span><app-icon name="building" [size]="16" /> {{ c.university }}</span>
            <span><app-icon name="star" [size]="15" [filled]="true" /> {{ c.rating.toFixed(1) }} ({{ c.reviews }} avis)</span>
            <span><app-icon name="clock" [size]="16" /> {{ c.hours }} h</span>
            @if (c.level) { <span class="lvl">{{ c.level }}</span> }
          </div>

          <button type="button" class="fav" [class.on]="fav()" [attr.aria-pressed]="fav()" (click)="toggleFav()">
            <app-icon name="bookmark" [size]="18" [filled]="fav()" />
            {{ fav() ? 'Dans mes favoris' : 'Ajouter aux favoris' }}
          </button>

          <section>
            <h2>Description</h2>
            <p>{{ c.description || 'Aucune description pour cette formation.' }}</p>
          </section>

          @if (skills().length) {
            <section>
              <h2>Compétences visées</h2>
              <div class="chips">
                @for (s of skills(); track s) { <span class="chip">{{ s }}</span> }
              </div>
            </section>
          }

          <section>
            <h2>Votre note</h2>
            <div class="rate" role="group" aria-label="Noter la formation">
              @for (v of stars; track v) {
                <button type="button" class="star" [class.on]="v <= mine()" [attr.aria-label]="v + ' sur 5'" (click)="rate(v)">
                  <app-icon name="star" [size]="30" [filled]="v <= mine()" />
                </button>
              }
            </div>
            @if (note()) { <p class="note" role="status">{{ note() }}</p> }
          </section>
        </div>
      </article>
    } @else if (loading()) {
      <p class="info">Chargement…</p>
    } @else {
      <p class="info">Formation introuvable, ou catalogue injoignable (port 8082).</p>
    }
  `,
  styles: `
    :host { display: block; max-width: 900px; margin: 0 auto; }
    .back { display: inline-flex; align-items: center; gap: 8px; margin: 22px 0 16px; color: var(--ink);
      font: 600 14px 'Inter', sans-serif; text-decoration: none; }
    .back:hover { text-decoration: underline; }
    .info { opacity: .7; }
    .detail { background: #fff; border-radius: 22px; overflow: hidden; max-width: 900px; }
    .hero { height: 260px; background-size: cover; background-position: center; background-color: #bfeff4; position: relative; }
    .badge { position: absolute; top: 16px; right: 16px; background: #fff; border-radius: 999px; padding: 6px 14px;
      font: 600 13px 'Inter', sans-serif; color: var(--ink); }
    .content { padding: 26px 30px 32px; display: grid; gap: 22px; }
    h1 { font-size: 32px; margin: 0; line-height: 1.2; }
    h2 { font-size: 18px; margin: 0 0 8px; }
    p { margin: 0; line-height: 1.6; color: var(--ink); }
    .meta { display: flex; flex-wrap: wrap; align-items: center; gap: 10px 20px; font-size: 14px; color: var(--muted); }
    .meta span { display: inline-flex; align-items: center; gap: 6px; }
    .lvl { background: #E7F6F8; color: var(--ink); border-radius: 999px; padding: 4px 12px; font-weight: 600; }
    .fav { justify-self: start; display: inline-flex; align-items: center; gap: 8px; border: 1.5px solid var(--primary-dark);
      background: #fff; color: var(--primary-dark); border-radius: 999px; padding: 10px 20px; font: 600 14px 'Inter', sans-serif; cursor: pointer; }
    .fav.on { background: var(--primary-dark); color: #fff; }
    .chips { display: flex; flex-wrap: wrap; gap: 8px; }
    .chip { background: #E7F6F8; color: var(--ink); border-radius: 999px; padding: 7px 14px; font: 600 13px 'Inter', sans-serif; }
    .rate { display: flex; gap: 4px; }
    .star { border: 0; background: none; padding: 2px; cursor: pointer; color: #C9D6DA; }
    .star.on { color: var(--star); }
    .star:hover { color: var(--star); }
    .note { margin-top: 8px; font-size: 14px; color: var(--muted); }
    .fav:focus-visible, .star:focus-visible, .back:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
    @media (max-width: 700px) { h1 { font-size: 26px; } .hero { height: 180px; } .content { padding: 20px; } }
  `,
})
export class FormationPage {
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private activity = inject(ActivityService);
  private store = inject(ActivityStore);

  readonly stars = [1, 2, 3, 4, 5];

  course = signal<ApiCourse | null>(null);
  loading = signal(true);
  mine = signal(0);
  note = signal('');

  skills = computed(() => (this.course()?.skills ?? '').split(/[,;]/).map(s => s.trim()).filter(Boolean));
  fav = computed(() => { const c = this.course(); return !!c && this.store.favorites().has(c.id); });
  image = computed(() => {
    const u = this.course()?.imageUrl;
    if (!u) return 'none';
    return `url(${u.startsWith('http') ? u : CATALOGUE + u})`;
  });

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!(id > 0)) { this.loading.set(false); return; }

    this.http.get<ApiCourse>(`${CATALOGUE}/api/courses/${id}`).subscribe({
      next: c => {
        this.course.set(c);
        this.loading.set(false);
        this.activity.view(id).subscribe({ error: () => {} });
      },
      error: () => this.loading.set(false),
    });
    this.activity.me().subscribe({
      next: m => this.mine.set(m.ratings?.[String(id)] ?? 0),
      error: () => {},
    });
  }

  rate(v: number): void {
    const c = this.course();
    if (!c) return;
    const before = this.mine();
    this.mine.set(v);
    this.note.set('');
    this.activity.rate(c.id, v).subscribe({
      next: () => this.note.set('Merci, votre note est enregistrée.'),
      error: () => { this.mine.set(before); this.note.set('Note non enregistrée : reconnectez-vous ou réessayez.'); },
    });
  }

  toggleFav(): void {
    const c = this.course();
    if (c) this.store.toggle(c.id);
  }
}