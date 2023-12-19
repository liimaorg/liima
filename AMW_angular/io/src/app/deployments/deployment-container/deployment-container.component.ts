import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { EnvironmentService } from '../../deployment/environment.service';
import { DeploymentService } from '../../deployment/deployment.service';

@Component({
  selector: 'app-deployment-container',
  template: ` <router-outlet></router-outlet> `,
  styles: [],
  standalone: true,
  providers: [EnvironmentService, DeploymentService],

  imports: [RouterOutlet],
})
export class DeploymentContainerComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}
}
