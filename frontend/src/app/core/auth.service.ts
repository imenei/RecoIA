import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_URL } from './api.config';

export interface AuthUser {
  id?: number;
  fullName: string;
  email: string;
  role: string;
  interests?: string[];
  track?: string | null;
  language?: string | null;
  duration?: string | null;
}
interface AuthResponse { token: string; user: AuthUser; }

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
  interests: string[];
  track: string | null;
  language: string | null;
  duration: string | null;
}

const API = 'http://localhost:8081/api/auth';
const KEY = 'recoia.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private session = signal<AuthResponse | null>(this.restore());

  readonly user = computed(() => this.session()?.user ?? null);
  readonly token = computed(() => this.session()?.token ?? null);
  readonly isLoggedIn = computed(() => !!this.session());
  readonly isAdmin = computed(() => this.user()?.role === 'ADMIN');
  readonly displayName = computed(() => this.user()?.fullName ?? '');
  readonly firstName = computed(() => this.displayName().split(' ')[0]);

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API}/login`, { email, password }).pipe(tap(r => this.save(r)));
  }

  register(payload: RegisterPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API}/register`, payload).pipe(tap(r => this.save(r)));
  }

  /** Met à jour le profil (PUT /api/auth/me) et garde le même jeton avec l'utilisateur à jour. */
  updateMe(payload: Partial<RegisterPayload>): Observable<AuthUser> {
    return this.http.put<AuthUser>(`${API_URL}/auth/me`, payload).pipe(
      tap(u => { const s = this.session(); if (s) this.save({ token: s.token, user: u }); }),
    );
  }

  /** Efface la session. Le jeton est sans etat cote serveur : l'oublier suffit. */
  logout(): void {
    this.session.set(null);
    try { localStorage.removeItem(KEY); } catch { /* ignore */ }
    this.router.navigate(['/connexion']);
  }

  /** Message lisible pour l'utilisateur a partir d'une erreur HTTP. */
  message(err: unknown): string {
    const e = err as HttpErrorResponse;
    if (e?.status === 0) return 'Serveur injoignable. Vérifiez que auth-service est lancé.';
    if (e?.status === 400) return 'Certains champs sont invalides. Vérifiez le formulaire.';
    if (e?.status === 401 || e?.status === 403) return 'Email ou mot de passe incorrect.';
    if (e?.status === 409) return 'Cet email est déjà utilisé.';
    return 'Une erreur est survenue. Réessayez.';
  }

  private save(r: AuthResponse) {
    this.session.set(r);
    try { localStorage.setItem(KEY, JSON.stringify(r)); } catch { /* ignore */ }
  }

  private restore(): AuthResponse | null {
    try { return JSON.parse(localStorage.getItem(KEY) ?? 'null'); } catch { return null; }
  }
}