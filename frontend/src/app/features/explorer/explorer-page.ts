import { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Formation, NIVEAUX } from '../../core/mock-courses';
import { Icon } from '../../shared/icon/icon';
import { RouterLink } from '@angular/router';
import { ActivityService } from '../../core/activity.service';
import { ActivityStore } from '../../core/activity.store';

const CATALOGUE = 'http://localhost:8082';

/** Formation telle que renvoyee par catalogue-service. */
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
  badge: string | null;
}

type Sort = 'pertinence' | 'note' | 'duree';

@Component({
  selector: 'app-explorer-page',
  imports: [Icon, RouterLink],
  templateUrl: './explorer-page.html',
  styleUrl: './explorer-page.scss',
})
export class ExplorerPage {
  private http = inject(HttpClient);
  private activity = inject(ActivityService);
  private store = inject(ActivityStore);

  readonly niveaux = NIVEAUX;
  readonly pageSize = 8;

  all = signal<Formation[]>([]);
  loading = signal(true);
  error = signal(false);

  query = signal('');
  level = signal('');
  sort = signal<Sort>('pertinence');
  minRating = signal(0);
  panel = signal(false);
  visible = signal(this.pageSize);
  saved = computed(() => [...this.store.favorites()]);

  filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    let list = this.all().filter(f =>
      (!this.level() || f.level === this.level()) &&
      f.rating >= this.minRating() &&
      (!q || (f.title + ' ' + f.university + ' ' + f.level + ' ' + f.badge).toLowerCase().includes(q)));
    if (this.sort() === 'note') list = [...list].sort((a, b) => b.rating - a.rating);
    if (this.sort() === 'duree') list = [...list].sort((a, b) => a.hours - b.hours);
    return list;
  });
  shown = computed(() => this.filtered().slice(0, this.visible()));

  emptyTitle = computed(() =>
    this.loading() ? 'Chargement…' : this.error() ? 'Catalogue injoignable' : 'Aucune formation trouvée');
  emptyHint = computed(() =>
    this.loading() ? '' : this.error()
      ? 'Vérifiez que catalogue-service est lancé (port 8082), puis rechargez la page.'
      : 'Essayez un autre mot-clé ou un autre niveau.');

  constructor() {
    this.http.get<ApiCourse[]>(`${CATALOGUE}/api/courses/all`).subscribe({
      next: list => { this.all.set(list.map(c => this.toFormation(c))); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); },
    });
  }

  private toFormation(c: ApiCourse): Formation {
    return {
      id: c.id,
      title: c.title,
      university: c.university ?? '',
      level: c.level ?? '',
      badge: c.badge || c.category || '',
      rating: Math.round(c.rating * 10) / 10,
      reviews: c.reviews >= 1000 ? (c.reviews / 1000).toFixed(1) + 'k' : String(c.reviews),
      hours: c.hours,
      image: c.imageUrl ? (c.imageUrl.startsWith('http') ? c.imageUrl : CATALOGUE + c.imageUrl) : '',
    };
  }

  setQuery(v: string) { this.query.set(v); this.visible.set(this.pageSize); }
  setLevel(v: string) { this.level.set(v); this.visible.set(this.pageSize); }
  setSort(v: string) { this.sort.set(v as Sort); }
  setMin(v: string) { this.minRating.set(+v); this.visible.set(this.pageSize); }
  more() { this.visible.update(n => n + this.pageSize); }
  toggleSave(id: number) {
    this.store.toggle(id);
  }

  view(id: number) {
    this.activity.view(id).subscribe({ error: () => {} });
  }
}