import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Icon } from '../../shared/icon/icon';
import { AuthService } from '../../core/auth.service';

interface NavItem {
  route: string;
  label: string;
  icon: string;
  role?: 'ADMIN' | 'STUDENT';   // absent = visible par tous
  divider?: boolean;
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, Icon],
  template: `
    <aside>
      <div class="brand">
        <svg width="52" height="52" viewBox="0 0 52 52" aria-hidden="true">
          <path d="M3 20 L26 8 L49 20 L26 32 Z" fill="#0B2C45"/>
          <path d="M13 26 V36 C13 40 19 43 26 43 C33 43 39 40 39 36 V26 L26 33 Z" fill="#19CBD8"/>
          <path d="M46 22 V34" stroke="#19CBD8" stroke-width="3" stroke-linecap="round"/>
        </svg>
        <div>
          <div class="logo">Reco<b>IA</b></div>
          <div class="slogan">Find your next path</div>
        </div>
      </div>

      <nav>
        @for (item of items; track item.route) {
          @if (visible(item)) {
            <a [routerLink]="item.route" routerLinkActive="on">
              <app-icon [name]="item.icon" [size]="22" />
              <span>{{ item.label }}</span>
            </a>
            @if (item.divider) { <hr /> }
          }
        }
        <button type="button" (click)="auth.logout()">
          <app-icon name="logout" [size]="22" />
          <span>Déconnexion</span>
        </button>
      </nav>
      <svg class="side-art" viewBox="-84 -66 168 122" width="160" aria-hidden="true" style="display:block;margin:22px auto 6px">
        <line x1="0" y1="-30" x2="0" y2="-42" stroke="#0B2C45" stroke-width="3" stroke-linecap="round"/>
        <circle cx="0" cy="-47" r="5" fill="#19CBD8"/>
        <rect x="-40" y="-14" width="8" height="22" rx="3" fill="#19CBD8"/>
        <rect x="32" y="-14" width="8" height="22" rx="3" fill="#19CBD8"/>
        <rect x="-34" y="-30" width="68" height="56" rx="16" fill="#0B2C45"/>
        <circle cx="-14" cy="-6" r="6" fill="#19CBD8"/>
        <circle cx="14" cy="-6" r="6" fill="#19CBD8"/>
        <path d="M-10 10 Q0 18 10 10" fill="none" stroke="#19CBD8" stroke-width="3" stroke-linecap="round"/>
        <rect x="-24" y="30" width="48" height="22" rx="10" fill="#19CBD8"/>
        <rect x="38" y="-62" width="44" height="28" rx="8" fill="#FFFFFF" stroke="#19CBD8" stroke-width="2"/>
        <path d="M60 -55 l2 6 6 2 -6 2 -2 6 -2 -6 -6 -2 6 -2z" fill="#19CBD8"/>
      </svg>

      <div class="promo">
        <p>Apprendre aujourd’hui,<br />réussir demain</p>
      </div>
    </aside>
  `,
  styles: `
    aside { height: 100%; display: flex; flex-direction: column; padding: 28px 14px 24px;
      background: rgba(255,255,255,.55); border-right: 1px solid rgba(255,255,255,.8); }
    .brand { display: flex; align-items: center; gap: 10px; padding-left: 8px; margin-bottom: 36px; }
    .logo { font: 700 30px 'Outfit', sans-serif; color: var(--ink); line-height: 1; }
    .logo b { color: var(--primary); }
    .slogan { font-size: 11px; color: var(--muted); margin-top: 4px; }
    nav { display: grid; gap: 8px; }
    nav a, nav button { display: flex; align-items: center; gap: 14px; padding: 14px 16px; border-radius: 18px;
      color: var(--ink); text-decoration: none; font-size: 15px; font-weight: 500; }
    nav button { border: 0; background: none; width: 100%; text-align: left; cursor: pointer; font-family: inherit; }
    nav a:hover, nav button:hover { background: rgba(255,255,255,.8); }
    nav a.on { background: #7FE6EC; font-weight: 700; box-shadow: inset 4px 0 0 #12B5C4; }
    nav a:focus-visible, nav button:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
    hr { border: 0; border-top: 1px solid rgba(11,44,69,.12); margin: 14px 12px; }
    .promo { margin-top: auto; text-align: center; }
    .promo p { margin: 6px 0 0; font-weight: 600; font-size: 14px; color: var(--ink); }
  `,
})
export class Sidebar {
  auth = inject(AuthService);

  items: NavItem[] = [
    { route: '/dashboard', label: 'Tableau de bord', icon: 'home', role: 'STUDENT' },
    { route: '/explorer', label: 'Explorer mes formations', icon: 'compass', role: 'STUDENT' },
    { route: '/recommandations', label: 'Mes recommandations', icon: 'sparkle', role: 'STUDENT' },
    { route: '/favoris', label: 'Mes favoris', icon: 'heart', role: 'STUDENT' },
    { route: '/profil', label: 'Mon profil', icon: 'user', role: 'STUDENT' },
    { route: '/statistiques', label: 'Statistiques', icon: 'chart', role: 'STUDENT', divider: true },
    { route: '/administration', label: 'Administration', icon: 'gear', role: 'ADMIN' },
    { route: '/suivi', label: "Suivi de l'activité", icon: 'chart', role: 'ADMIN', divider: true },
  ];

  visible(item: NavItem): boolean {
    if (!item.role) return true;
    return item.role === 'ADMIN' ? this.auth.isAdmin() : !this.auth.isAdmin();
  }
}