import { Component, input, output } from '@angular/core';
import { Recommendation, Verdict } from '../../core/models';
import { MatchBadge } from '../match-badge/match-badge';
import { RatingStars } from '../rating-stars/rating-stars';
import { FeedbackButtons } from '../feedback-buttons/feedback-buttons';

@Component({
  selector: 'app-course-card',
  imports: [MatchBadge, RatingStars, FeedbackButtons],
  template: `
    <article class="card">
      <div class="img" [style.background-image]="'url(' + reco().course.imageUrl + ')'">
        <app-match-badge class="match" [score]="reco().score" />
      </div>
      <div class="body">
        <h3>{{ reco().course.title }}</h3>
        <div class="tags">
          <span class="tag">{{ reco().course.category }}</span>
          <span class="tag">{{ reco().course.level }}</span>
        </div>
        @if (reco().rating) { <app-rating-stars [value]="reco().rating!" /> }
        <p class="why">✨ {{ reco().explanation }}</p>
        <app-feedback-buttons (verdict)="feedback.emit($event)" />
      </div>
    </article>
  `,
  styles: `
    article { overflow: hidden; }
    .img { height: 150px; background-size: cover; background-position: center;
      background-color: var(--primary-light); position: relative; }
    .match { position: absolute; top: 12px; right: 12px; }
    .body { padding: 18px; display: grid; gap: 10px; }
    h3 { font-size: 18px; }
    .tags { display: flex; gap: 6px; flex-wrap: wrap; }
    .why { margin: 0; font-size: 12px; color: var(--muted); }
  `,
})
export class CourseCard {
  reco = input.required<Recommendation>();
  feedback = output<Verdict>();
}
