import { Injectable, effect, inject, signal, untracked } from '@angular/core';
import { ActivityService } from './activity.service';
import { AuthService } from './auth.service';

/** Favoris de l'étudiant connecté, chargés une fois depuis activity-service et partagés par toutes les pages. */
@Injectable({ providedIn: 'root' })
export class ActivityStore {
  private activity = inject(ActivityService);
  private auth = inject(AuthService);

  readonly favorites = signal<ReadonlySet<number>>(new Set());
  readonly loaded = signal(false);

  constructor() {
    effect(() => {
      if (this.auth.isLoggedIn()) {
        untracked(() => this.load());
      } else {
        this.favorites.set(new Set());
        this.loaded.set(false);
      }
    });
  }

  isFav(id: number): boolean {
    return this.favorites().has(id);
  }

  load(): void {
    this.activity.me().subscribe({
      next: m => { this.favorites.set(new Set(m.favorites ?? [])); this.loaded.set(true); },
      error: () => this.loaded.set(true),
    });
  }

  /** Mise à jour immédiate de l'écran, annulée si le serveur refuse. */
  toggle(id: number): void {
    const was = this.isFav(id);
    this.set(id, !was);
    const call = was ? this.activity.removeFavorite(id) : this.activity.addFavorite(id);
    call.subscribe({ error: () => this.set(id, was) });
  }

  private set(id: number, on: boolean): void {
    const next = new Set(this.favorites());
    if (on) next.add(id); else next.delete(id);
    this.favorites.set(next);
  }
}