import { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { API_URL } from '../../core/api.config';
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
  rating: number;
  reviews: number;
  hours: number;
  imageUrl: string | null;
}

@Component({
  selector: 'app-favorites-page',
  imports: [Icon, RouterLink],
  template: `
    <h1>Mes favoris</h1>
    <p class="sub">Les formations que vous avez enregistrées.</p>

    @if (items().length) {
      <section class="grid">
        @for (c of items(); track c.id) {
          <article class="fc">
            <div class="img" [style.background-image]="image(c)">
              <span class="badge">{{ c.category }}</span>
            </div>
            <div class="body">
              <h3>{{ c.title }}</h3>
              <div class="uni"><app-icon name="building" [size]="15" /> {{ c.university }}</div>
              <div class="meta">
                <span><app-icon name="star" [size]="14" [filled]="true" /> {{ c.rating.toFixed(1) }}</span>
                <span><app-icon name="clock" [size]="15" /> {{ c.hours }}h</span>
                @if (c.level) { <span class="lvl">{{ c.level }}</span> }
              </div>
              <div class="foot">
                <button class="save" (click)="remove(c.id)" aria-label="Retirer des favoris">
                  <app-icon name="bookmark" [size]="22" [filled]="true" />
                </button>
                <a class="more" [routerLink]="['/formation', c.id]">Voir détails <app-icon name="arrow" [size]="15" /></a>
              </div>
            </div>
          </article>
        }
      </section>
    } @else if (loading()) {
      <p class="info">Chargement…</p>
    } @else if (error()) {
      <p class="info">Catalogue injoignable. Vérifiez que catalogue-service est lancé.</p>
    } @else {
      <div class="empty">
        <h2>Aucun favori pour le moment</h2>
        <p>Enregistrez des formations depuis l'explorateur ou vos recommandations.</p>
        <a routerLink="/explorer" class="more">Explorer les formations <app-icon name="arrow" [size]="15" /></a>
      </div>
    }
  `,
  styles: `
    h1 { margin: 24px 0 4px; }
    .sub { margin: 0 0 20px; color: var(--muted); }
    .info { opacity: .7; }
    .grid { display: grid; gap: 20px; grid-template-columns: repeat(auto-fill, minmax(270px, 1fr)); }
    .fc { background: #fff; border-radius: 18px; overflow: hidden; }
    .img { height: 128px; background-size: cover; background-position: center; background-color: #bfeff4;
      position: relative; }
    .badge { position: absolute; top: 12px; right: 12px; background: #fff; border-radius: 999px;
      padding: 4px 12px; font: 600 12px 'Inter', sans-serif; color: var(--ink); }
    .body { padding: 14px 16px 16px; display: grid; gap: 10px; }
    h3 { font-size: 17px; line-height: 1.25; min-height: 42px; margin: 0; }
    .uni, .meta { display: flex; align-items: center; gap: 12px; font-size: 13px; color: var(--muted); flex-wrap: wrap; }
    .uni { gap: 6px; }
    .meta span { display: inline-flex; align-items: center; gap: 5px; }
    .foot { display: flex; align-items: center; justify-content: space-between; margin-top: 4px; }
    .save { background: none; border: 0; cursor: pointer; padding: 2px; color: var(--primary-dark); }
    .more { display: inline-flex; align-items: center; gap: 6px; border: 0; border-radius: 999px;
      padding: 9px 16px; background: #19D3DF; color: var(--ink); font: 600 13px 'Inter', sans-serif;
      cursor: pointer; text-decoration: none; }
    .save:focus-visible, .more:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
    .empty { background: #fff; border-radius: 18px; padding: 40px 24px; text-align: center; display: grid;
      gap: 10px; justify-items: center; }
  `,
})
export class FavoritesPage {
  private http = inject(HttpClient);
  private store = inject(ActivityStore);
  private activity = inject(ActivityService);

  all = signal<ApiCourse[]>([]);
  catalogueLoading = signal(true);
  error = signal(false);

  loading = computed(() => this.catalogueLoading() || !this.store.loaded());
  items = computed(() => this.all().filter(c => this.store.favorites().has(c.id)));

  constructor() {
    this.http.get<ApiCourse[]>(`${API_URL}/courses/all`).subscribe({
      next: list => { this.all.set(list); this.catalogueLoading.set(false); },
      error: () => { this.error.set(true); this.catalogueLoading.set(false); },
    });
  }

  image(c: ApiCourse): string {
    if (!c.imageUrl) return 'none';
    const url = c.imageUrl.startsWith('http') ? c.imageUrl : CATALOGUE + c.imageUrl;
    return `url(${url})`;
  }

  remove(id: number): void { this.store.toggle(id); }
  view(id: number): void { this.activity.view(id).subscribe({ error: () => {} }); }
}