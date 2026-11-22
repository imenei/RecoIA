import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../core/auth.service';
import { Icon } from '../../shared/icon/icon';

@Component({
  selector: 'app-profile-page',
  imports: [Icon],
  template: `
    <h1>Mon profil</h1>
    <p class="sub">Ces informations servent à personnaliser vos recommandations.</p>

    <section class="card">
      <div class="avatar" aria-hidden="true">{{ initial() }}</div>

      <label class="field">
        <span>Nom complet</span>
        <input #n type="text" maxlength="120" [value]="name()" (input)="name.set(n.value)" />
      </label>

      <label class="field">
        <span>Email</span>
        <input type="email" [value]="email" disabled />
      </label>

      <div class="field">
        <span>Centres d'intérêt <em>({{ interests().length }}/{{ maxInterests }})</em></span>
        <div class="chips" role="group" aria-label="Centres d'intérêt">
          @for (d of domains; track d) {
            <button type="button" class="chip" [class.on]="interests().includes(d)"
                    [attr.aria-pressed]="interests().includes(d)" (click)="toggle(d)">
              @if (interests().includes(d)) { <app-icon name="check" [size]="14" /> }
              {{ d }}
            </button>
          }
        </div>
      </div>

      <div class="row">
        <label class="field">
          <span>Parcours</span>
          <select #t (change)="track.set(t.value)">
            <option value="" [selected]="!track()">Non précisé</option>
            @for (x of tracks; track x) { <option [value]="x" [selected]="track() === x">{{ x }}</option> }
          </select>
        </label>

        <label class="field">
          <span>Langue préférée</span>
          <select #l (change)="language.set(l.value)">
            @for (x of languages; track x.v) { <option [value]="x.v" [selected]="language() === x.v">{{ x.l }}</option> }
          </select>
        </label>

        <label class="field">
          <span>Durée préférée</span>
          <select #d (change)="duration.set(d.value)">
            @for (x of durations; track x.v) { <option [value]="x.v" [selected]="duration() === x.v">{{ x.l }}</option> }
          </select>
        </label>
      </div>

      <div class="actions">
        <button type="button" class="save" [disabled]="saving()" (click)="save()">
          {{ saving() ? 'Enregistrement…' : 'Enregistrer les modifications' }}
        </button>
        @if (message(); as m) {
          <p class="msg" [class.ok]="m.ok" role="status" aria-live="polite">{{ m.text }}</p>
        }
      </div>
    </section>
  `,
  styles: `
    h1 { margin: 24px 0 4px; }
    .sub { margin: 0 0 20px; color: var(--muted); }
    .card { background: #fff; border-radius: 18px; padding: 28px; display: grid; gap: 20px; max-width: 760px; }
    .avatar { width: 64px; height: 64px; border-radius: 50%; display: grid; place-items: center;
      background: #19D3DF; color: var(--ink); font: 700 26px 'Outfit', sans-serif; }
    .field { display: grid; gap: 8px; font-size: 14px; }
    .field > span { font-weight: 600; color: var(--ink); }
    .field em { font-style: normal; font-weight: 400; color: var(--muted); }
    input, select { padding: 11px 14px; border-radius: 12px; border: 1.5px solid #d5dde3; font: 400 15px 'Inter', sans-serif;
      background: #fff; color: var(--ink); }
    input:disabled { background: #f3f6f8; color: var(--muted); }
    input:focus-visible, select:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 1px; }
    .row { display: grid; gap: 16px; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); }
    .chips { display: flex; flex-wrap: wrap; gap: 8px; }
    .chip { display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: 999px;
      border: 1.5px solid #d5dde3; background: #fff; color: var(--ink); font: 600 13.5px 'Inter', sans-serif; cursor: pointer; }
    .chip:hover { border-color: var(--primary-dark); }
    .chip.on { background: var(--primary-dark); border-color: var(--primary-dark); color: #fff; }
    .chip:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
    .actions { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
    .save { border: 0; border-radius: 999px; padding: 12px 24px; background: #19D3DF; color: var(--ink);
      font: 700 14px 'Inter', sans-serif; cursor: pointer; }
    .save:disabled { opacity: .6; cursor: default; }
    .save:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
    .msg { margin: 0; font-size: 14px; color: #c0392b; }
    .msg.ok { color: #1b7f5c; }
  `,
})
export class ProfilePage {
  private auth = inject(AuthService);
  private u = this.auth.user();

  readonly maxInterests = 8;
  readonly domains = ['Informatique', 'Data Science', 'Business', 'Langues', 'Santé', 'Arts et design', 'Ingénierie'];
  readonly tracks = ['Licence', 'Master', 'Doctorat', 'Formation courte', 'Certifications'];
  readonly languages = [
    { v: 'FR', l: 'Français' }, { v: 'EN', l: 'Anglais' }, { v: 'ANY', l: 'Peu importe' },
  ];
  readonly durations = [
    { v: 'SHORT', l: 'Courte' }, { v: 'MEDIUM', l: 'Moyenne' }, { v: 'LONG', l: 'Longue' }, { v: 'ANY', l: 'Peu importe' },
  ];

  email = this.u?.email ?? '';
  name = signal(this.u?.fullName ?? '');
  interests = signal<string[]>(this.u?.interests ?? []);
  track = signal(this.u?.track ?? '');
  language = signal(this.u?.language ?? 'ANY');
  duration = signal(this.u?.duration ?? 'ANY');

  saving = signal(false);
  message = signal<{ ok: boolean; text: string } | null>(null);

  initial(): string { return (this.name().trim()[0] ?? '?').toUpperCase(); }

  toggle(d: string): void {
    const cur = this.interests();
    if (cur.includes(d)) {
      this.interests.set(cur.filter(x => x !== d));
    } else if (cur.length >= this.maxInterests) {
      this.message.set({ ok: false, text: `Vous pouvez choisir ${this.maxInterests} centres d'intérêt au maximum.` });
    } else {
      this.interests.set([...cur, d]);
      this.message.set(null);
    }
  }

  save(): void {
    if (!this.name().trim()) {
      this.message.set({ ok: false, text: 'Le nom ne peut pas être vide.' });
      return;
    }
    this.saving.set(true);
    this.message.set(null);
    this.auth.updateMe({
      fullName: this.name().trim(),
      interests: this.interests(),
      track: this.track() || null,
      language: this.language(),
      duration: this.duration(),
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.message.set({ ok: true, text: 'Profil mis à jour. Vos recommandations en tiendront compte.' });
      },
      error: (e: HttpErrorResponse) => {
        this.saving.set(false);
        const text =
          e.status === 0 ? 'Serveur injoignable. Vérifiez que la gateway et auth-service sont lancés.'
          : e.status === 400 ? 'Certains champs sont invalides. Vérifiez le formulaire.'
          : e.status === 401 || e.status === 403 ? 'Session expirée : reconnectez-vous.'
          : 'Une erreur est survenue. Réessayez.';
        this.message.set({ ok: false, text });
      },
    });
  }
}