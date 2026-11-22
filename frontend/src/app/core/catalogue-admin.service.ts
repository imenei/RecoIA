import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

/** Le catalogue est appele directement (comme auth-service). Le jeton est ajoute par authInterceptor. */
export const CATALOGUE_ORIGIN = 'http://localhost:8082';
const BASE = `${CATALOGUE_ORIGIN}/api`;

export interface AdminCourse {
  id: number;
  title: string;
  description?: string | null;
  category: string;
  level: string;
  skills?: string | null;
  imageUrl?: string | null;
  university?: string | null;
  hours?: number | null;
  rating?: number | null;
}

/** Valeurs du formulaire (tout en texte, converti a l'envoi). */
export interface CourseForm {
  title: string;
  description: string;
  category: string;
  level: string;
  skills: string;
  university: string;
  hours: string;
  imageUrl: string;
}

export interface CoursePage { items: AdminCourse[]; total: number; }

export const emptyForm = (): CourseForm => ({
  title: '', description: '', category: '', level: '', skills: '', university: '', hours: '', imageUrl: '',
});

export const formFromCourse = (c: AdminCourse): CourseForm => ({
  title: c.title ?? '',
  description: c.description ?? '',
  category: c.category ?? '',
  level: c.level ?? '',
  skills: c.skills ?? '',
  university: c.university ?? '',
  hours: c.hours != null ? String(c.hours) : '',
  imageUrl: c.imageUrl ?? '',
});

/** Corps JSON envoye au catalogue : les champs vides deviennent null. */
export function toPayload(f: CourseForm): Record<string, unknown> {
  const text = (v: string) => (v.trim() === '' ? null : v.trim());
  const hours = parseInt(f.hours, 10);
  return {
    title: f.title.trim(),
    description: text(f.description),
    category: f.category.trim(),
    level: f.level.trim(),
    skills: text(f.skills),
    university: text(f.university),
    hours: Number.isFinite(hours) ? hours : null,
    imageUrl: text(f.imageUrl),
  };
}

/** Une image enregistree par le catalogue est donnee en chemin relatif (/api/images/...). */
export function imageSrc(url?: string | null): string {
  if (!url) return '';
  return url.startsWith('/') ? CATALOGUE_ORIGIN + url : url;
}

@Injectable({ providedIn: 'root' })
export class CatalogueAdminService {
  private http = inject(HttpClient);

  list(query: string, page: number, size: number): Observable<CoursePage> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'id,desc');
    if (query.trim()) params = params.set('q', query.trim());
    return this.http.get<any>(`${BASE}/courses`, { params }).pipe(
      map(r => ({
        items: (r?.content ?? []) as AdminCourse[],
        total: r?.page?.totalElements ?? r?.totalElements ?? (r?.content?.length ?? 0),
      })),
    );
  }

  create(f: CourseForm): Observable<AdminCourse> {
    return this.http.post<AdminCourse>(`${BASE}/courses`, toPayload(f));
  }

  update(id: number, f: CourseForm): Observable<AdminCourse> {
    return this.http.put<AdminCourse>(`${BASE}/courses/${id}`, toPayload(f));
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/courses/${id}`);
  }

  uploadImage(id: number, file: File): Observable<AdminCourse> {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<AdminCourse>(`${BASE}/courses/${id}/image`, body);
  }

  importCsv(file: File): Observable<{ imported: number }> {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<{ imported: number }>(`${BASE}/courses/import`, body);
  }
}
