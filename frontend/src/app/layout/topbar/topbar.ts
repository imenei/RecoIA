import { Component } from '@angular/core';
import { Icon } from '../../shared/icon/icon';

@Component({
  selector: 'app-topbar',
  imports: [Icon],
  template: `
    <header>
      <label class="search">
        <app-icon name="search" [size]="22" />
        <input type="search" placeholder="Rechercher une formation, une compétence..." />
      </label>
      <div class="right">
        <button class="bell" aria-label="Notifications">
          <app-icon name="bell" [size]="24" />
          <span class="dot"></span>
        </button>
        <button class="user">
          <span class="avatar">I</span>
          <span class="name">Imene</span>
          <app-icon name="chevdown" [size]="18" />
        </button>
      </div>
    </header>
  `,
  styles: `
    header { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
    .search { display: flex; align-items: center; gap: 12px; width: min(590px, 100%); background: #fff;
      border-radius: 999px; padding: 0 22px; height: 54px; color: var(--muted);
      box-shadow: 0 4px 16px rgba(15,140,160,.08); }
    input { border: 0; outline: 0; background: transparent; flex: 1; font: 15px 'Inter', sans-serif; color: var(--ink); }
    .right { display: flex; align-items: center; gap: 14px; }
    .bell { position: relative; width: 54px; height: 54px; border-radius: 50%; border: 0; background: #fff;
      color: var(--ink); cursor: pointer; box-shadow: 0 4px 16px rgba(15,140,160,.08); }
    .dot { position: absolute; top: 14px; right: 15px; width: 10px; height: 10px; background: #ef4444;
      border-radius: 50%; border: 2px solid #fff; }
    .user { display: flex; align-items: center; gap: 10px; border: 0; background: #fff; border-radius: 999px;
      padding: 0 16px 0 6px; height: 54px; cursor: pointer; color: var(--ink);
      box-shadow: 0 4px 16px rgba(15,140,160,.08); font: 600 17px 'Inter', sans-serif; }
    .avatar { width: 42px; height: 42px; border-radius: 50%; background: var(--primary); color: var(--ink);
      display: grid; place-items: center; font: 700 18px 'Outfit', sans-serif; }
    .bell:focus-visible, .user:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
    @media (max-width: 700px) { .name { display: none; } }
  `,
})
export class Topbar {}
