import { Routes } from '@angular/router';
import { RecommendationsPage } from './features/recommendations/recommendations-page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'pour-toi' },
  { path: 'pour-toi', component: RecommendationsPage },
  // TODO: catalogue, favoris, profil, admin, dashboard, connexion
];
