import { Component, output, signal } from '@angular/core';
import { Verdict } from '../../core/models';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-feedback-buttons',
  imports: [Icon],
  template: `
    <div class="fb" role="group" aria-label="Votre avis sur cette recommandation">
      <button type="button" [class.on]="chosen() === 'LIKE'" [attr.aria-pressed]="chosen() === 'LIKE'"
              (click)="pick('LIKE')">
        <app-icon name="thumbup" [size]="16" /> Me convient
      </button>
      <button type="button" class="neg" [class.on]="chosen() === 'DISLIKE'" [attr.aria-pressed]="chosen() === 'DISLIKE'"
              (click)="pick('DISLIKE')">
        <app-icon name="thumbdown" [size]="16" /> Pas pour moi
      </button>
    </div>
  `,
  styles: `
    .fb { display: flex; gap: 8px; flex-wrap: wrap; }
    button { display: inline-flex; align-items: center; gap: 7px; padding: 7px 13px; border-radius: 999px;
      border: 1.5px solid #d5dde3; background: #fff; color: var(--ink, #0B2C45);
      font: 600 12.5px 'Inter', sans-serif; cursor: pointer; transition: background .15s, border-color .15s, color .15s; }
    button:hover { border-color: var(--primary-dark, #0e8a94); background: #f1fbfc; }
    button.on { background: var(--primary-dark, #0e8a94); border-color: var(--primary-dark, #0e8a94); color: #fff; }
    button.neg:hover { border-color: #c0392b; background: #fdf3f2; color: #c0392b; }
    button.neg.on { background: #c0392b; border-color: #c0392b; color: #fff; }
    button:focus-visible { outline: 3px solid var(--primary-dark, #0e8a94); outline-offset: 2px; }
  `,
})
export class FeedbackButtons {
  verdict = output<Verdict>();
  chosen = signal<Verdict | null>(null);

  pick(v: Verdict) {
    this.chosen.set(v);
    this.verdict.emit(v);
  }
}