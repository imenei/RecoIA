import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { GRAPHQL_URL } from './api.config';
import { AuthService } from './auth.service';

export interface CourseStat { courseId: number; title: string | null; count: number; }
export interface Totals { views: number; recommended: number; accepted: number; rejected: number; }
export interface Dashboard {
  totals: Totals;
  mostViewed: CourseStat[];
  mostRecommended: CourseStat[];
  mostAccepted: CourseStat[];
  mostRejected: CourseStat[];
}

const STATS = 'courseId title count';
const QUERY = `query Dashboard($limit: Int) {
  dashboard(limit: $limit) {
    totals { views recommended accepted rejected }
    mostViewed { ${STATS} }
    mostRecommended { ${STATS} }
    mostAccepted { ${STATS} }
    mostRejected { ${STATS} }
  }
}`;

interface GraphQlResponse { data?: { dashboard: Dashboard } | null; errors?: { message: string }[]; }

/** Tableau de bord de l'administrateur : requete GraphQL envoyee a stats-service via la gateway. */
@Injectable({ providedIn: 'root' })
export class StatsService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  dashboard(limit = 10): Observable<Dashboard> {
    const headers = { Authorization: `Bearer ${this.auth.token() ?? ''}` };
    return this.http
      .post<GraphQlResponse>(GRAPHQL_URL, { query: QUERY, variables: { limit } }, { headers })
      .pipe(map(r => {
        if (!r.data?.dashboard) throw new Error(r.errors?.[0]?.message ?? 'Réponse vide');
        return r.data.dashboard;
      }));
  }
}
