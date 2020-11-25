import { Component, OnInit } from '@angular/core';
import { DeploymentsStoreService } from '../deployments-store.service';

@Component({
  selector: 'app-deployment-container',
  template: ` <router-outlet></router-outlet> `,
  styles: [],
})
export class DeploymentContainerComponent implements OnInit {
  constructor(private deploymentStore: DeploymentsStoreService) {}

  ngOnInit(): void {
    this.deploymentStore.deployments = [];
  }
}
