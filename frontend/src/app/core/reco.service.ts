import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL } from './api.config';
import { Recommendation } from './models';

const CATALOGUE = 'http://localhost:8082';

/** Forme renvoyée par reco-service (GET /api/reco/students/{id}). */
interface RecoItem {
  courseId: number;
  title: string;
  imageUrl: string | null;
  category: string;
  score: number;
  explanation: string;
  level?: string | null;
  hours?: number | null;
  rating?: number | null;
  reviews?: number | null;
}

@Injectable({ providedIn: 'root' })
export class RecoService {
  private http = inject(HttpClient);

  forStudent(studentId: number, limit = 5): Observable<Recommendation[]> {
    return this.http
      .get<RecoItem[]>(`${API_URL}/reco/students/${studentId}`, { params: { limit } })
      .pipe(map(items => items.map(i => ({
        score: i.score,
        explanation: i.explanation,
        rating: i.rating ? Math.round(i.rating * 10) / 10 : undefined,
        course: {
          id: i.courseId,
          title: i.title,
          category: i.category,
          level: i.level ?? '',
          durationHours: i.hours ?? undefined,
          reviews: i.reviews != null
            ? (i.reviews >= 1000 ? (i.reviews / 1000).toFixed(1) + 'k' : String(i.reviews)) + ' avis'
            : undefined,
          imageUrl: i.imageUrl
            ? (i.imageUrl.startsWith('http') ? i.imageUrl : CATALOGUE + i.imageUrl)
            : `https://picsum.photos/seed/course${i.courseId}/400/250`,
        },
      }))));
  }
}