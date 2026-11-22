import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon/icon';

@Component({
  selector: 'app-forgot-page',
  imports: [RouterLink, Icon],
  styleUrl: './auth.scss',
  template: `
    <div class="wrap">
      <div class="panel single">
        <form class="form center" (submit)="submit($event, email.value)" novalidate>
          <a class="back" routerLink="/connexion"><app-icon name="back" [size]="18" /> Retour</a>
          <span class="circle"><app-icon name="mail" [size]="40" /></span>
          <h2>Mot de passe oublié ?</h2>
          <p class="sub">Entrez votre adresse email et nous vous enverrons un lien pour le réinitialiser.</p>
          <label class="field" style="width: 100%"><app-icon name="mail" [size]="20" />
            <input #email type="email" placeholder="Adresse email" autocomplete="email" />
          </label>
          @if (error()) { <p class="error" role="alert">{{ error() }}</p> }
          <button class="btn" style="width: 100%" type="submit">Envoyer le lien <app-icon name="arrow" [size]="18" /></button>
          <a class="link" routerLink="/connexion">Retour à la connexion</a>
        </form>
      </div>
    </div>
  `,
})
export class ForgotPage {
  private router = inject(Router);
  error = signal('');

  submit(e: Event, email: string) {
    e.preventDefault();
    if (!email.includes('@')) { this.error.set('Entrez une adresse email valide.'); return; }
    this.router.navigate(['/lien-envoye']);   // TODO: POST /api/auth/forgot-password
  }
}
