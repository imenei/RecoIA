import { Component } from '@angular/core';

/** Reseau de points decoratif (page de connexion). */
@Component({
  selector: 'app-network-art',
  template: `
    <svg viewBox="0 0 300 380" preserveAspectRatio="xMaxYMid slice" aria-hidden="true">
      <defs>
        <radialGradient id="na" cx="50%" cy="40%" r="60%">
          <stop offset="0%" stop-color="#fff" stop-opacity="0.9"/>
          <stop offset="100%" stop-color="#fff" stop-opacity="0"/>
        </radialGradient>
      </defs>
      <circle cx="170" cy="150" r="150" fill="url(#na)"/>
      <g stroke="#fff" stroke-width="1.5" opacity="0.9">
        @for (e of edges; track $index) {
          <line [attr.x1]="nodes[e[0]][0]" [attr.y1]="nodes[e[0]][1]" [attr.x2]="nodes[e[1]][0]" [attr.y2]="nodes[e[1]][1]" />
        }
      </g>
      <g fill="#fff" stroke="#19CBD8" stroke-width="2">
        @for (n of nodes; track $index) { <circle [attr.cx]="n[0]" [attr.cy]="n[1]" r="5" /> }
      </g>
    </svg>
  `,
  styles: `:host { display: block; } svg { width: 100%; height: 100%; }`,
})
export class NetworkArt {
  nodes: [number, number][] = [[120,40],[190,20],[250,60],[280,120],[260,190],[200,230],[140,210],[100,150],[150,100],
    [210,110],[170,160],[235,150],[90,90],[60,170],[150,260],[230,280]];
  edges: [number, number][] = [[0,1],[1,2],[2,3],[3,4],[4,5],[5,6],[6,7],[7,0],[0,8],[1,8],[1,9],[2,9],[3,11],[4,11],
    [8,9],[9,11],[8,10],[9,10],[11,10],[10,5],[10,6],[7,10],[0,12],[12,7],[7,13],[13,6],[6,14],[5,14],[14,15],[5,15],[11,5]];
}
