import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon/icon';

@Component({
  selector: 'app-link-sent-page',
  imports: [RouterLink, Icon],
  styleUrl: './auth.scss',
  template: `
    <div class="wrap">
      <div class="panel single">
        <div class="form center">
          <span class="circle ok"><app-icon name="check" [size]="44" /></span>
          <h2>Lien envoyé !</h2>
          <p class="sub">Un lien de réinitialisation a été envoyé<br />à votre adresse email.</p>
          <a class="btn small" routerLink="/connexion">Retour à la connexion</a>
          <svg width="150" height="96" viewBox="0 0 150 96" aria-hidden="true">
            <rect x="20" y="20" width="110" height="68" rx="10" fill="#19CBD8"/>
            <path d="M24 26 L75 64 L126 26" fill="none" stroke="#fff" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="118" cy="78" r="14" fill="#0B7F8C"/>
            <path d="M111 78 l5 5 9-10" fill="none" stroke="#fff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>
    </div>
  `,
})
export class LinkSentPage {}
