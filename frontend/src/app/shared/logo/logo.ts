import { Component, input } from '@angular/core';

@Component({
  selector: 'app-logo',
  template: `
    <svg [attr.width]="size() * 1.5" [attr.height]="size() * 1.5" viewBox="0 0 52 52" aria-hidden="true">
      <path d="M3 20 L26 8 L49 20 L26 32 Z" fill="#0B2C45"/>
      <path d="M13 26 V36 C13 40 19 43 26 43 C33 43 39 40 39 36 V26 L26 33 Z" fill="#19CBD8"/>
      <path d="M46 22 V34" stroke="#19CBD8" stroke-width="3" stroke-linecap="round"/>
    </svg>
    <span class="t" [style.font-size.px]="size()">Reco<b>IA</b></span>
  `,
  styles: `
    :host { display: inline-flex; align-items: center; gap: 8px; }
    .t { font-family: 'Outfit', sans-serif; font-weight: 700; color: var(--ink); line-height: 1; }
    b { color: var(--primary); }
  `,
})
export class Logo {
  size = input(28);
}
