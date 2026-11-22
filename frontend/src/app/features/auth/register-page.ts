import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon/icon';
import { Logo } from '../../shared/logo/logo';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register-page',
  imports: [RouterLink, Icon, Logo],
  styleUrl: './auth.scss',
  template: `
    <div class="wrap">
      <div class="panel">
        <form class="form" (submit)="submit($event, name.value, email.value, pwd.value, track.value, lang.value, dur.value)" novalidate>
          <div class="brand left"><app-logo [size]="24" /></div>
          <h2>Créer un compte</h2>
          <p class="sub">Rejoignez RecoIA et obtenez des recommandations personnalisées.</p>

          <label class="field"><app-icon name="user" [size]="20" />
            <input #name type="text" placeholder="Nom complet" autocomplete="name" />
          </label>
          <label class="field"><app-icon name="mail" [size]="20" />
            <input #email type="email" placeholder="Adresse email" autocomplete="email" />
          </label>
          <label class="field"><app-icon name="lock" [size]="20" />
            <input #pwd [type]="show() ? 'text' : 'password'" placeholder="Mot de passe (8 caractères min.)" autocomplete="new-password" />
            <button type="button" class="eye" (click)="show.set(!show())" [attr.aria-label]="show() ? 'Masquer le mot de passe' : 'Afficher le mot de passe'">
              <app-icon [name]="show() ? 'eye' : 'eyeoff'" [size]="20" />
            </button>
          </label>

          <div class="label">Centres d’intérêt <span>(1 minimum)</span></div>
          <div class="chips" role="group" aria-label="Centres d’intérêt">
            @for (d of domains; track d) {
              <button type="button" class="chip" [class.on]="selected().includes(d)"
                      [attr.aria-pressed]="selected().includes(d)" (click)="toggle(d)">{{ d }}</button>
            }
          </div>

          <div class="label">Parcours <span>(facultatif)</span></div>
          <label class="field"><app-icon name="compass" [size]="20" />
            <select #track aria-label="Parcours">
              <option value="">Non précisé</option>
              @for (t of tracks; track t) { <option [value]="t">{{ t }}</option> }
            </select>
          </label>

          <div class="label">Préférences <span>(facultatif)</span></div>
          <div class="two">
            <label class="field">
              <select #lang aria-label="Langue des formations">
                <option value="">Langue</option>
                <option value="FR">Français</option>
                <option value="EN">Anglais</option>
                <option value="ANY">Peu importe</option>
              </select>
            </label>
            <label class="field">
              <select #dur aria-label="Durée des formations">
                <option value="">Durée</option>
                <option value="SHORT">Courte (20 h ou moins)</option>
                <option value="MEDIUM">Moyenne</option>
                <option value="LONG">Longue (40 h ou plus)</option>
                <option value="ANY">Peu importe</option>
              </select>
            </label>
          </div>

          @if (error()) { <p class="error" role="alert">{{ error() }}</p> }

          <button class="btn" type="submit" [disabled]="loading()">
            {{ loading() ? 'Création…' : 'S’inscrire' }} <app-icon name="arrow" [size]="18" />
          </button>
          <p class="foot">Déjà un compte ? <a class="link" routerLink="/connexion">Se connecter</a></p>
        </form>

        <div class="side">
          <div class="people" aria-hidden="true">
            <span class="avatar big" style="left: 40%; top: 14%"><app-icon name="user" [size]="44" /></span>
            <span class="avatar" style="left: 14%; top: 32%"><app-icon name="user" [size]="32" /></span>
            <span class="avatar" style="left: 66%; top: 34%"><app-icon name="user" [size]="32" /></span>
            <span class="avatar" style="left: 38%; top: 50%"><app-icon name="user" [size]="32" /></span>
          </div>
          <p class="tagline">Votre parcours<br />apprentissage,<br />notre priorité.</p>
        </div>
      </div>
    </div>
  `,
})
export class RegisterPage {
  private router = inject(Router);
  private auth = inject(AuthService);

  // memes noms que les categories du catalogue
  readonly domains = ['Informatique', 'Data Science', 'Business', 'Langues', 'Santé', 'Arts et design', 'Ingénierie'];
  readonly tracks = ['Licence', 'Master', 'Doctorat', 'Formation courte', 'Certifications'];

  show = signal(false);
  error = signal('');
  loading = signal(false);
  selected = signal<string[]>([]);

  toggle(d: string) {
    this.selected.update(s => (s.includes(d) ? s.filter(x => x !== d) : [...s, d]));
  }

  submit(e: Event, name: string, email: string, password: string, track: string, language: string, duration: string) {
    e.preventDefault();
    if (!name.trim() || !email.trim()) {
      this.error.set('Veuillez renseigner votre nom et votre email.');
    } else if (password.length < 8) {
      this.error.set('Le mot de passe doit contenir au moins 8 caractères.');
    } else if (this.selected().length === 0) {
      this.error.set('Choisissez au moins un centre d’intérêt.');
    } else {
      this.error.set('');
      this.loading.set(true);
      this.auth.register({
        fullName: name.trim(),
        email: email.trim(),
        password,
        interests: this.selected(),
        track: track || null,
        language: language || null,
        duration: duration || null,
      }).subscribe({
        next: () => this.router.navigate(['/chargement']),
        error: err => { this.loading.set(false); this.error.set(this.auth.message(err)); },
      });
    }
  }
}