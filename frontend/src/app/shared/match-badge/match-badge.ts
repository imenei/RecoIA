import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-match-badge',
  template: `<span class="badge">✨ {{ percent() }}% match</span>`,
  styles: `.badge { background: var(--primary); color: var(--ink); border-radius: 999px;
    padding: 5px 12px; font-size: 12px; font-weight: 600; }`,
})
export class MatchBadge {
  score = input.required<number>();
  percent = computed(() => Math.round(this.score() * 100));
}
