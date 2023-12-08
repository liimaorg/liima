import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-deployment-container',
  template: ` <router-outlet></router-outlet> `,
  styles: [],
  standalone: true,
  imports: [RouterOutlet],
})
export class DeploymentContainerComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}
}
