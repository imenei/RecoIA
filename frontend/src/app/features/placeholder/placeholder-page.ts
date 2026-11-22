import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder-page',
  template: `
    <div class="card box">
      <h1>{{ title }}</h1>
      <p>Cette page arrive bientôt.</p>
    </div>
  `,
  styles: `.box { padding: 40px; margin-top: 24px; } p { color: var(--muted); }`,
})
export class PlaceholderPage {
  title: string = inject(ActivatedRoute).snapshot.data['title'] ?? '';
}
