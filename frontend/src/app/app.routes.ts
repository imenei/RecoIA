import { Routes } from '@angular/router';
import { AppShell } from './layout/app-shell/app-shell';
import { LandingPage } from './features/landing/landing-page';
import { LoginPage } from './features/auth/login-page';
import { RegisterPage } from './features/auth/register-page';
import { ForgotPage } from './features/auth/forgot-page';
import { LinkSentPage } from './features/auth/link-sent-page';
import { LoadingPage } from './features/auth/loading-page';
import { DashboardPage } from './features/dashboard/dashboard-page';
import { RecommendationsPage } from './features/recommendations/recommendations-page';
import { ExplorerPage } from './features/explorer/explorer-page';
import { PlaceholderPage } from './features/placeholder/placeholder-page';
import { FavoritesPage } from './features/favorites/favorites-page';
import { ProfilePage } from './features/profile/profile-page';
import { AdminPage } from './features/admin/admin-page';
import { SuiviPage } from './features/admin/suivi-page';
import { FormationPage } from './features/detail/formation-page';
import { authGuard } from './core/auth.guard';
import { adminGuard } from './core/admin.guard';

export const routes: Routes = [
  // Pages publiques (sans menu lateral)
  { path: '', pathMatch: 'full', component: LandingPage },
  { path: 'connexion', component: LoginPage },
  { path: 'inscription', component: RegisterPage },
  { path: 'mot-de-passe-oublie', component: ForgotPage },
  { path: 'lien-envoye', component: LinkSentPage },
  { path: 'chargement', component: LoadingPage },

  // Application connectee (avec menu lateral) : reservee aux utilisateurs connectes
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardPage },
      { path: 'recommandations', component: RecommendationsPage },
      { path: 'explorer', component: ExplorerPage },
      { path: 'favoris', component: FavoritesPage },
      { path: 'formation/:id', component: FormationPage },
      { path: 'profil', component: ProfilePage },
      { path: 'statistiques', component: PlaceholderPage, data: { title: 'Statistiques' } },
      // Ecran d'administration du catalogue : reserve au role ADMIN
      { path: 'administration', component: AdminPage, canActivate: [adminGuard] },
      { path: 'suivi', component: SuiviPage, canActivate: [adminGuard] },
    ],
  },
  { path: '**', redirectTo: '' },
];
