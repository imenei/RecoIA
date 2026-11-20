import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-rating-stars',
  template: `<span class="stars" [attr.aria-label]="value() + ' sur 5'">{{ stars() }}</span>`,
  styles: `.stars { color: var(--star); letter-spacing: 2px; }`,
})
export class RatingStars {
  value = input.required<number>();
  stars = computed(() => {
    const n = Math.round(this.value());
    return '★'.repeat(n) + '☆'.repeat(5 - n);
  });
}
