import { Component, OnDestroy, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Logo } from '../../shared/logo/logo';

/** Ecran de chargement apres connexion : redirige vers le tableau de bord. */
@Component({
  selector: 'app-loading-page',
  imports: [Logo],
  styleUrl: './auth.scss',
  template: `
    <div class="wrap">
      <div class="form center" role="status" style="gap: 20px">
        <app-logo [size]="44" />
        <p class="sub" style="color: var(--ink); font-weight: 500">Préparation de votre espace…</p>
        <span class="spinner"></span>
        <p class="sub">Un instant, s’il vous plaît</p>
      </div>
    </div>
  `,
})
export class LoadingPage implements OnDestroy {
  private router = inject(Router);
  private timer = setTimeout(() => this.router.navigate(['/dashboard']), 2200);
  ngOnDestroy() { clearTimeout(this.timer); }
}
