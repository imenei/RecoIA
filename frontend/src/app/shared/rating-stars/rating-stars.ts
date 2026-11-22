import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-rating-stars',
  template: `
    @if (compact()) {
      <span class="compact"><span class="s">★</span> <b>{{ value() }}</b> <span class="lbl">({{ label() }})</span></span>
    } @else {
      <span class="stars" [attr.aria-label]="value() + ' sur 5'">{{ stars() }}</span>
    }
  `,
  styles: `
    .stars { color: var(--star); letter-spacing: 2px; }
    .compact { font-size: 13px; color: var(--ink); }
    .s { color: var(--star); }
    .lbl { color: var(--muted); }
  `,
})
export class RatingStars {
  value = input.required<number>();
  label = input('');
  compact = input(false);
  stars = computed(() => {
    const n = Math.round(this.value());
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  });
}
