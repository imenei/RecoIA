import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Recommendation } from '../../core/models';
import { API_URL } from '../../core/api.config';
import { AuthService } from '../../core/auth.service';
import { RecoService } from '../../core/reco.service';
import { ActivityService } from '../../core/activity.service';
import { ActivityStore } from '../../core/activity.store';
import { CourseCard } from '../../shared/course-card/course-card';
import { Icon } from '../../shared/icon/icon';

interface CatalogueCourse { id: number; title: string; rating: number; category: string | null; }

const LANGUES: Record<string, string> = { FR: 'Français', EN: 'Anglais', ANY: 'Peu importe' };

@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, CourseCard, Icon],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage implements OnInit {
  private auth = inject(AuthService);
  private reco = inject(RecoService);
  private activity = inject(ActivityService);
  private store = inject(ActivityStore);
  private http = inject(HttpClient);

  /** Prénom de la personne connectée. */
  get userName(): string {
    return this.auth.firstName() || 'vous';
  }

  heroImage = signal(true);   // passe à false si public/images/hero.png est absent

  // Réseau de neurones décoratif (affiché si hero.png est absent)
  nodes: [number, number][] = [[120,60],[170,35],[230,40],[275,70],[290,115],[265,160],[215,185],[160,180],
    [115,150],[100,105],[150,85],[205,80],[250,110],[210,130],[160,125],[185,105]];
  edges: [number, number][] = [[0,1],[1,2],[2,3],[3,4],[4,5],[5,6],[6,7],[7,8],[8,9],[9,0],[0,10],[1,10],[1,11],
    [2,11],[3,12],[4,12],[10,11],[11,12],[10,15],[11,15],[12,15],[15,13],[13,14],[14,15],[9,14],[8,14],[7,14],
    [6,13],[5,13],[12,13],[10,9]];

  // Données chargées depuis le backend (déclarées AVANT les valeurs calculées qui les utilisent)
  recos = signal<Recommendation[]>([]);
  courses = signal<CatalogueCourse[]>([]);
  studentCount = signal<number | null>(null);
  acceptance = signal<number | null>(null);   // % de "Me convient" parmi tous les retours

  stats = computed(() => {
    const list = this.courses();
    const rated = list.filter(c => c.rating > 0);
    const avg = rated.length ? rated.reduce((s, c) => s + c.rating, 0) / rated.length : null;
    const acc = this.acceptance();
    const students = this.studentCount();
    return [
      { icon: 'cap', value: list.length ? String(list.length) : '–', label: 'Formations disponibles' },
      { icon: 'users', value: students !== null ? String(students) : '–', label: 'Étudiants inscrits' },
      { icon: 'star', value: avg !== null ? avg.toFixed(1) + '/5' : '–', label: 'Note moyenne du catalogue' },
      { icon: 'trend', value: acc !== null ? acc + '%' : '–', label: "Taux d'acceptation" },
    ];
  });

  /** Complétude du profil : 5 champs renseignés sur 5 = 100 %. */
  profile = computed(() => {
    const u = this.auth.user();
    const interests = u?.interests ?? [];
    const filled = [!!u?.fullName, interests.length > 0, !!u?.track, !!u?.language, !!u?.duration];
    const pct = Math.round((100 * filled.filter(Boolean).length) / filled.length);
    return {
      pct,
      dash: (pct * 3.455).toFixed(1),
      track: u?.track || 'Non précisé',
      interests: interests.length ? interests.join(', ') : 'Non précisés',
      language: LANGUES[u?.language ?? ''] ?? 'Non précisée',
    };
  });

  /** Les 3 premiers favoris, avec leur titre. */
  recentFavorites = computed(() => {
    const byId = new Map(this.courses().map(c => [c.id, c]));
    return [...this.store.favorites()].reverse().slice(0, 3)
      .map(id => byId.get(id)).filter((c): c is CatalogueCourse => !!c);
  });

  ngOnInit(): void {
    const id = this.auth.user()?.id;
    if (id != null) {
      this.reco.forStudent(id, 3).subscribe({
        next: r => this.recos.set(r),
        error: e => console.error('Recommandations indisponibles', e),
      });
    }
    this.http.get<CatalogueCourse[]>(API_URL + '/courses/all').subscribe({
      next: l => this.courses.set(l),
      error: () => {},
    });
    this.http.get<{ students: number }>(API_URL + '/reco/stats').subscribe({
      next: s => this.studentCount.set(s.students),
      error: () => {},
    });
    this.activity.me().subscribe({
      next: m => {
        const votes = Object.values(m.feedback ?? {});
        const likes = votes.filter(v => v > 0).length;
        this.acceptance.set(votes.length ? Math.round((100 * likes) / votes.length) : null);
      },
      error: () => {},
    });
  }
}