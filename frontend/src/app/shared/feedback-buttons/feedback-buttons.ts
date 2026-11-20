import { Component, output } from '@angular/core';
import { Verdict } from '../../core/models';

@Component({
  selector: 'app-feedback-buttons',
  template: `
    <button class="fb ok" (click)="verdict.emit('LIKE')">👍 Me convient</button>
    <button class="fb no" (click)="verdict.emit('DISLIKE')">👎 Pas pour moi</button>
  `,
  styles: `
    :host { display: flex; gap: 8px; }
    .fb { border: 0; border-radius: 999px; padding: 8px 14px; font: 600 13px 'Inter', sans-serif;
      cursor: pointer; color: #fff; }
    .ok { background: var(--primary-dark); }
    .no { background: var(--danger); }
  `,
})
export class FeedbackButtons {
  verdict = output<Verdict>();
}
