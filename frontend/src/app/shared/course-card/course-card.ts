import { Component, computed, inject, input, output } from '@angular/core';
import { Recommendation, Verdict } from '../../core/models';
import { ActivityService } from '../../core/activity.service';
import { ActivityStore } from '../../core/activity.store';
import { MatchBadge } from '../match-badge/match-badge';
import { RatingStars } from '../rating-stars/rating-stars';
import { FeedbackButtons } from '../feedback-buttons/feedback-buttons';
import { Icon } from '../icon/icon';

@Component({
  selector: 'app-course-card',
  imports: [MatchBadge, RatingStars, FeedbackButtons, Icon],
  template: `
    <article class="card">
      <div class="img" [style.background-image]="'url(' + reco().course.imageUrl + ')'">
        <button class="heart" [class.on]="fav()" (click)="toggleFav()" aria-label="Ajouter aux favoris">
          <app-icon name="heart" [size]="20" [filled]="fav()" />
        </button>
        <app-match-badge class="match" [score]="reco().score" />
      </div>
      <div class="body">
        <h3>{{ reco().course.title }}</h3>
        <div class="tags">
          <span class="tag">{{ reco().course.category }}</span>
          @if (reco().course.level) { <span class="tag">{{ reco().course.level }}</span> }
        </div>
        @if (reco().rating) {
          <app-rating-stars [value]="reco().rating!" [label]="reco().course.reviews ?? ''" [compact]="true" />
        }
        @if (showFeedback()) {
          <p class="why"><app-icon name="sparkle" [size]="14" /> {{ reco().explanation }}</p>
          <app-feedback-buttons (verdict)="feedback.emit($event)" />
        }
        <div class="foot">
          @if (reco().course.durationHours) { <span class="dur"><app-icon name="clock" [size]="16" /> {{ reco().course.durationHours }}h</span> } @else { <span></span> }
          <button class="more" (click)="view()">Voir détails <app-icon name="arrow" [size]="15" /></button>
        </div>
      </div>
    </article>
  `,
  styles: `
    article { overflow: hidden; border-radius: 18px; }
    .img { height: 128px; background-size: cover; background-position: center;
      background-color: #bfeff4; position: relative; }
    .heart { position: absolute; top: 12px; left: 12px; background: none; border: 0; color: #fff;
      cursor: pointer; padding: 2px; filter: drop-shadow(0 1px 3px rgba(0,0,0,.4)); }
    .heart.on { color: #ff6b81; }
    .match { position: absolute; top: 10px; right: 10px; }
    .body { padding: 14px 16px 16px; display: grid; gap: 10px; }
    h3 { font-size: 17px; line-height: 1.25; min-height: 42px; }
    .tags { display: flex; gap: 6px; flex-wrap: wrap; }
    .why { display: flex; gap: 6px; align-items: flex-start; margin: 0; font-size: 12px; color: var(--muted); }
    .foot { display: flex; align-items: center; justify-content: space-between; margin-top: 4px; }
    .dur { display: inline-flex; align-items: center; gap: 6px; font-size: 14px; color: var(--ink); }
    .more { display: inline-flex; align-items: center; gap: 6px; border: 0; border-radius: 999px;
      padding: 9px 16px; background: #19D3DF; color: var(--ink); font: 600 13px 'Inter', sans-serif; cursor: pointer; }
    .more:focus-visible, .heart:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
  `,
})
export class CourseCard {
  private activity = inject(ActivityService);
  private store = inject(ActivityStore);

  reco = input.required<Recommendation>();
  showFeedback = input(false);
  feedback = output<Verdict>();

  fav = computed(() => this.store.isFav(this.reco().course.id));

  toggleFav(): void { this.store.toggle(this.reco().course.id); }
  view(): void { this.activity.view(this.reco().course.id).subscribe({ error: () => {} }); }
}