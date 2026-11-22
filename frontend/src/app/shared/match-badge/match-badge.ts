import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-match-badge',
  template: `<span class="badge">{{ percent() }}% match</span>`,
  styles: `.badge { background: #1CC4D1; color: #fff; border-radius: 999px;
    padding: 6px 14px; font-size: 13px; font-weight: 600; display: inline-block; }`,
})
export class MatchBadge {
  score = input.required<number>();
  percent = computed(() => Math.round(this.score() * 100));
}
