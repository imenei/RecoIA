import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './api.config';

/** Réponse de GET /api/activity/me. */
export interface ActivityMe {
  favorites: number[];
  ratings: Record<string, number>;
  feedback: Record<string, number>;   // 1 = me convient, -1 = pas pour moi
}

@Injectable({ providedIn: 'root' })
export class ActivityService {
  private http = inject(HttpClient);
  private base = `${API_URL}/activity`;

  /** accepted = true : "Me convient" ; false : "Pas pour moi". */
  feedback(courseId: number, accepted: boolean): Observable<void> {
    return this.http.put<void>(`${this.base}/feedback/${courseId}`, { accepted });
  }

  me(): Observable<ActivityMe> {
    return this.http.get<ActivityMe>(`${this.base}/me`);
  }

  addFavorite(courseId: number): Observable<void> {
    return this.http.put<void>(`${this.base}/favorites/${courseId}`, {});
  }

  removeFavorite(courseId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/favorites/${courseId}`);
  }

  /** Note de 1 à 5 ; une nouvelle note remplace l'ancienne. */
  rate(courseId: number, value: number): Observable<void> {
    return this.http.put<void>(`${this.base}/ratings/${courseId}`, { value });
  }

  view(courseId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/views/${courseId}`, {});
  }
}