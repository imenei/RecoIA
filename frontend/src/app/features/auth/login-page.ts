import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon/icon';
import { Logo } from '../../shared/logo/logo';
import { NetworkArt } from '../../shared/network-art/network-art';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login-page',
  imports: [RouterLink, Icon, Logo, NetworkArt],
  styleUrl: './auth.scss',
  template: `
    <div class="wrap">
      <div class="panel">
        <form class="form" (submit)="submit($event, email.value, pwd.value)" novalidate>
          <div class="brand left"><app-logo [size]="26" /></div>
          <h1>Bon retour !</h1>
          <p class="sub">Connectez-vous pour accéder<br />à vos recommandations.</p>

          <label class="field"><app-icon name="mail" [size]="20" />
            <input #email type="email" placeholder="Adresse email" autocomplete="email" />
          </label>
          <label class="field"><app-icon name="lock" [size]="20" />
            <input #pwd [type]="show() ? 'text' : 'password'" placeholder="Mot de passe" autocomplete="current-password" />
            <button type="button" class="eye" (click)="show.set(!show())" [attr.aria-label]="show() ? 'Masquer le mot de passe' : 'Afficher le mot de passe'">
              <app-icon [name]="show() ? 'eye' : 'eyeoff'" [size]="20" />
            </button>
          </label>

          @if (error()) { <p class="error" role="alert">{{ error() }}</p> }

          <div class="row">
            <label><input type="checkbox" /> Se souvenir de moi</label>
            <a class="link" routerLink="/mot-de-passe-oublie">Mot de passe oublié ?</a>
          </div>

          <button class="btn" type="submit" [disabled]="loading()">
            {{ loading() ? 'Connexion…' : 'Se connecter' }} <app-icon name="arrow" [size]="18" />
          </button>
          <div class="divider">ou</div>
          <button class="social" type="button"><span class="g">G</span> Continuer avec Google</button>
          <button class="social" type="button"><svg width="20" height="20" viewBox="0 0 24 24" fill="#0B2C45" aria-hidden="true"><path d="M16.37 12.5c-.02-2.2 1.8-3.26 1.88-3.3-1.02-1.5-2.62-1.7-3.19-1.73-1.36-.14-2.65.8-3.34.8-.69 0-1.75-.78-2.88-.76-1.48.02-2.85.86-3.6 2.18-1.54 2.67-.4 6.62 1.1 8.78.73 1.06 1.6 2.25 2.75 2.2 1.1-.04 1.52-.71 2.85-.71 1.33 0 1.7.71 2.87.69 1.19-.02 1.94-1.08 2.66-2.14.84-1.23 1.19-2.42 1.2-2.48-.03-.01-2.3-.88-2.3-3.53zM14.2 6.02c.6-.73 1.01-1.74.9-2.75-.87.04-1.92.58-2.54 1.3-.56.65-1.05 1.68-.92 2.67.97.08 1.96-.49 2.56-1.22z"/></svg> Continuer avec Apple</button>
          <p class="foot">Vous n’avez pas de compte ? <a class="link" routerLink="/inscription">S’inscrire</a></p>
        </form>

        <div class="side">
          <div class="art"><app-network-art /></div>
          <p class="tagline">Apprenez,<br />Progressez,<br />Réussissez.</p>
        </div>
      </div>
    </div>
  `,
})
export class LoginPage {
  private router = inject(Router);
  private auth = inject(AuthService);
  show = signal(false);
  error = signal('');
  loading = signal(false);

  submit(e: Event, email: string, password: string) {
    e.preventDefault();
    if (!email.trim() || !password) {
      this.error.set('Veuillez renseigner votre email et votre mot de passe.');
      return;
    }
    this.error.set('');
    this.loading.set(true);
    this.auth.login(email.trim(), password).subscribe({
      next: () => this.router.navigate(['/chargement']),
      error: err => {
        this.loading.set(false);
        this.error.set(this.auth.message(err));
      },
    });
  }
}