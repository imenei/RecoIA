import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../sidebar/sidebar';
import { Topbar } from '../topbar/topbar';

/** Mise en page de l'application connectee : menu lateral + barre du haut. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, Sidebar, Topbar],
  template: `
    <div class="shell">
      <app-sidebar class="side" />
      <div class="main">
        <app-topbar />
        <router-outlet />
      </div>
    </div>
  `,
  styles: `
    .shell { display: grid; grid-template-columns: 280px minmax(0, 1fr); min-height: 100vh; }
    .side { display: block; position: sticky; top: 0; height: 100vh; }
    .main { padding: 28px 32px 40px; min-width: 0; }
    @media (max-width: 900px) {
      .shell { grid-template-columns: 1fr; }
      .side { display: none; }
      .main { padding: 20px 16px 32px; }
    }
  `,
})
export class AppShell {}
