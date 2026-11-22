import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../shared/icon/icon';
import { Logo } from '../../shared/logo/logo';

@Component({
  selector: 'app-landing-page',
  imports: [RouterLink, Icon, Logo],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.scss',
})
export class LandingPage {
  bubbles = [
    { icon: 'code', x: 22, y: 8 }, { icon: 'briefcase', x: 50, y: 0 }, { icon: 'chart', x: 84, y: 20 },
    { icon: 'heart', x: 4, y: 34 }, { icon: 'cap', x: 28, y: 58 }, { icon: 'globe', x: 80, y: 64 },
  ];

  features = [
    { icon: 'sparkle', title: 'Recommandations', sub: 'IA' },
    { icon: 'book', title: 'Catalogue complet', sub: '' },
    { icon: 'chart', title: 'Suivi de progression', sub: '' },
    { icon: 'badge', title: 'Accès facile', sub: '' },
  ];

  popular = [
    { title: 'Développement Web avec React', rating: 4.8, reviews: '12k', hours: 40, image: 'https://picsum.photos/seed/rec1/400/250' },
    { title: 'Data Science avec Python', rating: 4.7, reviews: '9.8k', hours: 35, image: 'https://picsum.photos/seed/rec3/400/250' },
    { title: 'Intelligence Artificielle pour Tous', rating: 4.6, reviews: '7.5k', hours: 30, image: 'https://picsum.photos/seed/rec2/400/250' },
    { title: 'Gestion de Projets Agiles', rating: 4.7, reviews: '6.1k', hours: 25, image: 'https://picsum.photos/seed/rec8/400/250' },
  ];
}
