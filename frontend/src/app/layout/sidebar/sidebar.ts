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

      <div class="promo">
        <svg viewBox="0 0 160 130" width="160" height="130" aria-hidden="true">
          <defs>
            <radialGradient id="sg" cx="50%" cy="45%" r="50%">
              <stop offset="0%" stop-color="#fff" stop-opacity="0.95"/>
              <stop offset="100%" stop-color="#19CBD8" stop-opacity="0"/>
            </radialGradient>
          </defs>
          <circle cx="80" cy="55" r="52" fill="url(#sg)"/>
          <g stroke="#19CBD8" stroke-width="1.2" fill="none" opacity="0.9">
            <polygon points="80,18 108,36 108,70 80,88 52,70 52,36"/>
            <line x1="80" y1="18" x2="80" y2="88"/><line x1="52" y1="36" x2="108" y2="70"/><line x1="108" y1="36" x2="52" y2="70"/>
          </g>
          <g fill="#19CBD8"><circle cx="80" cy="18" r="3"/><circle cx="108" cy="36" r="3"/><circle cx="108" cy="70" r="3"/>
            <circle cx="80" cy="88" r="3"/><circle cx="52" cy="70" r="3"/><circle cx="52" cy="36" r="3"/><circle cx="80" cy="53" r="3"/></g>
          <path d="M14 104 Q47 92 80 106 Q113 92 146 104 L146 114 Q113 102 80 116 Q47 102 14 114 Z" fill="#0B2C45"/>
        </svg>
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